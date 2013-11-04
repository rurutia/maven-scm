package com.cbs.process.sale;

import java.io.Serializable;

import org.activiti.engine.RuntimeService;
import org.activiti.rest.api.ActivitiUtil;

/**
 * Used for "Steps" business rule task
 * Perform business logic at each step 
 *
 */
public class StepController implements Serializable {

	private static final long serialVersionUID = 1L;
	private long step;
	private String processId;
	
	public StepController(String processId) {
		this.processId = processId;
		ActivitiUtil.getRuntimeService().setVariable(this.processId, "step", 0);
		this.step = 0;
	}
	
	public long getStep() {
		return step;
	}
	public void setStep(long step) {
		this.step = step;
		ActivitiUtil.getRuntimeService().setVariable(this.processId, "step", step);
	}
	public String getProcessId() {
		return processId;
	}
	public void setProcessId(String processId) {
		this.processId = processId;
	}
	
	public void setDuration(long duration) {
		RuntimeService rs = ActivitiUtil.getRuntimeService();
		rs.setVariable(this.processId, "duration", duration);
		rs.removeVariable(this.processId, "callbackDateTime");
	}
	
	public void setDiscount(long discount) {
		ActivitiUtil.getRuntimeService().setVariable(this.processId, "discount", discount);
	}
	
	public void setNextTaskName(String nextTaskName) {
		ActivitiUtil.getRuntimeService().setVariable(this.processId, "nextTaskName", nextTaskName);
	}
	
}
