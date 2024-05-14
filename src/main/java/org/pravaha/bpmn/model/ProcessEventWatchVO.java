package org.pravaha.bpmn.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import lombok.Data;

@Data
public class ProcessEventWatchVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	
	private String eventType;
	
	private String correlationId;
	
	private String relatedId;
	
	private String processId;
	
	private Date createDate;
	
	private int status;
	
	public ProcessEventWatchVO() {
		
	}
	
	public String toString()
	{
		StringBuffer strBuffer= new StringBuffer();
		strBuffer.append(this.id +" || " +this.processId +" => "+" || "+this.correlationId+"||"+this.eventType+" || "+this.relatedId+"||"+this.status);
		return strBuffer.toString();
	}
}
