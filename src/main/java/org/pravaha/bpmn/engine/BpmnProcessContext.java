 package org.pravaha.bpmn.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Element;
import org.pravaha.bpmn.model.ProcessContextVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BpmnProcessContext {
	final static Logger logger = LoggerFactory.getLogger("BpmnProcessContext");

	protected HashMap<String, Object> procVariables = null;
	protected List<String> transientVars = null;
	protected HashMap<String, Object> transientVariables = null;
	protected String pid = null;
	protected Date createDate = null;
	protected Date lastUpdateDate = null;

	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	Element commitNode = null;

	public BpmnProcessContext() {
		super();
		procVariables = new HashMap<String, Object>();
		transientVars = new ArrayList<>();
		transientVariables = new HashMap<>();
		this.createDate = Calendar.getInstance().getTime();
	}

	public BpmnProcessContext(Element commitVar) {
		this.commitNode = commitVar;
	}

	public void initializeVariables(HashMap<String, Object> vars) {
		this.procVariables = vars;
	}

	public void assignVariables(HashMap<String, Object> vars) {
		// iterate through the hashmap passed and set the proc Variables map
		for (String oneKey : vars.keySet()) {
			Object oneObj = vars.get(oneKey);
		//	System.out.println(" Adding variable:" + oneKey + "::" + oneObj);
			addVariable(oneKey, oneObj);
		}
	}

	public HashMap<String, Object> getprocVariables() {
		return procVariables;
	}

	public void addVariable(String varName, Object varValue) {
		this.procVariables.put(varName, varValue);
	}

	public void addTransientVariable(String varName, Object varValue) {
		this.procVariables.put(varName, varValue);
		this.transientVars.add(varName);
	}

	public void updateVariable(String retVariable, Object retValue) {
		// TODO Auto-generated method stub
		Object curValue = this.procVariables.get(retVariable);
		//smxLogger.INFO(pid, "SMXProcessContext:updateVariable::Updating variable" + retVariable + " from :" + curValue
		//		+ ": to :" + retValue);
		logger.debug(pid, "SMXProcessContext:updateVariable::Updating variable" + retVariable + " from :" + curValue
				+ ": to :" + retValue,false);
		addVariable(retVariable, retValue);
	}

	public Object getVariable(String argName) {
		// TODO Auto-generated method stub
		return this.procVariables.get(argName);

	}

	public static BpmnProcessContext buildProcessContextVO(ProcessContextVO processContextVO) {
		BpmnProcessContext bpmnProcessContext = new BpmnProcessContext();
		bpmnProcessContext.setPid(processContextVO.getProcessId());
		bpmnProcessContext.setCreateDate(processContextVO.getCreateDate());
		bpmnProcessContext.setLastUpdateDate(processContextVO.getLastUpdateDate());

		try {

			ByteArrayInputStream bais = new ByteArrayInputStream(processContextVO.getProcessContext());

			ObjectInputStream oip = new ObjectInputStream(bais);
			Object object = oip.readObject();
			HashMap tempGsaMap = (HashMap) object;
			if (tempGsaMap != null)
				bpmnProcessContext.initializeVariables(tempGsaMap);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return bpmnProcessContext;
	}

	public void updateProcessContextVO(ProcessContextVO contextVO) {
		try {
			ByteArrayOutputStream boas = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(boas);
			oos.writeObject(this.procVariables);
			oos.flush();
			oos.close();
			contextVO.setProcessContext(boas.toByteArray());
			contextVO.setLastUpdateDate(Calendar.getInstance().getTime());
		} catch (Exception ex) {
			ex.printStackTrace();

		}

		return;
	}

	public ProcessContextVO getProcessContextVO() {
		ProcessContextVO processContextVO = new ProcessContextVO();
		processContextVO.setProcessId(this.pid);
		processContextVO.setCreateDate(this.createDate);
		processContextVO.setLastUpdateDate(Calendar.getInstance().getTime());

		try {
			ByteArrayOutputStream boas = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(boas);
			oos.writeObject(this.procVariables);
			oos.flush();
			oos.close();
			processContextVO.setProcessContext(boas.toByteArray());
		} catch (Exception ex) {
			ex.printStackTrace();

		}

		return processContextVO;
	}

	public String toString() {
		StringBuffer strbuffer = new StringBuffer();
		Iterator itr = getprocVariables().keySet().iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			Object Objvalue = getprocVariables().get(key);
			String value = null;

			if (Objvalue != null) {
				if (Objvalue instanceof String) {
					value = (String) Objvalue;

					if (value != null && value.length() > 255)
						value = value.substring(0, 255) + " :: TRUNCATED to 255 chars";
				} else
					value = Objvalue.toString();
			} else {
				System.out.println("BpmnProcessContext :: toString()" + pid + " key=" + key + " :: Objvalue is null.");
			}
			strbuffer.append("|Key:" + key).append("::Value = " + value);
		}
		return strbuffer.toString();

	}

	public void clearTransientVariables() {
		if (this.transientVars != null) {
			this.transientVars.forEach(x -> {
				Object obj = this.transientVariables.get(x);
				if(obj==null)
					this.procVariables.put(x, "TS");
				else
					this.procVariables.put(x,  obj);
			});
		}
	}

	public void setTransientVariables(HashMap<String, Object> transVars) {
		this.transientVariables.putAll(transVars);
	}

}

