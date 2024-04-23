package org.pravaha.bpmn.engine;

import java.util.ArrayList;
import java.util.List;

public class BpmnTask {

	public static int START_TASK = 100;
	public static int USER_TASK = 101;
	public static int SVC_EXT_TASK = 102;
	public static int SVC_DELEGATE_TASK = 103;
	public static int EX_GW_TASK = 104;
	public static int END_TASK = 120;
	List<String> outgoingLinks = null;
	
	
	protected int taskType = -1;
	protected String taskName = null;
	protected String taskId = null;
	
	public BpmnTask(int taskType, String taskId, String taskName) {
		this.taskType = taskType;
		this.taskId = taskId;
		this.taskName = taskName;
		outgoingLinks = new ArrayList<String>();
	}
	
	public void setOutgoingLink(String link) {
		outgoingLinks.add(link);
	}
	
	public String getTopOutgoingLink() {
		return outgoingLinks.isEmpty()? null:outgoingLinks.get(0);
	}
	
	public List<String> getOutgoingLinks(){
		return outgoingLinks;
	}

	@Override
	public String toString() {
		return "BpmnTask [outgoingLinks=" + outgoingLinks + ", taskType=" + taskType + ", taskName=" + taskName
				+ ", taskId=" + taskId + "]";
	}
	
	
}
