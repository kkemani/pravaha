package org.pravaha.bpmn.model;

import lombok.Data;

@Data
public class BpmnProcessVariable {

	private String name;
	private Object variable;
}
