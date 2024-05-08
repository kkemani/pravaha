package org.pravaha.bpmn.model;

import java.util.Calendar;

import lombok.Data;

@Data
public class ProcessTaskVO implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	private long task_id;

	private String processId;

	private String taskName;

	private Calendar startDate;

	private Calendar endDate;

	private int taskStatus;

	private String parentPid;

	private int task_type;

	private String description;

	public ProcessTaskVO() {
		
	}

	public String toString() {
		StringBuffer strbuffer = new StringBuffer();
		strbuffer.append("task_id -" + this.task_id).append("Parent Pid = " + this.parentPid)
				.append("Task Type = " + this.task_type).append("Description -" + this.description)
				.append("Task Status" + this.taskStatus).append("Task Name" + this.taskName);
		return strbuffer.toString();
	}

}
