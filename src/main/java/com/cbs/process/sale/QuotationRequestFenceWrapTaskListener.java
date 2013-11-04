package com.cbs.process.sale;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import com.cbs.rest.api.task.TaskResponseCBS;
import com.cbs.rest.api.utility.ConstantsCBS;

/**
 * Centralized Task initial setup for all tasks in "quotation request process fence wrap".
 * This approach is deprecated and groovy script should be attached
 * to individual tasks. The class is not removed to support old models.
 *
 */

public class QuotationRequestFenceWrapTaskListener implements TaskListener {
	
	private static final long serialVersionUID = 1L;

	public void notify(DelegateTask task) {
//		System.out.println(task.getTaskDefinitionKey());
		task.setDueDate(TaskDueDateSupport.afterHours(22));
		task.setVariableLocal(ConstantsCBS.VARIABLE_TYPE_CAN_START_TIME, task.getCreateTime());

		task.setDescription("");
		if("quotation_request_supply_quotation".equals(task.getTaskDefinitionKey())) {
			task.setVariableLocal(ConstantsCBS.VARIABLE_TYPE_TIMER_COUNT, 1);
			task.setVariableLocal(ConstantsCBS.VARIABLE_TYPE_PRIORITY_FUTURE, 75);
			
			task.removeVariable("supplyQuotationIsEmailSent");
			task.setVariable("supplyQuotationReasonEmailNotSent", "");
			task.setVariable("discount", new Long(0));
			StepController stepController = new StepController(task.getProcessInstanceId());
			task.setVariable("stepController", stepController);
		}
		else if ("quotation_request_follow_up".equals(task.getTaskDefinitionKey())) {
			task.setVariableLocal(ConstantsCBS.VARIABLE_TYPE_TIMER_COUNT, 1);
			task.setVariableLocal(ConstantsCBS.VARIABLE_TYPE_PRIORITY_FUTURE, 75);
		
			if(task.getVariable("priorityReadable") != null) 
				task.setPriority(TaskResponseCBS.convertPriorityReadableToNum((String)task.getVariable("priorityReadable")));
			task.removeVariable("contactedByCustomer");
			task.removeVariable("callSuccessful");
			task.removeVariable("sendEmail");
			task.removeVariable("followUpOutcome");
			task.setVariable("notSendEmailReason", "");
			Boolean isNextTaskDeadlineSet;	
			if(task.getVariable("isNextTaskDeadlineSet")!=null){
				isNextTaskDeadlineSet=(Boolean)task.getVariable("isNextTaskDeadlineSet");
				task.setVariableLocal("isDeadline", isNextTaskDeadlineSet);
			}
			task.removeVariable("isNextTaskDeadlineSet");
			
			String callbackDateTime = (String)task.getVariable("callbackDateTime");
			if( callbackDateTime != null && !callbackDateTime.equals("")) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
				try {
					Date date = formatter.parse(callbackDateTime);
					task.setDueDate(date);
					task.setVariable("callbackDateTime", null);
				} catch (ParseException e) {
					 throw new ActivitiException(e.getMessage());
				}
			}
			else {
				long duration = (Long) task.getVariable("duration");
				task.setDueDate(TaskDueDateSupport.afterHours((int)duration));
			}
		}
		else if ("quotation-process-set-callback-time".equals(task.getTaskDefinitionKey())) {
			// Default HIGH priority for next "Follow-Up" task
//			task.setVariable("priorityReadable", "HIGH");
		}
		
	}

	
}
