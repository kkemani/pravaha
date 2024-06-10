package org.pravaha.bpmn.dataaccess;

import org.pravaha.bpmn.model.ProcessEventWatchVO;
import org.pravaha.bpmn.model.ProcessRuntimeVO;
import org.pravaha.bpmn.model.ProcessTaskVO;

public interface BpmnProcessDao{
	
	public ProcessRuntimeVO saveProcessRuntime(ProcessRuntimeVO processRuntimeVO);
	
	public ProcessRuntimeVO getProcessRunTime(String processId);
	
	public ProcessEventWatchVO saveProcessEventWatch(ProcessEventWatchVO processEventWatchVO);

	public void deleteEventWatch(Long id);
	
	public ProcessTaskVO updateTaskStatus(String processId, Long taskId, int taskStatus);
	
	public void saveProcessTask(ProcessTaskVO processTaskVO);
	

}
