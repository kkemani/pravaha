package org.pravaha.bpmn.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.jdom2.Namespace;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pravaha.bpmn.defines.TaskEnum;
import org.pravaha.bpmn.engine.BpmnEndEvent;
import org.pravaha.bpmn.engine.BpmnExclusiveGwEvent;
import org.pravaha.bpmn.engine.BpmnSequenceFlow;
import org.pravaha.bpmn.engine.BpmnServiceTask;
import org.pravaha.bpmn.engine.BpmnStartEvent;
import org.pravaha.bpmn.engine.BpmnTask;

public class BpmnConfigurationManager {
	
	final static Logger logger = LoggerFactory.getLogger("BpmnLogger");
	
	protected String bpmFileName = null;

	protected Document smxProcessConfig = null;
	protected Element processDefinition = null;
	protected BpmnTask curExecutionTask = null;
	public String processName = null;
	public String processVersion = null;
	protected String bpmnProcessDefinition = null;

	public static Namespace bpmnNamespace = Namespace.getNamespace(TaskEnum.BPMN_NS.getValue());
	protected Hashtable<String, BpmnTask> processTaskMap = null;
	protected Hashtable<String, BpmnTask> endTaskMap = null;
	protected Hashtable<String, BpmnSequenceFlow> seqFlowMap = null;

	public BpmnConfigurationManager() {
		processTaskMap = new Hashtable<String, BpmnTask>();
		seqFlowMap = new Hashtable<String, BpmnSequenceFlow>();
	}

	public BpmnConfigurationManager(String bpmnFile) {
		this.bpmFileName = bpmnFile;
		processTaskMap = new Hashtable<String, BpmnTask>();
		seqFlowMap = new Hashtable<String, BpmnSequenceFlow>();
	}

	public String getBpmFileName() {
		return bpmFileName;
	}

	public void setBpmFileName(String bpmFileName) {
		this.bpmFileName = bpmFileName;
	}

	public String getBpmnProcessDefinition() {
		return bpmnProcessDefinition;
	}

	public void setBpmnProcessDefinition(String bpmnProcessDefinition) {
		this.bpmnProcessDefinition = bpmnProcessDefinition;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getProcessVersion() {
		return processVersion;
	}

	@SuppressWarnings("deprecation")
//	public void initializeProcessConfiguration(SMXLogger smxLogger){
	public void initializeProcessConfiguration() {
		// build the Document here
//		String procConfigStr = DataUtility.getDatafromFile(this.bpmFileName);
//		String procConfigStr = Paths.;
		if (bpmnProcessDefinition == null && this.bpmFileName != null)
			try {
				bpmnProcessDefinition = new String(Files.readAllBytes(Paths.get(bpmFileName)));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		try {

			SAXBuilder builder = new SAXBuilder();
			bpmnProcessDefinition = bpmnProcessDefinition.replaceAll("camunda:class", "class");
			smxProcessConfig = builder.build(new java.io.StringReader(bpmnProcessDefinition));
			processDefinition = smxProcessConfig.getRootElement().getChild(TaskEnum.BPMN_PROCESS_EL.getValue(),
					bpmnNamespace);
			processName = processDefinition.getAttributeValue("name");
			processVersion = "01";
			loadTasks();
			initializeSequenceFlowLinks();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void initializeSequenceFlowLinks() {
		List<Element> seqFlowList = processDefinition.getChildren(TaskEnum.BPMN_SEQ_FLOW_EL.getValue(), bpmnNamespace);
		if (seqFlowList == null)
			return;

		seqFlowList.forEach(x -> {
			BpmnSequenceFlow oneSeqFlow = getSequenceFlow(x);
			seqFlowMap.put(oneSeqFlow.getLinkId(), oneSeqFlow);
		});

	}

	private BpmnSequenceFlow getSequenceFlow(Element x) {
		String linkId = x.getAttributeValue("id");
		String name = x.getAttributeValue("name");
		String source = x.getAttributeValue("sourceRef");
		String target = x.getAttributeValue("targetRef");
		Element expression = x.getChild("conditionExpression", bpmnNamespace);
		BpmnSequenceFlow oneSeqFlow = new BpmnSequenceFlow(linkId, name, source, target);
		if (expression != null) {
			oneSeqFlow.addExpression(expression.getAttributeValue("language"), expression.getValue());
		}
		return oneSeqFlow;
	}

	protected void loadTasks() {
		List<Element> serviceTaskList = processDefinition.getChildren(TaskEnum.BPMN_SVC_TASK_EL.getValue(),
				bpmnNamespace);
		loadServiceTasks(serviceTaskList);
		List<Element> endNodeList = processDefinition.getChildren(TaskEnum.BPMN_END_EV_EL.getValue(),
				bpmnNamespace);
		loadEndTasks(endNodeList);
		List<Element> exGwNodeList = processDefinition.getChildren(TaskEnum.BPMN_EX_GW_EV_EL.getValue(),
				bpmnNamespace);
		loadExGwTasks(exGwNodeList);
	
	}
	
	private void loadExGwTasks(List<Element> exGwNodeList) {
		// TODO Auto-generated method stub
		if (exGwNodeList == null)
			return;
		
		exGwNodeList.forEach(x -> {
			BpmnExclusiveGwEvent bpmnExGwEvent = new BpmnExclusiveGwEvent(BpmnTask.EX_GW_TASK, x.getAttributeValue("id"),
					x.getAttributeValue("name"));
			logger.debug("Exclusive Gateway Task Id = {} ",x.getAttributeValue("id"));
			 List<Element> outChildElList = x.getChildren("outgoing", bpmnNamespace);
			 int outInstCount = outChildElList!=null? outChildElList.size():0;
			 if(outInstCount>0) {
				 outChildElList.forEach(y -> {
					 bpmnExGwEvent.setOutgoingLink(y.getValue());
				 });
			 }
			processTaskMap.put(x.getAttributeValue("id"), bpmnExGwEvent);

		});
	}
	
	private void loadEndTasks(List<Element> endNodeList) {
		// TODO Auto-generated method stub
		if (endNodeList == null)
			return;
		
		endNodeList.forEach(x -> {
			BpmnEndEvent bpmnEndEvent = new BpmnEndEvent(BpmnTask.END_TASK, x.getAttributeValue("id"),
					x.getAttributeValue("name"));
			logger.debug("End Task Id : --> "+x.getAttributeValue("id"));
			processTaskMap.put(x.getAttributeValue("id"), bpmnEndEvent);

		});
	}
	

	private void loadServiceTasks(List<Element> serviceTaskList) {
		if (serviceTaskList == null)
			return;
		
		serviceTaskList.forEach(x -> {
			String type = x.getAttributeValue("type");
			int taskType = type == null ? BpmnTask.SVC_DELEGATE_TASK : BpmnTask.SVC_EXT_TASK;
			BpmnServiceTask bpmnServiceTask = new BpmnServiceTask(taskType, x.getAttributeValue("id"),
					x.getAttributeValue("name"));
			if (type == null) {
				bpmnServiceTask.setClassName(x.getAttributeValue("class"));
			} else {
				bpmnServiceTask.setTopicName(x.getAttributeValue("topic"));
			}
			Element outgoingEl = x.getChild("outgoing", bpmnNamespace);
			if (outgoingEl != null)
				bpmnServiceTask.setOutgoingLink(outgoingEl.getValue());
			processTaskMap.put(x.getAttributeValue("id"), bpmnServiceTask);
		});
		
	}

	public BpmnTask getNextNode(String id) {
		// check service Tasks
		BpmnTask bpmnTask = processTaskMap.get(id);
		if (bpmnTask != null)
			return bpmnTask;

		return null;
	}

	public void setProcessVersion(String processVersion) {
		this.processVersion = processVersion;
	}

	public BpmnTask getStartTask() {
		List<Element> startEventList = processDefinition.getChildren(TaskEnum.BPMN_START_EV_EL.getValue(),
				bpmnNamespace);
		if (null != startEventList && !startEventList.isEmpty()) {
			Element startEl = startEventList.get(0);
			BpmnStartEvent bpmnStartEv = new BpmnStartEvent(startEl.getAttributeValue("id"),
					startEl.getAttributeValue("name"));
			Element outgoingEl = startEl.getChild("outgoing", bpmnNamespace);
			if (outgoingEl != null)
				bpmnStartEv.setOutgoingLink(outgoingEl.getValue());
			return bpmnStartEv;
		}
		return null;
	}

	public String getSequenceFlowTarget(String outLinkId) {
		BpmnSequenceFlow bpmnSeqFlow = seqFlowMap.get(outLinkId);
		return bpmnSeqFlow != null ? bpmnSeqFlow.getTarget() : null;
	}
	
	public BpmnSequenceFlow getBpmnSequenceFlow(String outLinkId) {
		BpmnSequenceFlow bpmnSeqFlow = seqFlowMap.get(outLinkId);
		return bpmnSeqFlow;
	}

	@Override
	public String toString() {
		return "BpmnConfigurationManager [bpmFileName=" + bpmFileName + ", smxProcessConfig=" + smxProcessConfig
				+ ", processDefinition=" + processDefinition + ", curExecutionTask=" + curExecutionTask
				+ ", processName=" + processName + ", processVersion=" + processVersion + ", processTaskMap="
				+ processTaskMap + ", seqFlowMap=" + seqFlowMap + "]";
	}

}
