package org.pravaha.bpmn.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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

import org.pravaha.bpmn.configuration.BpmnConfigHolder;
import org.pravaha.bpmn.configuration.BpmnConfigurationManager;
import org.pravaha.bpmn.dataaccess.BpmnProcessDao;
import org.pravaha.bpmn.defines.BpmnProcessEnum;
import org.pravaha.bpmn.defines.TaskEnum;
import org.pravaha.bpmn.model.ProcessContextVO;
import org.pravaha.bpmn.model.ProcessDefinitionVO;
import org.pravaha.bpmn.model.ProcessEventWatchVO;
import org.pravaha.bpmn.model.ProcessRuntimeVO;
import org.pravaha.bpmn.model.ProcessTaskVO;
import org.pravaha.bpmn.util.VariableListMap;
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

	public BpmnProcessRuntime(String eventType, String correlationId, BpmnProcessDao bpmnProcessDao) {
		processEvent(eventType, correlationId, bpmnProcessDao);
	}

	private void processEvent(String eventType, String correlationId, BpmnProcessDao bpmnProcessDao) {
		// TODO Auto-generated method stub
		
		if (this.bpmnProcessDao == null)
			this.bpmnProcessDao = bpmnProcessDao;
		
		ProcessEventWatchVO eventVo = new ProcessEventWatchVO();
		eventVo = this.bpmnProcessDao.getEventByEventTypeAndCorrId(eventType, correlationId);
		String processId = eventVo.getProcessId();
		
		ProcessRuntimeVO processRTVo = this.bpmnProcessDao.getProcessRunTime(processId);
		String processName = processRTVo.getProcessName();
		
		ProcessDefinitionVO processDefVo = this.bpmnProcessDao.getProcessDefinition(processName);
		String processFileName = processDefVo.getProcessFileName();
		String file = "C:\\Users\\bablu\\Downloads\\" + processFileName;
		BpmnConfigHolder bpmnCfgHolder = BpmnConfigHolder.getInstance();
		BpmnConfigurationManager bpmnCfgManager = bpmnCfgHolder.getBpmnCfgManager(file);
		executionPath = new BpmnExecutionPath();
		executionPath.setBpmnConfigurationManager(bpmnCfgManager);
		
		ProcessContextVO contextVo = this.bpmnProcessDao.getProcessContextByPId(processId);
		VariableListMap variableMap = new VariableListMap();
		Hashtable<String, Object> hashTablemap = variableMap.getVariableListMap(contextVo);
	}
		

	protected void initializeProcess() throws BpmnException {
		BpmnConfigHolder cfgHolder = BpmnConfigHolder.getInstance();
		BpmnConfigurationManager bpmnCfgManager = cfgHolder.getBpmnCfgManager(procConfigFile);
		executionPath = new BpmnExecutionPath();
		executionPath.setBpmnConfigurationManager(bpmnCfgManager);
		Calendar procStartTime = Calendar.getInstance();
		// else - create a new PID (use a timestamp)
		this.processName = bpmnCfgManager.processName;
		this.processVer = bpmnCfgManager.processVersion;
		// String bpmnProcessDef =
		// executionPath.getBpmnConfigurationManager().getBpmnProcessDefinition();
		// create and store a record into the tbl_process_runtime with start time = now
		/*
		 * ProcessRuntimeVO procRuntimeVO = new ProcessRuntimeVO(); if (this.processName
		 * == null) this.processName = this.processName; if (this.processVer == null ||
		 * this.processVer.trim().length() == 0) this.processVer = "1.0"; else
		 * this.processVer;
		 * 
		 * procRuntimeVO.setProcessName(this.processName);
		 * procRuntimeVO.setProcessVer(this.processVer);
		 * procRuntimeVO.setStartDate(procStartTime);
		 * procRuntimeVO.setLastUpdateDate(procStartTime);
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

	public void startProcess() throws BpmnException {
		// save a record for RuntimeVO
		saveProcessDefinition();
		saveProcessRunTime();
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
			saveBpmnProcessContext();
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

	private void saveProcessDefinition() {
		// TODO Auto-generated method stub

		ProcessDefinitionVO vo = new ProcessDefinitionVO();
		String bpmnProcessDef = executionPath.getBpmnConfigurationManager().getBpmnProcessDefinition();
		HashMap<String, Object> mapObj = getProcessDetails(bpmnProcessDef);
		String processName = mapObj.get("Id").toString();
		String procesVersion = mapObj.get("Version").toString();
		String processFileName = executionPath.getBpmnConfigurationManager().getBpmFileName();
		vo.setProcessName(processName);
		vo.setProcessFileName(getFileName(processFileName));
		vo.setProcessVersion(procesVersion);

		if (this.bpmnProcessDao != null && vo != null)
			vo = bpmnProcessDao.saveProcessDefintion(vo);
		else
			System.out.println("BPMN Process DAO is null");

	}

	public ProcessDefinitionVO getProcessDefinition(String processName) {
		return this.bpmnProcessDao.getProcessDefinition(processName) != null?this.bpmnProcessDao.getProcessDefinition(processName):null;
		
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
			saveTaskDetails(oneTask);
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
			savePRTimeWithEndDate(this.processId);
			return true;
		}
		return true;
	}

	public void saveTaskDetails(BpmnTask oneTask) {
		ProcessTaskVO vo = new ProcessTaskVO();
		vo.setProcessId(this.processId);
		vo.setTaskName(oneTask.getTaskId());
		vo.setTaskStatus((int) BpmnProcessEnum.PROCESS_INPROGRESS.getValue());
		vo.setTaskType((int) BpmnProcessEnum.PROCESS_INTERNAL_TASK.getValue());
		vo.setDescription(oneTask.getTaskName());
		if (this.bpmnProcessDao != null)
			this.bpmnProcessDao.saveProcessTask(vo);
	}

	public void saveProcessRunTime() {
		ProcessRuntimeVO vo = new ProcessRuntimeVO();
		String bpmnProcessDef = executionPath.getBpmnConfigurationManager().getBpmnProcessDefinition();
		HashMap<String, Object> mapObj = getProcessDetails(bpmnProcessDef);
		String id = mapObj.get("Id").toString();
		String version = mapObj.get("Version").toString();

		vo.setProcessName(this.processName);
//		vo.setBusinessKey("seygen123"); // from where we are getting or where we are generating
		vo.setStatus((int) BpmnProcessEnum.PROCESS_INPROGRESS.getValue());
		vo.setProcessVer(this.processVer);
		if (this.bpmnProcessDao != null)
			vo = this.bpmnProcessDao.saveProcessRuntime(vo);
		else
			System.out.println("Dao is null ");
		this.processId = vo.getProcessId();
		this.businessKey = vo.getBusinessKey();
	}

	public void savePRTimeWithEndDate(String processId) {
		ProcessRuntimeVO vo = new ProcessRuntimeVO();
		vo = this.bpmnProcessDao.getProcessRunTime(processId);
		vo.setEndDate(Calendar.getInstance().getTime());
		if (this.bpmnProcessDao != null)
			vo = this.bpmnProcessDao.saveProcessRuntime(vo);
	}

	public void saveEventWatchDetails(BpmnTask oneTask, BpmnProcessDao bpmnProcessDao) {
		ProcessEventWatchVO vo = new ProcessEventWatchVO();
		vo.setEventType(oneTask.getTaskId());
		vo.setCorrelationId(this.businessKey);
		vo.setRelatedId(delegateExecution.getVariable("relatedId").toString());
		vo.setProcessId(this.processId);
		vo.setStatus((int) BpmnProcessEnum.PROCESS_INPROGRESS.getValue());
		if (this.bpmnProcessDao != null)
			this.bpmnProcessDao.saveProcessEventWatch(vo);

		System.out.println("BpmnProcessRT:::saveEventWatchDetails::EventType: " + vo.getEventType());
		System.out.println("BpmnProcessRT:::saveEventWatchDetails::CorrelationId: " + vo.getCorrelationId());

	}

	private void saveBpmnProcessContext() throws IOException {
		// TODO Auto-generated method stub
		ProcessContextVO vo = new ProcessContextVO();
		vo.setProcessId(this.processId);
		VariableListMap variableMap = new VariableListMap();
		vo.setProcessContext(variableMap.serializeMap(delegateExecution.getBaseVariableMap()));
		vo.setLastUpdateDate(Calendar.getInstance().getTime());
		if (this.bpmnProcessDao != null)
			this.bpmnProcessDao.saveProcessContext(vo);

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
				String id = process.getAttribute("id");
				String version = process.getAttribute("version");
				map.put("Id", id);

				if (version != null && !version.isEmpty())
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
