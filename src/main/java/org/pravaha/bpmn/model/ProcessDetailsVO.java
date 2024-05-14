package org.pravaha.bpmn.model;

import java.util.List;

import lombok.Data;

@Data
public class ProcessDetailsVO {

	private String processId;
	
	private ProcessRuntimeVO processRuntimeVO;
	
	private ProcessContextVO processContextVO;
	
	private List<ProcessEventWatchVO> processEventWatchVO;
	
	private List<ProcessTaskVO> processTaskVO;
	
	public ProcessDetailsVO() {
		
	}
	
	@Override
	public String toString() {
		return "ProcessDetailsVO [processId=" + processId + ", processRuntimVO=" + processRuntimeVO
				+ ", processContextVO=" + processContextVO + ", processEventWatchVO=" + processEventWatchVO
				+ ", processTaskVO=" + processTaskVO + "]";
	}

}
