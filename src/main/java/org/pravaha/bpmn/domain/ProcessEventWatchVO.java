package org.pravaha.bpmn.domain;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name="Tbl_process_event_watch")
@Data
public class ProcessEventWatchVO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column (name="Id")
	private long id;
	
	@Column (name="Event_type")
	private String eventType;
	
	@Column(name="Correlation_id")
	private String correlationId;
	
	@Column(name="relatedId")
	private String relatedId;
	
	@Column(name="Process_id")
	private String Pid;
	
	@Column(name="Create_date")
	private Calendar createDate;
	
	@Column(name="status")
	private int status;
	
	public ProcessEventWatchVO() {
		
	}
	
	public String toString()
	{
		StringBuffer strBuffer= new StringBuffer();
		strBuffer.append(this.id +" || " +this.Pid +" => "+" || "+this.correlationId+"||"+this.eventType+" || "+this.relatedId+"||"+this.status);
		return strBuffer.toString();
	}
}
