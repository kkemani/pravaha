package org.pravaha.bpmn.configuration;

import java.util.HashMap;

public class BpmnConfigHolder {

	private static BpmnConfigHolder instance;
	private BpmnConfigurationManager bpmnConfigurationManager;
	private HashMap<String, BpmnConfigurationManager> hashmap;

	// Private constructor to prevent instantiation
	private BpmnConfigHolder() {
		try {
			hashmap = new HashMap<>();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Public method to provide access to the instance
	public static synchronized BpmnConfigHolder getInstance() {
		if (instance == null) {
			instance = new BpmnConfigHolder();
		}

		return instance;
	}

	public void setBpmnCfgManager(String processName, BpmnConfigurationManager bpmnConfigurationManager) {
		hashmap.put(processName, bpmnConfigurationManager);
	}

	public BpmnConfigurationManager getBpmnCfgManager(String procConfigFile) {
		BpmnConfigurationManager bpmnCfg = null;
		String processName = null;
		// check db if file is available and get the record
		bpmnCfg = processName != null ? hashmap.get(processName) : null;

		if (bpmnCfg != null)
			return bpmnCfg;
		else {
			bpmnCfg = new BpmnConfigurationManager(procConfigFile);
			bpmnCfg.initializeProcessConfiguration();
			setBpmnCfgManager(bpmnCfg.getProcessName(), bpmnCfg);
			// if isRecordInDB false then save the file vs process name and version
			
		}

		return bpmnCfg;
	}
}
