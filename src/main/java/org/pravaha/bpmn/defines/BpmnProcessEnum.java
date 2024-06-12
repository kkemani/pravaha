package org.pravaha.bpmn.defines;

public enum BpmnProcessEnum {

	PROCESS_RUNTIME_VERSION_STR("Version"),
	PROCESS_RUNTIME_PROCESS_ID_STR("ProcessId"),
	PROCESS_INPROGRESS(1),
	PROCESS_COMPLETED(2),
	PROCESS_FAILED(3),
	PROCESS_RUNTIME_VERSION(1),
	VAR_TYPE_TRANSIENT(3),
	PROCESS_INTERNAL_TASK(1),
	PROCESS_EXTERNAL_TASK(2),
	BPMN_PROCESS_VERSION(1);
	
	
	 private final Object variableType;
	
	BpmnProcessEnum(Object varType){
		this.variableType = varType;
	}
	
	public Object getValue() {
		return this.variableType;
	}
	
}
