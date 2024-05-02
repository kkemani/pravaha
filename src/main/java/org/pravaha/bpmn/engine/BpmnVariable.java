package org.pravaha.bpmn.engine;

import lombok.Data;

@Data
public class BpmnVariable {

	public Object variable;
	public int scope;
}
