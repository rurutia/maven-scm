package com.cbs.test.bpmn;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.task.Task;

public class SampleServiceTask implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
//		System.out.println(execution.getId());
//		Thread.sleep(10000);
		Task task = execution.getEngineServices().getTaskService().createTaskQuery().executionId(execution.getParentId()).singleResult();
		
		execution.getEngineServices().getTaskService().createTaskQuery().taskId("taskId").singleResult().setPriority(100);
//		task.setName("foobar");

	}
	

}
