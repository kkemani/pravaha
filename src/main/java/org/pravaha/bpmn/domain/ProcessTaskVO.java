package org.pravaha.bpmn.domain;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
//import javax.xml.bind.annotation.XmlRootElement;

@Entity(name = "com.seygen.smx.bpo.domain.SMXProcessTaskVO")
//@XmlRootElement
@Table(name = "TBL_PROCESS_TASK")

public class ProcessTaskVO implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
//	@GeneratedValue(strategy = GenerationType.AUTO, generator = "task_seq")
//	@SequenceGenerator(name = "task_seq", sequenceName = "SMX_BPO_TASK_SEQ")
	@Column(name = "task_id")
	private long task_id;

	@Column(name = "process_id")
	private String processId;

	@Column(name = "Task_name")
	private String taskName;

	@Column(name = "Start_date")
	private Calendar startDate;

	@Column(name = "End_Date")
	private Calendar endDate;

	@Column(name = "Task_status")
	private int taskStatus;

	@Column(name = "Parent_process_id")
	private String parentPid;

	@Column(name = "Task_type")
	private int task_type;

	@Column(name = "Description")
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
