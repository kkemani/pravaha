package org.pravaha.bpmn.defines;

public enum BpmnVariableEnum {

	VAR_TYPE_GLOBAL(1),
	VAR_TYPE_LOCAL(2),
	VAR_TYPE_TRANSIENT(3);
	
	
	 private final int variableType;
	
	BpmnVariableEnum(int varType){
		this.variableType = varType;
	}
	
	public int getValue() {
		return this.variableType;
	}
	
}
