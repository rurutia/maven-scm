package com.cbs.test;

import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.identity.Group;
import org.activiti.rest.api.ActivitiUtil;

public class ChangeTaskExecutionListener implements ExecutionListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		System.out.println("after----------getExeuctionId" + execution.getId());
		System.out.println("after----------getCurrentActivityId()" + execution.getCurrentActivityId());
		String taskId = (String)execution.getVariableLocal("taskId");
		int priority = (Integer)execution.getVariableLocal("priority");
		execution.getEngineServices().getTaskService().createTaskQuery()
		.taskId(taskId).singleResult().setPriority(priority);
	}

}
