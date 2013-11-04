package com.cbs.test;

import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.identity.Group;
import org.activiti.rest.api.ActivitiUtil;

public class MyExecutionListener implements ExecutionListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		System.out.println("----------getExeuctionId" + execution.getId());
		System.out.println("----------getCurrentActivityId()" + execution.getCurrentActivityId());
		String taskId = (String)execution.getVariable("taskId");
		int priority = (Integer)execution.getVariable("priority");
		execution.setVariableLocal("taskId", taskId);
		execution.setVariableLocal("priority", priority);
	}

}
