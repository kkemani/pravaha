package org.pravaha.bpmn.test;

import org.junit.Test;
import org.pravaha.bpmn.configuration.BpmnConfigurationManager;
import org.pravaha.bpmn.util.BpmnUtil;

public class BpmnConfigurationManagerTest {

//	@Test
	public void getToString() {
		try {
			BpmnUtil bpmnUtil = new BpmnUtil();
			String bpmnFile = bpmnUtil.getBPMNFile("src/main/resources/SampleBpmnProcess.bpmn");
			BpmnConfigurationManager obj = new BpmnConfigurationManager();
			obj.setBpmnProcessDefinition(bpmnFile);
			obj.initializeProcessConfiguration();
			System.out.println(obj.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
