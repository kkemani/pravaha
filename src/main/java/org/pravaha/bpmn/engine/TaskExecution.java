package org.pravaha.bpmn.engine;

public interface TaskExecution {

	public void execute(DelegateExecution delExec) throws BpmnException;
}
