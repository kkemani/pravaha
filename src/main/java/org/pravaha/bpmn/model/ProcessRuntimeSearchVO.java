package org.pravaha.bpmn.model;

import lombok.Data;
import java.util.Date;

@Data
public class ProcessRuntimeSearchVO {
	
	private Date startDate;
	
	private Date endDate;
	
	public ProcessRuntimeSearchVO() {
		
	}

	public String toString() {
		
		StringBuffer strBuffer= new StringBuffer();
		strBuffer.append(this.startDate +" => "+" || "+this.endDate);
		return strBuffer.toString();
		
	}
}
