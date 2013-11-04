package com.cbs.process.listener.execution;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

/**
 * 
 * Update priority after timer triggers
 *
 */

public class PreFutureActionListener implements ExecutionListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		System.out.println("-----------PreFutureActionListener execution id:" + execution.getId());
		String processId = execution.getProcessInstanceId();
		String targetTaskId = (String)execution.getEngineServices().getRuntimeService().getVariable(processId, "targetTaskId");
		execution.getEngineServices().getTaskService().setVariableLocal(targetTaskId, "cancelTimerExecutionId", execution.getId());
		execution.setVariableLocal("targetTaskId", targetTaskId);
		execution.setVariableLocal("futurePriority", 
				execution.getEngineServices().getRuntimeService().getVariable(processId, "futurePriority"));
		
	}
}
