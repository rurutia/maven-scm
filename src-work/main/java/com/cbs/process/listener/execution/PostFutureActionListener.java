package com.cbs.process.listener.execution;


import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

/**
 * 
 * Update priority after timer triggers
 *
 */

public class PostFutureActionListener implements ExecutionListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		System.out.println("-----------PostActionListener execution id:" + execution.getId());
		String targetTaskId = (String)execution.getVariableLocal("targetTaskId");
		org.activiti.engine.task.Task task = execution.getEngineServices().getTaskService()
									.createTaskQuery().taskId(targetTaskId)
									.singleResult();
		if(task != null) {
			System.out.println("----------targetTaskId:" + execution.getVariableLocal("targetTaskId"));
			System.out.println("----------futurePriority:" + execution.getVariableLocal("futurePriority"));
			int priority = (Integer)execution.getVariableLocal("futurePriority");
			task.setPriority(priority);
			org.activiti.engine.TaskService taskService = execution.getEngineServices().getTaskService();
			taskService.removeVariableLocal(targetTaskId, "priorityFuture");
			taskService.removeVariableLocal(targetTaskId, "priorityChangeTime");
			taskService.removeVariableLocal(targetTaskId, "cancelTimerExecutionId");
		}
	}

}
