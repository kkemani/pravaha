package org.pravaha.bpmn.engine;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BpmnProcessRuntime {
	final static Logger logger = LoggerFactory.getLogger("BpmnProcessRuntime");
	
	protected String pid = null;
	protected String processName = null;
	protected String processVer = null;
	protected String procConfigFile = null;
	protected BpmnExecutionPath executionPath = null;
	protected DelegateExecution delegateExecution;

	public BpmnProcessRuntime(String processConfigFile) {
		this.procConfigFile = processConfigFile;
		this.pid = UUID.randomUUID().toString();
		try{
			initializeProcess();
		} catch(BpmnException e) {
			e.printStackTrace();
		}
	}

	protected void initializeProcess() throws BpmnException {
		executionPath = new BpmnExecutionPath(procConfigFile);
		Calendar procStartTime = Calendar.getInstance();
		// else - create a new PID (use a timestamp)
		this.processName = executionPath.getBpmnCfgManager().processName;
		this.processVer = executionPath.getBpmnCfgManager().processVersion;
		// create and store a record into the tbl_process_runtime with start time = now
		/*
		if (procRuntimeVO == null)
			procRuntimeVO = new SMXProcessRuntimeVO();
		if (this.processName == null)
			this.processName = "DUMMY-PROCESS-NAME";
		if (this.processVer == null || this.processVer.trim().length() == 0)
			this.processVer = "1.0";

		procRuntimeVO.setProcessName(this.processName);
		procRuntimeVO.setStartDate(procStartTime);
		procRuntimeVO.setLastUpdateDate(procStartTime);
		procRuntimeVO.setProcessVer(this.processVer);
		*/
		try {
			this.pid = UUID.randomUUID().toString();
			delegateExecution = new DelegateExecution();
			//procRuntimeVO.setProcessId(this.pid);
			//this.smxProcDM.stroreProcessRecord(procRuntimeVO);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setVariables(Hashtable<String, Object> processVariables) {
		delegateExecution.setVariables(processVariables);
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

//	private void startProcess() throws BpmnException {
	public void startProcess() throws BpmnException {
		// TODO Auto-generated method stub
		// iterate through the links and move to the next node
		BpmnTask oneTask = executionPath.getStartTask();
		boolean processEndOrWait = false;
		while (!processEndOrWait) {
			processEndOrWait = processOneNode(oneTask);

			if (processEndOrWait)
				break;
			
			oneTask = executionPath.getNextNode(delegateExecution);
			logger.debug("Next task : oneTask="+ oneTask);
//			if (oneTask == null || oneTask.taskType == BpmnTask.END_TASK) {
//				processEndOrWait = true;
//			}
		}

		/*
		procRuntimeVO.setEndDate(Calendar.getInstance());
		procRuntimeVO.setStatus(SMX_STOP_EVENT);
		procRuntimeVO.setLastUpdateDate(Calendar.getInstance());
		*/

	}

	private boolean processOneNode(BpmnTask oneTask) throws BpmnException {
		if (oneTask instanceof BpmnStartEvent)
			return false;

		else if(oneTask instanceof BpmnServiceTask) {
			BpmnServiceTask oneServiceTask = (BpmnServiceTask) oneTask;
			logger.debug("BpmnProcessRuntime::processOneNode:invoking BpmnServiceTask");
			oneServiceTask.execute(delegateExecution);
			return false;
		} else if(oneTask instanceof BpmnExclusiveGwEvent) {
			return false;
		}
		else if (oneTask instanceof BpmnEndEvent) {
			logger.debug("BpmnProcessRuntime::processOneNode:Found End Event - stopping flow");
			return true;
		}
		return true;
	}
}
