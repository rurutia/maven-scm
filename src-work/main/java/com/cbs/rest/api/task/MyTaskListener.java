package com.cbs.rest.api.task;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.commons.lang.StringUtils;

import com.cbs.rest.api.utility.ConstantsCBS;

public class MyTaskListener implements TaskListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateTask task) {
		System.out.println("task id is:" + task.getId());
		String cancelTimerExecutionId = (String)task.getVariableLocal("cancelTimerExecutionId");
		System.out.println("cancel timer id:" + cancelTimerExecutionId);

		if(cancelTimerExecutionId != null) {
			task.getExecution().getEngineServices().getRuntimeService()
				.messageEventReceived(ConstantsCBS.MESSAGE_CANCEL_ADHOC_TIMER, cancelTimerExecutionId);
		}
		
	}

}
