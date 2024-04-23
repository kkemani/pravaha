package org.pravaha.bpmn.defines;

public enum TaskEnum {
    BPMN_NS("http://www.omg.org/spec/BPMN/20100524/MODEL"),
    BPMN_CAMUNDA_NS("http://camunda.org/schema/1.0/bpmn"),
    BPMN_PROCESS_EL("process"),
    BPMN_SVC_TASK_EL("serviceTask"),
    BPMN_START_EV_EL("startEvent"),
    BPMN_SEQ_FLOW_EL("sequenceFlow"),
    BPMN_END_EV_EL("endEvent"),
    BPMN_EX_GW_EV_EL("exclusiveGateway");

    private final String value;

    TaskEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
