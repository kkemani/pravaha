package org.pravaha.bpmn.engine;

import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pravaha.bpmn.defines.BpmnVariableEnum;

public class DelegateExecution {
	final static Logger logger = LoggerFactory.getLogger("DelegateExecution");

	protected Map<String, BpmnVariable> variableMap; 
	protected int taskStatus;
	
	public DelegateExecution() {
		variableMap = new Hashtable<>();
	}
	// add as global 
	public void setVariables(Map<String, Object> varMap) {
		System.out.println("DelegateExecution:::Setvariables:isNull");
		System.out.println(varMap!=null);
		varMap.keySet().forEach(x -> {
			Object oneObj = varMap.get(x);
			logger.debug("DelegateExecution::setVariables:oneObj={} ",oneObj);
			BpmnVariable oneVar = new BpmnVariable();
			oneVar.variable = oneObj;
			oneVar.scope = BpmnVariableEnum.VAR_TYPE_GLOBAL.getValue();
			variableMap.put(x, oneVar);
		});
	}
	public Object getVariable(String key) {
		BpmnVariable oneVar = variableMap.get(key);
		if(oneVar!=null)
			return oneVar.variable;
		
		return null;
	}
	public final Map<String, Object> getBaseVariableMap() {
		Map<String, Object> varMap = new Hashtable<>();
		variableMap.keySet().forEach(x -> {
			BpmnVariable oneValue = variableMap.get(x);
			varMap.put(x, oneValue.variable);
		});
		
		return varMap;
	}
	@Override
	public String toString() {
		return "DelegateExecution [variableMap=" + variableMap + ", taskStatus=" + taskStatus + "]";
	}
	
	
}
