package com.cbs.process.listener.task;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import com.cbs.rest.api.utility.ConstantsCBS;

public class TaskCompletionListener implements TaskListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateTask task) {
		System.out.println("task id is:" + task.getId());
		String cancelTimerExecutionId = (String)task.getVariableLocal("cancelTimerExecutionId");
		System.out.println("cancel timer id:" + cancelTimerExecutionId);

		if(cancelTimerExecutionId != null) {
			RuntimeService rs = task.getExecution().getEngineServices().getRuntimeService(); 
			System.out.println("---------------");
			System.out.println("----------" + rs.createExecutionQuery().executionId(cancelTimerExecutionId).singleResult());
			if(null != rs.createExecutionQuery().executionId(cancelTimerExecutionId).singleResult()){
				rs.messageEventReceived(ConstantsCBS.MESSAGE_CANCEL_ADHOC_TIMER, cancelTimerExecutionId);
			}
		}
		
	}

}

