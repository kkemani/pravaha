package org.pravaha.bpmn.domain;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Calendar;

@Entity
@Table(name = "Tbl_process_context")
@Data
public class ProcessContextVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "Process_id")
    private String Pid;

    @Column(name = "Process_context")
    private byte[] processContext;

    @Column(name = "Create_Date")
    private Calendar createDate;

    @Column(name = "Last_update_date")
    private Calendar lastUpdateDate;
    
    public ProcessContextVO() {
    	
    }
	
	public String toString()
	{
		StringBuffer strBuffer= new StringBuffer();
		strBuffer.append("Process Id = >"+this.Pid +"||"+this.processContext);
		return strBuffer.toString();
	}
}
