package org.pravaha.bpmn;

import java.util.List;

import org.pravaha.bpmn.configuration.BpmnConfigurationManager;
import org.pravaha.bpmn.util.BpmnUtil;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	
    	BpmnUtil bpmnUtil = new BpmnUtil();
    	try {
			String str = bpmnUtil.getBPMNFile("src/main/resources/SampleBpmnProcess.bpmn");
			System.out.println("----------->>>"+str);
			BpmnConfigurationManager obj = new BpmnConfigurationManager();
			obj.setBpmnProcessDefinition(str);
			obj.initializeProcessConfiguration();
			System.out.println(obj.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
