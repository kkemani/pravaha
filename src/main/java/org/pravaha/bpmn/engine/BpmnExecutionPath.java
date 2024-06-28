package org.pravaha.bpmn.engine;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;

import org.pravaha.bpmn.configuration.BpmnConfigHolder;
import org.pravaha.bpmn.configuration.BpmnConfigurationManager;
import org.pravaha.bpmn.evaluator.ExpressionEvaluator;

@Data
public class BpmnExecutionPath {
	final static Logger logger = LoggerFactory.getLogger("BpmnExecutionPath");

	protected BpmnConfigurationManager bpmnConfigurationManager;
	protected BpmnTask currentTask;

	public BpmnExecutionPath() {}

	public void setBpmnConfigurationManager(BpmnConfigurationManager bpmnCfgManager) {
		this.bpmnConfigurationManager = bpmnCfgManager;
	}
	

	public BpmnTask getStartTask() {
		this.currentTask = bpmnConfigurationManager.getStartTask();
		return this.currentTask;
	}

	public BpmnTask getNextNode(DelegateExecution delegateExecution) {
		BpmnTask bpmnTask = null;
		if (this.currentTask instanceof BpmnStartEvent) {
			String outgoingId = this.currentTask.getTopOutgoingLink();
			System.out.println("OutId : "+outgoingId); 
			if (null != outgoingId) {
				bpmnTask = getNodeFromLink(outgoingId);
				this.currentTask = bpmnTask;
			}
			return bpmnTask;
		} else if (this.currentTask instanceof BpmnExclusiveGwEvent) { // for exclusive gateways
			List<String> outIds = this.currentTask.getOutgoingLinks();
			
			List<BpmnSequenceFlow> seqFlows = new ArrayList<BpmnSequenceFlow>();
			BpmnSequenceFlow noExpSeqFlow = null;
			for (String oneOutId : outIds) {
				BpmnSequenceFlow oneBpmnSeqFlow = new BpmnSequenceFlow();
				oneBpmnSeqFlow = bpmnConfigurationManager.getBpmnSequenceFlow(oneOutId);
				if (null != oneBpmnSeqFlow) {
					if (null != oneBpmnSeqFlow.getExpression()) {
						seqFlows.add(oneBpmnSeqFlow);
					} else
						noExpSeqFlow = oneBpmnSeqFlow;
				} else
					logger.info("BpmnExecutionPath::getNextNode:oneBpmnSeqFlow is null...");
			}
			if (!seqFlows.isEmpty()) {
				boolean res = false;
				// Expression Evaluator
				ExpressionEvaluator exEval = new ExpressionEvaluator();

				for (BpmnSequenceFlow oneSeqflow : seqFlows) {
					res = exEval.evaluateComplexExpression(oneSeqflow.getExpression().getExpression(),
							delegateExecution.getBaseVariableMap());
					logger.debug("Expression = {} res={}", oneSeqflow.getExpression().getExpression(), res);
					if (res) {
						logger.debug("Expression = {}", oneSeqflow.getExpression().getExpression(),
								" is correctly Evaluated.");
						this.currentTask = getNodeFromLink(oneSeqflow.linkId);
						System.out.println("Current Task : "+this.currentTask);
						return getNodeFromLink(oneSeqflow.linkId);
					}

				}
				if (!res) {
					logger.debug("ExpEval res is false - returning noExp Link={}", noExpSeqFlow.getLinkId());
					this.currentTask = getNodeFromLink(noExpSeqFlow.linkId);
					System.out.println("Current Task : "+this.currentTask);
					return getNodeFromLink(noExpSeqFlow.getLinkId());
				}
			} else {
				logger.debug("List of BpmnSequenceFlow has no expression - returning noExp Link={}",
						noExpSeqFlow.getLinkId());
				this.currentTask = getNodeFromLink(noExpSeqFlow.linkId);
				System.out.println("Current Task : "+this.currentTask);
				return getNodeFromLink(noExpSeqFlow.getLinkId());
			}
		} else { // other tasks
					// get current task list of outgoing links - will be implemented later as API
					// will change to list
			String outgoingId = this.currentTask.getTopOutgoingLink();
			logger.debug("getNextNode outgoingId={}  ", outgoingId);
			if (null != outgoingId) {
				bpmnTask = getNodeFromLink(outgoingId);
				this.currentTask = bpmnTask;
			}
			return bpmnTask;
		}
		return null;
	}

	private BpmnTask getNodeFromLink(String outgoingId) {
		String target = bpmnConfigurationManager.getSequenceFlowTarget(outgoingId);
		// check service tasks 
		BpmnTask bpmnTask = bpmnConfigurationManager.getNextNode(target);
		if(bpmnTask!=null)
			return bpmnTask;
		// check Exclusive Gateways
		
		// check Inclusive Gateways
		return null;
	}
}
