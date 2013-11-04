package com.cbs.rest.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.RestResponseFactory;
import org.activiti.rest.api.RestUrls;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.api.engine.RestIdentityLink;
import org.activiti.rest.api.engine.variable.RestVariable.RestVariableScope;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.cbs.rest.api.history.HistoricProcessInstanceResponseCBS;
import com.cbs.rest.api.process.ProcessInstancesResponseCBS;
import com.cbs.rest.api.process.ProcessListSupport;
import com.cbs.rest.api.task.HistoricTaskResponse;
import com.cbs.rest.api.task.TaskAction;
import com.cbs.rest.api.task.TaskResponseCBS;
import com.cbs.rest.api.utility.ConstantsCBS;
import com.cbs.rest.api.utility.Utility;

/**
 * 
 * Customized rest response factory to create CBS rest response
 *
 */

public class RestResponseFactoryCBS extends RestResponseFactory {

	public TaskResponseCBS createTaskReponseCBSSimple(SecuredResource securedResource, Task task) {
		TaskResponseCBS response = new TaskResponseCBS(task);
		response.setUrl(securedResource.createFullResourceUrl(RestUrls.URL_TASK, task.getId()));

		// Add references to other resources, if needed
		if (response.getParentTaskId() != null) {
			response.setParentTaskUrl(securedResource.createFullResourceUrl(RestUrls.URL_TASK, response.getParentTaskId()));
		}
		if (response.getProcessDefinitionId() != null) {
			response.setProcessDefinitionUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_DEFINITION, response.getProcessDefinitionId()));
		}
		if (response.getExecutionId() != null) {
			response.setExecutionUrl(securedResource.createFullResourceUrl(RestUrls.URL_EXECUTION, response.getExecutionId()));
		}
		if (response.getProcessInstanceId() != null) {
			response.setProcessInstanceUrl(securedResource.createFullResourceUrl(RestUrls.URL_PROCESS_INSTANCE, response.getProcessInstanceId()));
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	public TaskResponseCBS createTaskReponseComprehensive(SecuredResource securedResource, Task task) {
		TaskService taskService = ActivitiUtil.getTaskService();
		String taskId = task.getId();

		// Collection of task local variables needs to be retrieved
		List<String> taskVarNames = new ArrayList<String>();
		taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_ASSIGNMENT_TIME);
		taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_IN_PROGRESS_TIME);
		taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_CAN_START_TIME);
		taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_IS_DEADLINE);
		taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_PRIORITY_CHANGE_TIME);
		taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_PRIORITY_FUTURE);
		taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_TIMER_COUNT);
		taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_TASK_LOG);
		taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_TASK_IS_WAITING);
		taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_TASK_WAITING_REASON);

		Map<String, Object> taskVariables = taskService.getVariablesLocal(taskId, taskVarNames);

		Date taskInProgressTime = (Date) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_IN_PROGRESS_TIME);
		Date taskAssignmentTime = (Date) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_ASSIGNMENT_TIME);
		Date taskCanStartTime = (Date) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_CAN_START_TIME);
		boolean taskIsDeadline = false;
		if( taskVariables.get(ConstantsCBS.VARIABLE_TYPE_IS_DEADLINE) != null) {
			taskIsDeadline = (Boolean) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_IS_DEADLINE);
		}
		Date taskPriorityChangeTime = (Date) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_PRIORITY_CHANGE_TIME);
		Integer taskPriorityFuture = (Integer) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_PRIORITY_FUTURE);
		Integer taskTimerCount = (Integer) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_TIMER_COUNT);
		boolean taskIsWaiting = false;
		if( taskVariables.get(ConstantsCBS.VARIABLE_TYPE_TASK_IS_WAITING) != null) {
			taskIsWaiting = (Boolean) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_TASK_IS_WAITING);
		}
		String taskWaitingReason = (String) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_TASK_WAITING_REASON); 

		ObjectMapper mapper = new ObjectMapper();
		// "taskLog" is only used to be in compatible with old tasks which use Bytes to store task log
		List<TaskAction> taskLog = (List<TaskAction>) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_TASK_LOG);
		String taskLogJson = (String)taskService.getVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_TASK_LOG_JSON);
		if(taskLogJson != null) {
			try {
				taskLog = mapper.readValue(taskLogJson,new TypeReference<ArrayList<TaskAction>>(){});
			} catch (Exception e) {
				throw new ActivitiException(e.getMessage());
			} 
		}

		TaskResponseCBS taskResponse = new TaskResponseCBS(task);
		taskResponse.setAssignmentTime(taskAssignmentTime); 
		taskResponse.setInProgressTime(taskInProgressTime);
		taskResponse.setCanStartTime(taskCanStartTime);
		taskResponse.setPriorityChangeTime(taskPriorityChangeTime);
		if(taskPriorityFuture != null)
			taskResponse.setPriorityFuture(taskPriorityFuture);
		if(taskTimerCount != null)
			taskResponse.setTimerCount(taskTimerCount);
		taskResponse.setDeadline(taskIsDeadline);
		taskResponse.setLog(taskLog);
		taskResponse.setWaiting(taskIsWaiting);
		taskResponse.setWaitingReason(taskWaitingReason);
		
		String processDeploymentId = ActivitiUtil.getRepositoryService().createProcessDefinitionQuery()
		.processDefinitionId(task.getProcessDefinitionId()).singleResult().getDeploymentId();

		taskResponse.setProcessDeploymentId(processDeploymentId);
		
		List<Comment> comments = taskService.getTaskComments(taskId);
		comments = new Utility<Comment>().reverseList(comments);
		taskResponse.setComments(comments);
		taskResponse.setVariableMap(getProcessVariables(task.getProcessInstanceId())); 

		// add identity links to task response
		List<RestIdentityLink> result = new ArrayList<RestIdentityLink>();
		List<IdentityLink> identityLinks = ActivitiUtil.getTaskService().getIdentityLinksForTask(taskId);

		String taskCandidateGroupMap = 
				taskResponse.getVariableMap().get(ConstantsCBS.VARIABLE_TYPE_TASK_CANDIDATE_GROUP_MAP);
		identityLinks = ProcessListSupport.addExtraIdentityLink(identityLinks, taskCandidateGroupMap, taskResponse.getTaskDefinitionKey(),
				taskResponse.getId(), taskResponse.getProcessDefinitionId(), taskResponse.getProcessInstanceId() );

		for(IdentityLink link : identityLinks) {
			result.add(this.createRestIdentityLink(securedResource, link));
		}
		taskResponse.setIdentityLinks(result);

		return taskResponse;
	}

	public ProcessInstancesResponseCBS createHistoricProcessInstanceResponseCBS(SecuredResource securedResource, HistoricProcessInstance processInstance) {
		ProcessInstancesResponseCBS processResponse = new ProcessInstancesResponseCBS(processInstance);

		HistoricVariableInstance startUserIdVar = ActivitiUtil.getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).variableName("startUserId").singleResult();
		if(startUserIdVar != null)
			processResponse.setStartUserId((String)startUserIdVar.getValue());
		else
			processResponse.setStartUserId(processInstance.getStartUserId());

		Map<String, String> variableMap = new HashMap<String, String>();
		String id = processInstance.getId();
		List<HistoricVariableInstance> vars = ActivitiUtil.getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(id).list();
		for(HistoricVariableInstance var : vars) {
			if(var.getVariableName() != null && var.getValue() !=  null )
				variableMap.put(var.getVariableName(), var.getValue().toString());
		}

		processResponse.setVariables(variableMap);

		addTaskList(processResponse);

		addSubprocessList(processResponse);

		processResponse.setProcessResponseType(ConstantsCBS.PROCESS_RESPONSE_TYPE_SINGLE_HISTORIC);
		return processResponse;
	}

	private void addSubprocessList(ProcessInstancesResponseCBS response) {
		HistoricActivityInstanceQuery activitiQuery = ActivitiUtil.getHistoryService().createHistoricActivityInstanceQuery();
		List<HistoricActivityInstance> callActivities = activitiQuery.processInstanceId(response.getId()).activityType("callActivity").list();
		response.setSubprocesses(callActivities);
	}

	private void addTaskList(ProcessInstancesResponseCBS response) {
		List<HistoricTaskResponse> taskResponseList = new ArrayList<HistoricTaskResponse>(); 
		HistoricTaskInstanceQuery taskQuery = ActivitiUtil.getHistoryService().createHistoricTaskInstanceQuery();
		List<HistoricTaskInstance> taskList = taskQuery.processInstanceId(response.getId())
				.orderByHistoricTaskInstanceStartTime()
				.desc()
				.list();
		for(HistoricTaskInstance taskInstance : taskList) {
			List<String> taskVarNames = new ArrayList<String>();
			taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_ASSIGNMENT_TIME);
			taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_CALLING_PROCESS_TREE);
			HistoricTaskResponse historicTaskInstance = ProcessListSupport.addHistoricTaskVariablesAsAttributes(taskInstance, taskVarNames);

			taskResponseList.add(historicTaskInstance);
		}
		response.setTasks(taskResponseList);
	}

	private Map<String, String> getProcessVariables(String processInstanceId) {
		Map<String, String> variableMap = new HashMap<String, String>();
		RuntimeService rs = ActivitiUtil.getRuntimeService();
		Map<String, Object> variables = rs.getVariables(processInstanceId);
		for( String key : variables.keySet()) {
			String value = ( variables.get(key) == null) ? "null" : variables.get(key).toString();
			if("customerKey".equals(key) && "adhoc".equals(value)) {
				String adhocParentId = (String)rs.getVariable(processInstanceId, ConstantsCBS.ADHOC_PARENT_PROCESS_ID);
				if(adhocParentId != null) {
					value = (String)ActivitiUtil.getHistoryService().createHistoricVariableInstanceQuery()
							.processInstanceId(adhocParentId)
							.variableName("customerKey")
							.singleResult()
							.getValue();
				}
			}
			variableMap.put(key, value);
		}
		return variableMap;
	}




}
