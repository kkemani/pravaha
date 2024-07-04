package org.pravaha.bpmn.engine;

import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
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
	protected static long taskId = 0;

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

	protected void initializeProcess() throws BpmnException {
		BpmnConfigHolder cfgHolder = BpmnConfigHolder.getInstance();
		BpmnConfigurationManager bpmnCfgManager = cfgHolder.getBpmnCfgManager(procConfigFile);
		executionPath = new BpmnExecutionPath();
		executionPath.setBpmnConfigurationManager(bpmnCfgManager);
		this.processName = bpmnCfgManager.processName;
		this.processVer = bpmnCfgManager.processVersion;
		try {
			this.pid = UUID.randomUUID().toString();
			delegateExecution = new DelegateExecution();
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

	private boolean processOneNode(BpmnTask oneTask) throws BpmnException {
		if (oneTask instanceof BpmnStartEvent) {
			return false;
		} else if (oneTask instanceof BpmnServiceTask) {
			BpmnServiceTask oneServiceTask = (BpmnServiceTask) oneTask;
			saveTaskDetails(oneTask);
			logger.debug("BpmnProcessRuntime::processOneNode:invoking BpmnServiceTask");
			oneServiceTask.execute(delegateExecution);
			updateTaskDetailsStatus(oneTask);
			return false;
		} else if (oneTask instanceof BpmnExclusiveGwEvent) {
			return false;
		} else if (oneTask instanceof BpmnIntermediateCatchEvent) {
			saveEventWatchDetails(oneTask, this.bpmnProcessDao);
			logger.debug("BpmnProcessRuntime::processOneNode:Found Intemediate Catch Event - stopping flow");
			return true;
		} else if (oneTask instanceof BpmnEndEvent) {
			logger.debug("BpmnProcessRuntime::processOneNode:Found End Event - stopping flow");
			// save into runtime - with endDate
			savePRTimeWithEndDate(this.processId);
			return true;
		}
		return true;
	}

	private void processEvent(String eventType, String correlationId, BpmnProcessDao bpmnProcessDao) {
		// TODO Auto-generated method stub
		if (this.bpmnProcessDao == null)
			this.bpmnProcessDao = bpmnProcessDao;
		String processId = getProcessEventWatchVO(eventType, correlationId).getProcessId();
		String processName = this.bpmnProcessDao.getProcessRunTime(processId).getProcessName();
		String processVersion = this.bpmnProcessDao.getProcessRunTime(processId).getProcessVer();
		String processFileName = this.bpmnProcessDao.getProcessDefinition(processName, processVersion)
				.getProcessFileName();
		String file = "C:\\Users\\bablu\\Downloads\\" + processFileName;
		BpmnConfigurationManager bpmnCfgManager = setBpmnConfigurationManager(file);

		ProcessContextVO contextVo = this.bpmnProcessDao.getProcessContextByPId(processId);
		VariableListMap variableMap = new VariableListMap();
		Hashtable<String, Object> hashTablemap = variableMap.getVariableListMap(contextVo);
		setCurrentTask(hashTablemap, eventType, bpmnCfgManager);
	}

	private BpmnConfigurationManager setBpmnConfigurationManager(String bpmnfile) {
		BpmnConfigHolder bpmnCfgHolder = BpmnConfigHolder.getInstance();
		BpmnConfigurationManager bpmnCfgManager = bpmnCfgHolder.getBpmnCfgManager(bpmnfile);
		executionPath = new BpmnExecutionPath();
		executionPath.setBpmnConfigurationManager(bpmnCfgManager);
		return bpmnCfgManager;
	}

	private void setCurrentTask(Hashtable<String, Object> hashTablemap, String eventType,
			BpmnConfigurationManager bpmnCfgManager) {
		delegateExecution = new DelegateExecution();
		delegateExecution.setVariables(hashTablemap);
		Hashtable<String, BpmnTask> hashTable = bpmnCfgManager.getProcessTaskMap();
		BpmnTask oneTask = hashTable.get(eventType);
		executionPath.setCurrentTask(oneTask);
	}

	public void resumeProcess() {
		BpmnTask oneTask = executionPath.getNextNode(delegateExecution);
		boolean processEndOrWait = false;
		while (!processEndOrWait) {
			try {
				processEndOrWait = processOneNode(oneTask);
			} catch (BpmnException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (processEndOrWait)
				break;

			oneTask = executionPath.getNextNode(delegateExecution);
		}
		try {
			saveBpmnProcessContext();
			ProcessRuntimeVO runTime = this.bpmnProcessDao.getProcessRunTime(this.processId);
			if (runTime != null)
				runTime.setStatus((int) BpmnProcessEnum.PROCESS_INPROGRESS.getValue());
			this.bpmnProcessDao.saveProcessRuntime(runTime);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ProcessEventWatchVO getProcessEventWatchVO(String eventType, String correlationId) {
		ProcessEventWatchVO eventVo = this.bpmnProcessDao.getEventByEventTypeAndCorrId(eventType, correlationId);
		return eventVo;
	}

	public void saveTaskDetails(BpmnTask oneTask) {
		ProcessTaskVO vo = new ProcessTaskVO();
		vo.setProcessId(this.processId);
		vo.setTaskName(oneTask.getTaskId());
		vo.setTaskStatus((int) BpmnProcessEnum.PROCESS_INPROGRESS.getValue());
		vo.setTaskType((int) BpmnProcessEnum.PROCESS_INTERNAL_TASK.getValue());
		vo.setDescription(oneTask.getTaskName());
		if (this.bpmnProcessDao != null)
			vo = this.bpmnProcessDao.saveProcessTask(vo);

		this.taskId = vo.getTaskId();
	}

	public void updateTaskDetailsStatus(BpmnTask oneTask) {
		int status = (int) BpmnProcessEnum.PROCESS_COMPLETED.getValue();
		this.bpmnProcessDao.updateTaskStatus(this.processId, this.taskId, status);
	}

	public void saveProcessRunTime() {
		ProcessRuntimeVO vo = new ProcessRuntimeVO();
		String bpmnProcessDef = executionPath.getBpmnConfigurationManager().getBpmnProcessDefinition();
		HashMap<String, Object> mapObj = getProcessDetails(bpmnProcessDef);
//		String id = mapObj.get("Id").toString();
		String version = mapObj.get("Version").toString();

		vo.setProcessName(this.processName);
		vo.setStatus((int) BpmnProcessEnum.PROCESS_INPROGRESS.getValue());
		vo.setProcessVer(this.processVer);
		if (this.bpmnProcessDao != null)
			vo = this.bpmnProcessDao.saveProcessRuntime(vo);
		else
			logger.debug("Dao is null ");
		this.processId = vo.getProcessId();
		this.businessKey = vo.getBusinessKey();
	}

	public void savePRTimeWithEndDate(String processId) {
		if (this.bpmnProcessDao != null) {
			ProcessRuntimeVO vo = this.bpmnProcessDao.getProcessRunTime(processId);
			vo.setEndDate(Calendar.getInstance().getTime());
			vo.setStatus((int) BpmnProcessEnum.PROCESS_COMPLETED.getValue());
			vo = this.bpmnProcessDao.saveProcessRuntime(vo);
		} else
			logger.debug("BPMN Dao is null ");
	}

	public void saveEventWatchDetails(BpmnTask oneTask, BpmnProcessDao bpmnProcessDao) {
		ProcessEventWatchVO vo = new ProcessEventWatchVO();
		vo.setEventType(oneTask.getTaskId());
		vo.setCorrelationId(this.businessKey);
		vo.setRelatedId(delegateExecution.getVariable("relatedId").toString());
		vo.setProcessId(this.processId);
		vo.setStatus((int) BpmnProcessEnum.PROCESS_INPROGRESS.getValue());
		if (this.bpmnProcessDao != null) {
			this.bpmnProcessDao.saveProcessEventWatch(vo);

			ProcessRuntimeVO runTime = this.bpmnProcessDao.getProcessRunTime(this.processId);
			if (runTime != null)
				runTime.setStatus((int) BpmnProcessEnum.PROCESS_IN_PENDING.getValue());
			this.bpmnProcessDao.saveProcessRuntime(runTime);
		}

		logger.debug("BpmnProcessRT:::saveEventWatchDetails::CorrelationId: ", vo.getCorrelationId());
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
			logger.info("BPMN Process DAO is null");

	}

	public ProcessDefinitionVO getProcessDefinition(String processName, String processVersion) {
		return this.bpmnProcessDao.getProcessDefinition(processName, processVersion) != null
				? this.bpmnProcessDao.getProcessDefinition(processName, processVersion)
				: null;

	}

	public String getFileName(String filepath) {
		String fileName = filepath.substring(filepath.lastIndexOf("\\") + 1);
		return fileName;
	}

	private HashMap<String, Object> getProcessDetails(String processDefinition) {
		HashMap<String, Object> map = new HashMap<>();
		String replacedPD = processDefinition.replaceAll("camunda:versionTag", "versionTag");
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(replacedPD)));

			NodeList processList = document.getElementsByTagName("bpmn:process");
			if (processList.getLength() > 0) {
				Element process = (Element) processList.item(0);
				String id = process.getAttribute("id");
				String version = process.getAttribute("versionTag");
				map.put("Id", id);

				if (version != null && !version.isEmpty())
					map.put("Version", version);
				else
					map.put("Version", BpmnProcessEnum.BPMN_PROCESS_INIT_VERSION.getValue());
			} else {
				logger.info("No BPMN process found in the document.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

}
