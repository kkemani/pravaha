package org.pravaha.bpmn.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

@Data
public class ProcessContextVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String processId;

    private byte[] processContext;

    private Date createDate;

    private Date lastUpdateDate;
    
    public ProcessContextVO() {
    	
    }
	
	public String toString()
	{
		StringBuffer strBuffer= new StringBuffer();
		strBuffer.append("Process Id = >"+this.processId +"||"+this.processContext);
		return strBuffer.toString();
	}
}
