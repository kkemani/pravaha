package org.pravaha.bpmn.model;

import java.util.Calendar;

import lombok.Data;

@Data
public class ProcessTaskVO implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	private long taskId;

	private String processId;

	private String taskName; //bpmn:serviceTask id="Activity_1clykiy

	private Calendar startDate;

	private Calendar endDate;

	private int taskStatus;

	private String parentPid;

	private int taskType; // 1 is internal and 2 is external

	private String description; // name 

	public ProcessTaskVO() {
		
	}

	public String toString() {
		StringBuffer strbuffer = new StringBuffer();
		strbuffer.append("taskId -" + this.taskId).append("Parent Pid = " + this.parentPid)
				.append("Task Type = " + this.taskType).append("Description -" + this.description)
				.append("Task Status" + this.taskStatus).append("Task Name" + this.taskName);
		return strbuffer.toString();
	}

}
