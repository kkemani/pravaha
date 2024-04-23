package org.pravaha.bpmn.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.security.auth.x500.X500Principal;
import javax.swing.text.StyledEditorKit.ForegroundAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pravaha.bpmn.configuration.BpmnConfigurationManager;
import org.pravaha.bpmn.evaluator.ExpressionEvaluator;

public class BpmnExecutionPath {
	final static Logger logger = LoggerFactory.getLogger("");

	protected BpmnConfigurationManager bpmnCfgManager;
	protected BpmnTask currentTask;

	public BpmnExecutionPath() {}
	
	public BpmnExecutionPath(String bmpnConfigFile) {
		bpmnCfgManager = new BpmnConfigurationManager(bmpnConfigFile);
		bpmnCfgManager.initializeProcessConfiguration();
	}
	
	public BpmnConfigurationManager getBpmnCfgManager() {
		return bpmnCfgManager;
	}

	public void setBpmnCfgManager(BpmnConfigurationManager bpmnCfgManager) {
		this.bpmnCfgManager = bpmnCfgManager;
	}
	

	public BpmnTask getCurrentTask() {
		return currentTask;
	}
	
	public void setCurrentTask(BpmnTask currentTask) {
		this.currentTask = currentTask;
	}

	public BpmnTask getStartTask() {
		this.currentTask = bpmnCfgManager.getStartTask();
		return this.currentTask;
	}

	public BpmnTask getNextNode(DelegateExecution delegateExecution) {
		BpmnTask bpmnTask = null;
		if(this.currentTask instanceof BpmnStartEvent) {
			String outgoingId = this.currentTask.getTopOutgoingLink();
			if(null!=outgoingId) {
				bpmnTask = getNodeFromLink(outgoingId);
				this.currentTask = bpmnTask;
			}
			return bpmnTask;
		}else if(this.currentTask instanceof BpmnExclusiveGwEvent) {
			List<String> outIds = this.currentTask.getOutgoingLinks();
			List<BpmnSequenceFlow> seqFlows = new ArrayList<BpmnSequenceFlow>();
			BpmnSequenceFlow noExpSeqFlow = null;
			for (String oneOutId : outIds) {
				BpmnSequenceFlow oneBpmnSeqFlow = new BpmnSequenceFlow();
				oneBpmnSeqFlow = bpmnCfgManager.getBpmnSequenceFlow(oneOutId);
				if(null!=oneBpmnSeqFlow) {
					if(null!=oneBpmnSeqFlow.getExpression()) {
						seqFlows.add(oneBpmnSeqFlow);
					} else 
						noExpSeqFlow = oneBpmnSeqFlow;
				} else
					logger.info("BpmnExecutionPath::getNextNode:oneBpmnSeqFlow is null...");
			}
			
			if(!seqFlows.isEmpty()) {
				// Expression Evaluator
				ExpressionEvaluator exEval = new ExpressionEvaluator();
				for(BpmnSequenceFlow oneSeqflow: seqFlows) {
					boolean res = exEval.evaluateComplexExpression(oneSeqflow.getExpression().getExpression(),delegateExecution);
					if(res) {
						logger.info("Expression ",oneSeqflow.getExpression().getExpression()," is correctly Evaluated.");
						return getNodeFromLink(oneSeqflow.linkId);
					}
				}
				
			}else{
				return getNodeFromLink(noExpSeqFlow.getLinkId());
			}
		} 
		else {
			// get current task list of outgoing links - will be implemented later as API will chaneg to list
			String outgoingId = this.currentTask.getTopOutgoingLink();
			logger.debug("getNextNode outgoingId={}  ",outgoingId);
			if(null!=outgoingId) {
				bpmnTask = getNodeFromLink(outgoingId);
				this.currentTask = bpmnTask;
			}
			return bpmnTask;
		}
		return null;
	}

	private BpmnTask getNodeFromLink(String outgoingId) {
		String target = bpmnCfgManager.getSequenceFlowTarget(outgoingId);
		// check service tasks 
		BpmnTask bpmnTask = bpmnCfgManager.getNextNode(target);
		if(bpmnTask!=null)
			return bpmnTask;
		// check Exclusive Gateways
		
		// check Inclusive Gateways
		return null;
	}
}
