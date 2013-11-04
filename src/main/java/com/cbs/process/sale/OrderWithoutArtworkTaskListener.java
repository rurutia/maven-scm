package com.cbs.process.sale;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import com.cbs.rest.api.utility.ConstantsCBS;

/**
 * Centralized Task initial setup for all tasks in "order without artwork".
 * This approach is deprecated and groovy script should be attached
 * to individual tasks. The class is not removed to support old models.
 *
 */

public class OrderWithoutArtworkTaskListener implements TaskListener {
	
	private static final long serialVersionUID = 1L;

	public void notify(DelegateTask task) {
//		System.out.println("inside order without artwork:" + task.getTaskDefinitionKey());
		task.setDueDate(TaskDueDateSupport.afterHours(22));
		task.setVariableLocal(ConstantsCBS.VARIABLE_TYPE_CAN_START_TIME, task.getCreateTime());
		task.setDescription("");

	}

	
}
