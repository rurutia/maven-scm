package com.cbs.rest.api.form;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.engine.variable.RestVariable;
import org.activiti.rest.api.runtime.process.BaseExecutionVariableResource;
import org.apache.commons.io.IOUtils;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import com.cbs.rest.api.RestResponseFactoryCBS;
import com.cbs.rest.api.process.ProcessListSupport;
import com.cbs.rest.api.task.TaskResponseCBS;
import com.cbs.rest.api.utility.ConstantsCBS;

/**
 * 
 * Validate task form user inputs against groovy scripts
 * Complete task if pass the validation
 *
 */
public class ValidateFormPropertiesResource extends BaseExecutionVariableResource {

	@SuppressWarnings("unchecked")
	@Post
	public Object validateFormProperties(Representation representation) {
		if (authenticate() == false) return null;

		// Get "taskId" from Rest URL
		String taskId = getAttribute("taskId");
		if (taskId == null) {
			throw new ActivitiIllegalArgumentException("The taskId cannot be null");
		}

		// Result to be returned in JSON format
		Object result = null;

		TaskService taskService = ActivitiUtil.getTaskService();

		// Form variables to be validated and submitted
		RestVariable[] restVariables;
		try {
			restVariables = getConverterService().toObject(representation, RestVariable[].class, this);
			if(restVariables == null || restVariables.length == 0) {
				throw new ActivitiIllegalArgumentException("Request didn't cantain a list of variables to create.");
			}

			Map<String, Object> properties = new HashMap<String, Object>();

			for(RestVariable variable : restVariables) {
				//				Object actualVariableValue = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
				//						.getVariableValue(variable);

				properties.put(variable.getName(), variable.getValue());
			}

			Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
			ProcessDefinition processDefinition = 
					ActivitiUtil.getRepositoryService().createProcessDefinitionQuery()
					.processDefinitionId(task.getProcessDefinitionId())
					.singleResult();
			InputStream inputStream =
					ActivitiUtil.getRepositoryService()
					.getResourceAsStream(processDefinition.getDeploymentId(), "groovy/form_validation/" + processDefinition.getKey() + "/" + task.getTaskDefinitionKey() + ".groovy");
			String validationScript = IOUtils.toString(inputStream, "UTF-8");

			Map<String, Object> response = new HashMap<String, Object>();
			Map<String, String> errors = new HashMap<String, String>();
			response.put("errorMessages", errors);
			TaskResponseCBS taskResponse =  new RestResponseFactoryCBS().createTaskReponseComprehensive(this, task);
			response.put("task", taskResponse);

			// setup groovy script to validate form properties 
			Binding binding = new Binding();
			// inject processId and taskService used by some task validations (e.g. must be last task in a process to be completed)
			properties.put("processId", task.getProcessInstanceId());
			binding.setVariable("runtimeService", ActivitiUtil.getRuntimeService());
			binding.setVariable("properties", properties);
			binding.setVariable("response", response);

			GroovyShell shell = new GroovyShell(binding);
			Object value = shell.evaluate(validationScript);

			// if groovy validation script returns success, submit task form data
			if((Boolean)((Map<String, Object>)value).get("success")) {
				
				// add current user to process candidate list after completing task
				ProcessListSupport.addToCandidateAssigneeList(taskResponse.getProcessInstanceId(), loggedInUser);

				// remove future timer in Ad-Hoc process if any  
				String cancelTimerExecutionId = (String)taskService.getVariableLocal(taskId,"cancelTimerExecutionId");
				if(cancelTimerExecutionId!=null) {
					ActivitiUtil.getRuntimeService().messageEventReceived(ConstantsCBS.MESSAGE_CANCEL_ADHOC_TIMER, cancelTimerExecutionId);
					taskService.removeVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_PRIORITY_CHANGE_TIME);
					taskService.removeVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_PRIORITY_FUTURE);
				}

				taskService.removeVariableLocal(task.getId(), ConstantsCBS.VARIABLE_TYPE_IN_PROGRESS_TIME);

				// submit form along with variables
				Map<String, String> variables = new HashMap<String, String>();
				for(String key : properties.keySet()) {
					if( properties.get(key) != null && properties.get(key).toString().length() > 0 ) {
						variables.put(key, properties.get(key).toString());
					}
				}

				ActivitiUtil.getFormService().submitTaskFormData(taskId, variables);
				taskResponse.setEndTime(new Date());
			}


			result = value;

			System.out.println("result---" + result);
		} catch (IOException ioe) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, ioe);
		}

		return result;
	}

}
