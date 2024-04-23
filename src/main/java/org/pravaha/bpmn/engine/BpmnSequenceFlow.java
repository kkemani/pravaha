package org.pravaha.bpmn.engine;

public class BpmnSequenceFlow {

	protected String linkId;

	protected String name;
	protected String source;
	protected String target;
	protected BpmnConditionalExpression expression;

	// support below samples
	// <bpmn:sequenceFlow id="videoLobFlow" name="Video Lob Flow" sourceRef="HasVideoLOB" targetRef="Activity_02stguj">
	//   <bpmn:conditionExpression xsi:type="bpmn:tFormalExpression" language="groovy">execution.getVariable('lobVideo') == 'true'</bpmn:conditionExpression>
	//    </bpmn:sequenceFlow>
	
	public BpmnSequenceFlow() {
		
	}
	
	
	public BpmnSequenceFlow(String linkId, String name, String source, String target) {
		this.linkId = linkId;
		this.name = name;
		this.source = source;
		this.target = target;
	}
	
	public void addExpression(String lang, String expressionValue) {
		expression = new BpmnConditionalExpression();
		expression.lang = lang;
		expression.expression = expressionValue;
	}

	public String getLinkId() {
		return linkId;
	}

	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public BpmnConditionalExpression getExpression() {
		return expression;
	}

	public void setExpression(BpmnConditionalExpression expression) {
		this.expression = expression;
	}

}
