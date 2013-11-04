package com.cbs.process.sale;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.rest.api.ActivitiUtil;

import com.cbs.rest.api.task.TaskResponseCBS;
import com.cbs.rest.api.utility.ConstantsCBS;

/**
 * Centralized Task initial setup for all tasks in "order with artwork".
 * This approach is deprecated and groovy script should be attached
 * to individual tasks. The class is not removed to support old models.
 *
 */

public class OrderWithArtworkTaskListener implements TaskListener {
	
	private static final long serialVersionUID = 1L;

	public void notify(DelegateTask task) {
		task.setDueDate(TaskDueDateSupport.afterHours(22));
		task.setVariableLocal(ConstantsCBS.VARIABLE_TYPE_CAN_START_TIME, task.getCreateTime());
	    task.setDescription("");
		
		// for Artwork with invoice process
		if ("send_artwork_invoice".equals(task.getTaskDefinitionKey())) {
			String artworkInvoiceChanges = (String)task.getVariable("artworkInvoiceChanges");
			if(artworkInvoiceChanges != null) 
				task.setDescription(artworkInvoiceChanges);
			task.setVariable("customerRequirement", "");
			task.setVariable("noSalesOrderReason", "");
			task.removeVariable("quoteToSalesOrder");
			task.removeVariable("emailCustomer");
		}
		else if ("artwork_get_payment".equals(task.getTaskDefinitionKey())) {
			task.setVariable("get_payment", true);
		}
		else if ("do_artwork".equals(task.getTaskDefinitionKey())) {
			String artworkChanges = (String)task.getVariable("artworkChanges");
			if(artworkChanges != null) 
				task.setDescription(artworkChanges);
		}
		else if ("forward_artwork".equals(task.getTaskDefinitionKey()) || "followup_artwork".equals(task.getTaskDefinitionKey())) {
			String formKey = ActivitiUtil.getFormService().getTaskFormData(task.getId()).getFormKey();
			task.setVariable("formKey", formKey);
			task.setVariable("forwardArtworkChanges", "");
			task.removeVariable("whatNeedToDo");
			task.removeVariable("isCustomerRequestCallback");		
			task.removeVariable("contactedByCustomer");
			
			Boolean isNextTaskDeadlineSet;	
			if(task.getVariable("isNextTaskDeadlineSet")!=null){
				isNextTaskDeadlineSet=(Boolean)task.getVariable("isNextTaskDeadlineSet");
				task.setVariableLocal("isDeadline", isNextTaskDeadlineSet);
			}
			task.removeVariable("isNextTaskDeadlineSet");

			if( task.getVariable("payment_made") != null ) {
				boolean isPaymentMade = (Boolean)task.getVariable("payment_made");
				if( isPaymentMade ) {
					task.setName("Forward Artwork (Payment Made)");
				}
				else {
					task.setName("Forward Artwork (Payment Not received)");
				}
			}
			
			if(task.getVariable("priorityReadable") != null) 
				task.setPriority(TaskResponseCBS.convertPriorityReadableToNum((String)task.getVariable("priorityReadable")));
			
			String callbackDateTime = (String)task.getVariable("callbackDateTime");
			if( callbackDateTime != null) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
				try {
					Date date = formatter.parse(callbackDateTime);
					task.setDueDate(date);
				} catch (ParseException e) {
					 throw new ActivitiException(e.getMessage());
				}
			}

		}

	}

	
}
