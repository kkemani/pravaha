package org.pravaha.bpmn.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BpmnUtil {

	public String getBPMNFile(String bpmFileName) {
		String strRef = null;
		try {
			strRef = new String(Files.readAllBytes(Paths.get(bpmFileName)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return strRef;
	}

}
