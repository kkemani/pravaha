package org.pravaha.bpmn.engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;

@Data
public class BpmnServiceTask extends BpmnTask implements TaskExecution {
	
	final static Logger logger = LoggerFactory.getLogger("BpmnServiceTask");

	protected String topicName = null;
	protected String className = null;
	
	public BpmnServiceTask(int type, String taskId, String name) {
		super(type, taskId, name);
	}



	@Override
	public void execute(DelegateExecution delExec)throws BpmnException {
		System.out.println("className---> "+className);
		if(className!=null) {
			BpmnServiceTask oneTask = getTaskObject();
			logger.debug("Object returned ="+ oneTask);
			System.out.println("Object Returned : "+oneTask);
			int argSize = 1;
			try {
			Class [] methodDefArgList = new Class[argSize];
			methodDefArgList[0] = DelegateExecution.class;
			Method invokeMethod = oneTask.getClass().getDeclaredMethod("execute", methodDefArgList);
			Object [] methodArgList = new Object[argSize];
			methodArgList[0] = delExec;
			invokeMethod.invoke(oneTask,methodArgList); 
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private BpmnServiceTask getTaskObject() {
		try {
			Class clazz = Class.forName(className);
			Class [] constrArr = new Class[3];
			constrArr[0]= int.class;
			constrArr[1]= String.class;
			constrArr[2]= String.class;
			
			Object [] valueArray = new Object[3];
			valueArray[0] = this.taskType;
			valueArray[1] = this.taskId;
			valueArray[2] = this.taskName;
			Constructor processConstructor = clazz.getConstructor(constrArr);
			Object objClazz = processConstructor.newInstance(valueArray);

			return (BpmnServiceTask) objClazz;
		}catch(Exception e) {
			
		}
		return null;
	}
	


}
