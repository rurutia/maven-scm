package com.cbs.rest.api.task;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableInstanceQuery;
import org.activiti.engine.task.Comment;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

import com.cbs.rest.api.utility.ConstantsCBS;
import com.cbs.rest.api.utility.Utility;

/**
 * 
 * Get single historic task response
 * @link HistoricTaskResponse
 *
 */
public class TaskResourceHistoric extends SecuredResource {

	@Get
	public HistoricTaskResponse getHistoricTaskInstance() throws IOException {
		if(authenticate() == false) return null;

		String taskId = (String) getRequest().getAttributes().get("taskId");

		TaskService taskService = ActivitiUtil.getTaskService();
		HistoricTaskInstance taskInstance = ActivitiUtil.getHistoryService()
				.createHistoricTaskInstanceQuery()
				.taskId(taskId)
				.singleResult();

		HistoricTaskResponse task = new HistoricTaskResponse(taskInstance);
		
		String processDeploymentId = ActivitiUtil.getRepositoryService().createProcessDefinitionQuery()
				.processDefinitionId(task.getProcessDefinitionId()).singleResult().getDeploymentId();
        task.setProcessDeploymentId(processDeploymentId);
        
		HistoricVariableInstanceQuery variableQuery = ActivitiUtil.getHistoryService().createHistoricVariableInstanceQuery();
		List<HistoricVariableInstance> variableList = variableQuery
				.taskId(taskInstance.getId())
				.list();
		for(HistoricVariableInstance variable : variableList) {
			if(ConstantsCBS.VARIABLE_TYPE_ASSIGNMENT_TIME.equals(variable.getVariableName()))
				task.setAssignmentTime((Date)variable.getValue()); 
			if(ConstantsCBS.VARIABLE_TYPE_IN_PROGRESS_TIME.equals(variable.getVariableName()))
				task.setInProgressTime((Date)variable.getValue());
			if(ConstantsCBS.VARIABLE_TYPE_CAN_START_TIME.equals(variable.getVariableName()))
				task.setCanStartTime((Date)variable.getValue());
		}
		    
		List<Comment> comments = taskService.getTaskComments(taskId);
		comments = new Utility<Comment>().reverseList(comments);
		task.setComments(comments);
		task.setVariableMap(getProcessVariablesHistoric(task.getProcessInstanceId())); 
		task.setFormKey(taskInstance.getFormKey());
		return task;
	}

	private Map<String, String> getProcessVariablesHistoric(String processId) {
		Map<String, String> variableMap = new HashMap<String, String>();
		List<HistoricVariableInstance> vars = ActivitiUtil.getHistoryService()
				.createHistoricVariableInstanceQuery()
				.processInstanceId(processId)
				.list();
		for(HistoricVariableInstance var : vars) {
			if(var.getVariableName() != null && var.getValue() !=  null )
				variableMap.put(var.getVariableName(), var.getValue().toString());
		}
		return variableMap;
	}

}
