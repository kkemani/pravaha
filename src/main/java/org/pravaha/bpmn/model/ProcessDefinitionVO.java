package org.pravaha.bpmn.model;

import lombok.Data;

@Data
public class ProcessDefinitionVO {
	
	private String processName;
	
	private String processFileName;
	
	private String processVersion;

	@Override
	public String toString() {
		return "ProcessDefinitionDomain [processName=" + processName + ", processFileName=" + processFileName
				+ ", processVersion=" + processVersion + "]";
	}

}
