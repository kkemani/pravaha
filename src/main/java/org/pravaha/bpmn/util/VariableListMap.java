package org.pravaha.bpmn.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Map;

import org.pravaha.bpmn.model.ProcessContextVO;

public class VariableListMap {

	public Hashtable<String, Object> getVariableListMap(ProcessContextVO contextVo) {

		Hashtable<String, Object> hashTablemap = new Hashtable<String, Object>();
		try {
			Map<String, String> map = deserializeMap(contextVo.getProcessContext());
			map.forEach((key, value) -> {
				System.out.println("Key: " + key + ", Value: " + value);
				hashTablemap.put(key, value);
			});
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return hashTablemap;

	}
	
	public static Map<String, String> deserializeMap(byte[] byteArray) throws IOException, ClassNotFoundException {
		ByteArrayInputStream byteIn = new ByteArrayInputStream(byteArray);
		ObjectInputStream in = new ObjectInputStream(byteIn);
		return (Map<String, String>) in.readObject();
	}
	
	public byte[] serializeMap(Map<String, Object> map) throws IOException {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos)) {
			oos.writeObject(map);
			return bos.toByteArray();
		}
	}

}
