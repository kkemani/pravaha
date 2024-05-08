package org.pravaha.bpmn.model;


import java.io.Serializable;
import java.util.Calendar;

import lombok.Data;



@Data
public class ProcessRuntimeVO implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String processId;
	
	private String processName;
	
	
	private Calendar startDate;


	private Calendar lastUpdateDate;

	private Calendar endDate;

	private int status;

	private String parentProcessId;
	
	private String processVer;


	
}