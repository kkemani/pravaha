package org.pravaha.bpmn.engine;

public class BpmnStartEvent extends BpmnTask {

	public BpmnStartEvent(String taskId, String taskName) {
		super(START_TASK, taskId, taskName);
	}
	
}
