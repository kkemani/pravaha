package org.pravaha.bpmn.model;


import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class ProcessRuntimeVO implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String processId;
	
	private String businessKey;
	
	private String processName;
	
	private Date startDate;

	private Date lastUpdateDate;

	private Date endDate;

	private int status;

	private String parentProcessId;
	
	private String processVer;

	@Override
	public String toString() {
		return "ProcessRuntimeVO [processId=" + processId + ", businessKey=" + businessKey + ", processName="
				+ processName + ", startDate=" + startDate + ", lastUpdateDate=" + lastUpdateDate + ", endDate="
				+ endDate + ", status=" + status + ", parentProcessId=" + parentProcessId + ", processVer=" + processVer
				+ "]";
	}
	
	


	
}