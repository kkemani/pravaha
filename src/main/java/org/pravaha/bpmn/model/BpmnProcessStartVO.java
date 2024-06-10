package org.pravaha.bpmn.model;

import lombok.Data;
import java.util.List;

@Data
public class BpmnProcessStartVO {
	
	public BpmnProcessStartVO() {
		
	}
	
	private String fileName;
	private List<BpmnProcessVariable> processVariableList;
	
	
	@Override
	public String toString() {
		return "BpmnProcessStartVO [fileName=" + fileName + ", processVariableList=" + processVariableList + "]";
	}
	
	
	
	
	

}
