package org.pravaha.bpmn.dataaccess;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;

import org.pravaha.bpmn.model.ProcessRuntimeVO;

public class ProcessRuntimeDao {

	public ProcessRuntimeDao() {
		
	}
	
	public ProcessRuntimeVO getProcessRuntime(String processId) {
		return null;
	}
	
	public ProcessRuntimeVO saveProcessRuntime(ProcessRuntimeVO procRuntimeVO) {
		return null;
	}
	
	public void updateProcessRuntime(Calendar endTime, Integer status) {
		
	}
	
	public List<ProcessRuntimeVO> getProcessRuntimeByDate(Date startDate, Date endDate) {
		return null;
	}
	
}
