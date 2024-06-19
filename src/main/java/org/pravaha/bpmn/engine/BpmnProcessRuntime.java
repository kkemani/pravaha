package org.pravaha.bpmn.engine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jdom2.Namespace;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.pravaha.bpmn.dataaccess.BpmnProcessDao;
import org.pravaha.bpmn.defines.BpmnProcessEnum;
import org.pravaha.bpmn.defines.TaskEnum;
import org.pravaha.bpmn.model.ProcessContextVO;
import org.pravaha.bpmn.model.ProcessDefinitionVO;
import org.pravaha.bpmn.model.ProcessEventWatchVO;
import org.pravaha.bpmn.model.ProcessRuntimeVO;
import org.pravaha.bpmn.model.ProcessTaskVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import lombok.Data;

@Data
public class BpmnProcessRuntime {
	final static Logger logger = LoggerFactory.getLogger("BpmnProcessRuntime");

	protected String pid = null;
	protected String processName = null;
	protected String processVer = null;
	protected String procConfigFile = null;
	protected BpmnExecutionPath executionPath = null;
	protected DelegateExecution delegateExecution;
	protected BpmnProcessDao bpmnProcessDao;
	protected static String processId = null;
	protected static String businessKey = null;
	

	public static Namespace bpmnNamespace = Namespace.getNamespace(TaskEnum.BPMN_NS.getValue());

	public BpmnProcessRuntime(String processConfigFile) {
		this.procConfigFile = processConfigFile;
		this.pid = UUID.randomUUID().toString();
		try {
			initializeProcess();
		} catch (BpmnException e) {
			e.printStackTrace();
		}
	}

	protected void initializeProcess() throws BpmnException {
		executionPath = new BpmnExecutionPath(procConfigFile);
		Calendar procStartTime = Calendar.getInstance();
		// else - create a new PID (use a timestamp)
		this.processName = executionPath.getBpmnCfgManager().processName;
		this.processVer = executionPath.getBpmnCfgManager().processVersion;
		String bpmnProcessDef = executionPath.getBpmnCfgManager().getBpmnProcessDefinition();
		// create and store a record into the tbl_process_runtime with start time = now
		/*
		 * if (procRuntimeVO == null) procRuntimeVO = new SMXProcessRuntimeVO(); if
		 * (this.processName == null) this.processName = "DUMMY-PROCESS-NAME"; if
		 * (this.processVer == null || this.processVer.trim().length() == 0)
		 * this.processVer = "1.0";
		 * 
		 * procRuntimeVO.setProcessName(this.processName);
		 * procRuntimeVO.setStartDate(procStartTime);
		 * procRuntimeVO.setLastUpdateDate(procStartTime);
		 * procRuntimeVO.setProcessVer(this.processVer);
		 */
		try {
			this.pid = UUID.randomUUID().toString();
			delegateExecution = new DelegateExecution();
			// procRuntimeVO.setProcessId(this.pid);
			// this.smxProcDM.stroreProcessRecord(procRuntimeVO);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setVariables(Hashtable<String, Object> processVariables) {
		delegateExecution.setVariables(processVariables);
	}

	public void startProcess() throws BpmnException{
		saveProcessDefinition(this.bpmnProcessDao);
		// save a record for RuntimeVO
		saveProcessRunTime(this.bpmnProcessDao);
		// iterate through the links and move to the next node
		BpmnTask oneTask = executionPath.getStartTask();
		logger.debug("Next task : oneTask={} ", oneTask);
		boolean processEndOrWait = false;
		while (!processEndOrWait) {
			processEndOrWait = processOneNode(oneTask);
			if (processEndOrWait)
				break;

			oneTask = executionPath.getNextNode(delegateExecution);
		}
		try {
			saveBpmnProcessContext(this.bpmnProcessDao);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * procRuntimeVO.setEndDate(Calendar.getInstance());
		 * procRuntimeVO.setStatus(SMX_STOP_EVENT);
		 * procRuntimeVO.setLastUpdateDate(Calendar.getInstance());
		 */

	}


	private void saveProcessDefinition(BpmnProcessDao bpmnProcessDao) {
		// TODO Auto-generated method stub
		ProcessDefinitionVO vo = new ProcessDefinitionVO();
		String bpmnProcessDef = executionPath.getBpmnCfgManager().getBpmnProcessDefinition();
		HashMap<String, Object> mapObj = getProcessDetails(bpmnProcessDef);
		String processName = mapObj.get("ProcessId").toString();
		String procesVersion = mapObj.get("Version").toString();
		String processFileName = executionPath.getBpmnCfgManager().getBpmFileName();
		vo.setProcessName(processName);
		vo.setProcessFileName(getFileName(processFileName));
		vo.setProcessVersion(procesVersion);
		vo = bpmnProcessDao.saveProcessDefintion(vo);
		
		
	}
	
	public String getFileName(String filepath) {
	        String fileName = filepath.substring(filepath.lastIndexOf("\\") + 1);
	        return fileName;
	}

	private boolean processOneNode(BpmnTask oneTask) throws BpmnException {
		if (oneTask instanceof BpmnStartEvent) {
			return false;
		} else if (oneTask instanceof BpmnServiceTask) {
			BpmnServiceTask oneServiceTask = (BpmnServiceTask) oneTask;
			saveTaskDetails(oneTask, this.bpmnProcessDao);
			logger.debug("BpmnProcessRuntime::processOneNode:invoking BpmnServiceTask");
			oneServiceTask.execute(delegateExecution);
			return false;
		} else if (oneTask instanceof BpmnExclusiveGwEvent) {
			return false;
		} else if (oneTask instanceof BpmnIntermediateCatchEvent) {
			saveEventWatchDetails(oneTask, this.bpmnProcessDao);
			System.out.println("BpmnProcessRuntime::processOneNode:Found Intemediate Catch Event - stopping flow");
			logger.debug("BpmnProcessRuntime::processOneNode:Found Intemediate Catch Event - stopping flow");
			return true;
		} else if (oneTask instanceof BpmnEndEvent) {
			System.out.println("BpmnProcessRuntime::processOneNode:Found End Event - stopping flow");
			logger.debug("BpmnProcessRuntime::processOneNode:Found End Event - stopping flow");
			// save into runtime - with endDate
			savePRTimeWithEndDate(this.processId,this.bpmnProcessDao);
			return true;
		}
		return true;
	}

	public void saveTaskDetails(BpmnTask oneTask, BpmnProcessDao bpmnProcessDao) {
		ProcessTaskVO vo = new ProcessTaskVO();
		vo.setProcessId(this.processId);
		vo.setTaskName(oneTask.getTaskId());
		vo.setTaskStatus((int)BpmnProcessEnum.PROCESS_INPROGRESS.getValue());
		vo.setTaskType((int)BpmnProcessEnum.PROCESS_INTERNAL_TASK.getValue());
		vo.setDescription(oneTask.getTaskName());
		if(bpmnProcessDao!=null)
			bpmnProcessDao.saveProcessTask(vo);
	}
	
	

	public void saveProcessRunTime(BpmnProcessDao bpmnProcessDao) {
		ProcessRuntimeVO vo = new ProcessRuntimeVO();
		String bpmnProcessDef = executionPath.getBpmnCfgManager().getBpmnProcessDefinition();
		HashMap<String, Object> mapObj = getProcessDetails(bpmnProcessDef);
		String processName = mapObj.get("ProcessId").toString();
		String version = mapObj.get("Version").toString();
		
		vo.setProcessName(processName);
//		vo.setBusinessKey("seygen123"); // from where we are getting or where we are generating
		vo.setStatus((int)BpmnProcessEnum.PROCESS_INPROGRESS.getValue());
		vo.setProcessVer(version);
		if(bpmnProcessDao!=null)
			vo = bpmnProcessDao.saveProcessRuntime(vo);
		this.processId = vo.getProcessId();
		this.businessKey = vo.getBusinessKey();
	}
	
	public void savePRTimeWithEndDate(String processId, BpmnProcessDao bpmnProcessDao) {
		ProcessRuntimeVO vo = new ProcessRuntimeVO();
		vo = bpmnProcessDao.getProcessRunTime(processId);
		vo.setEndDate(Calendar.getInstance().getTime());
		if(bpmnProcessDao!=null)
			vo = bpmnProcessDao.saveProcessRuntime(vo);
	}
	
	public void saveEventWatchDetails(BpmnTask oneTask, BpmnProcessDao bpmnProcessDao) {
		ProcessEventWatchVO vo = new ProcessEventWatchVO();
		vo.setEventType(oneTask.getTaskId());
		vo.setCorrelationId(this.businessKey);
		vo.setRelatedId(delegateExecution.getVariable("relatedId").toString());
		vo.setProcessId(this.processId);
		vo.setStatus((int)BpmnProcessEnum.PROCESS_INPROGRESS.getValue());
		if(bpmnProcessDao!=null)	
			bpmnProcessDao.saveProcessEventWatch(vo);
	}
	

	private void saveBpmnProcessContext(BpmnProcessDao bpmnProcessDao) throws IOException{
		// TODO Auto-generated method stub
		ProcessContextVO vo = new ProcessContextVO();
		vo.setProcessId(this.processId);
		vo.setProcessContext(serializeMap(delegateExecution.getBaseVariableMap()));
		vo.setLastUpdateDate(Calendar.getInstance().getTime());
		if(bpmnProcessDao!=null)
			bpmnProcessDao.saveProcessContext(vo);
		
	}
	
	private byte[] serializeMap(Map<String, Object> map) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(map);
            return bos.toByteArray();
        }
    }

	
	private HashMap<String, Object> getProcessDetails(String processDefinition) {
	    HashMap<String, Object> map = new HashMap<>();
	    String replacedPD = processDefinition.replaceAll("camunda:versionTag", "version");
	    try {
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        Document document = builder.parse(new InputSource(new StringReader(replacedPD)));

	        NodeList processList = document.getElementsByTagName("bpmn:process");
	        if (processList.getLength() > 0) {
	            Element process = (Element) processList.item(0);
	            String processId = process.getAttribute("id");
	            String version = process.getAttribute("version");
	            map.put("ProcessId", processId);
	           
	            if(version!=null && !version.isEmpty())	            	
	            	map.put("Version", version);
	            else 
	            	map.put("Version", BpmnProcessEnum.BPMN_PROCESS_INIT_VERSION.getValue());
	        } else {
	            System.err.println("No BPMN process found in the document.");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return map;
	}

}
