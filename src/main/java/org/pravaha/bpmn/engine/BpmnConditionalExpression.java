package org.pravaha.bpmn.engine;

//import com.seygen.smx.bpo.engine.SMXProcessContext;

public class BpmnConditionalExpression {

	protected String lang;
	protected String expression;

	
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	
//	public Object evaluateExpression(SMXProcessContext smxContext) {
//		return null;
//	}
}
