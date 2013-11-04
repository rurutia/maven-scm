package com.cbs.rest.api.task;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import com.cbs.rest.api.RestResponseFactoryCBS;
import com.cbs.rest.api.task.TaskResponseCBS;

public class TaskResourceDefinitionKey extends SecuredResource {

	@Get
	public TaskResponseCBS getTaskIdByTaskDefinition(Representation entity) {
		if(authenticate() == false) return null;

		String processId = (String) getRequest().getAttributes().get("processId");
		if(processId == null) {
			throw new ActivitiIllegalArgumentException("No process instance id provided");
		}

		String taskDefinitionKey = (String) getRequest().getAttributes().get("taskDefinitionKey");
		if(taskDefinitionKey == null) {
			throw new ActivitiIllegalArgumentException("No task definition key provided");
		}

		Task task = ActivitiUtil.getTaskService()
				.createTaskQuery()
				.processInstanceId(processId)
				.taskDefinitionKey(taskDefinitionKey)
				.singleResult();

		TaskResponseCBS taskResponse;
		
		if(task == null)
			throw new ActivitiIllegalArgumentException("task with definition key:" + taskDefinitionKey + " is not found in process:" + processId);
		else 
		   taskResponse =  new RestResponseFactoryCBS().createTaskReponseComprehensive(this, task);
		
		return taskResponse;
	}

}
