package org.pravaha.bpmn.engine;

import lombok.Data;

@Data
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


}
