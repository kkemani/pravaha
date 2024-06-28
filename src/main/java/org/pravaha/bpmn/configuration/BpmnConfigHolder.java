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
	
//	public ProcessDefinitionVO getProcessDefinition(String processName) {
//		return this.bpmnProcessDao.getProcessDefinition(processName) != null?this.bpmnProcessDao.getProcessDefinition(processName):null;
//		
//	}
//	
//	private void saveProcessDefinition() {
//		// TODO Auto-generated method stub
//
//		ProcessDefinitionVO vo = new ProcessDefinitionVO();
//		String bpmnProcessDef = bpmnConfigurationManager.getBpmnProcessDefinition();
//		HashMap<String, Object> mapObj = getProcessDetails(bpmnProcessDef);
//		String processName = mapObj.get("Id").toString();
//		String procesVersion = mapObj.get("Version").toString();
//		String processFileName = bpmnConfigurationManager.getBpmFileName();
//		vo.setProcessName(processName);
//		vo.setProcessFileName(getFileName(processFileName));
//		vo.setProcessVersion(procesVersion);
//
//		if (this.bpmnProcessDao != null && vo != null)
//			vo = bpmnProcessDao.saveProcessDefintion(vo);
//		else
//			System.out.println("BPMN Process DAO is null");
//
//	}
//	
//	private HashMap<String, Object> getProcessDetails(String processDefinition) {
//		HashMap<String, Object> map = new HashMap<>();
//		String replacedPD = processDefinition.replaceAll("camunda:versionTag", "version");
//		try {
//			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder builder = factory.newDocumentBuilder();
//			Document document = builder.parse(new InputSource(new StringReader(replacedPD)));
//
//			NodeList processList = document.getElementsByTagName("bpmn:process");
//			if (processList.getLength() > 0) {
//				Element process = (Element) processList.item(0);
//				String id = process.getAttribute("id");
//				String version = process.getAttribute("version");
//				map.put("Id", id);
//
//				if (version != null && !version.isEmpty())
//					map.put("Version", version);
//				else
//					map.put("Version", BpmnProcessEnum.BPMN_PROCESS_INIT_VERSION.getValue());
//			} else {
//				System.err.println("No BPMN process found in the document.");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return map;
//	}
//	
//	private String getFileName(String filepath) {
//		String fileName = filepath.substring(filepath.lastIndexOf("\\") + 1);
//		return fileName;
//	}
}
