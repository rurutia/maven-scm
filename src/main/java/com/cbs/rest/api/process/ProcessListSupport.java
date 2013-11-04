package com.cbs.rest.api.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.RestResponseFactory;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.api.engine.RestIdentityLink;
import org.activiti.rest.application.ActivitiRestServicesApplication;

import com.cbs.rest.api.indentity.IdentityLinkCBS;
import com.cbs.rest.api.task.HistoricTaskResponse;
import com.cbs.rest.api.task.TaskResponseCBS;
import com.cbs.rest.api.utility.ConstantsCBS;

public class ProcessListSupport {

	public static HistoricTaskResponse addHistoricTaskVariablesAsAttributes(HistoricTaskInstance taskInstance, List<String> taskVarNames) {
		HistoricTaskResponse historicTaskInstance = new HistoricTaskResponse(taskInstance);
		HistoryService historyService = ActivitiUtil.getHistoryService();

		List<HistoricVariableInstance> taskVariables = historyService.createHistoricVariableInstanceQuery()
				.taskId(taskInstance.getId())
				.list();

		for(HistoricVariableInstance var: taskVariables) {
			if(taskVarNames.contains(var.getVariableName()) && var.getValue() != null) {
				if(ConstantsCBS.VARIABLE_TYPE_ASSIGNMENT_TIME.equals(var.getVariableName())) {
					historicTaskInstance.setAssignmentTime((Date)var.getValue());
				}
				else if(ConstantsCBS.VARIABLE_TYPE_CALLING_PROCESS_TREE.equals(var.getVariableName())) {
					historicTaskInstance.setCallingProcessTreeId((String)var.getValue());
				}
			}
		}
		return historicTaskInstance;
	}

	public static TaskResponseCBS addActiveTaskVariablesAsAttributes(Task taskInstance, List<String> taskVarNames) {
		TaskResponseCBS task = new TaskResponseCBS(taskInstance);
		TaskService taskService = ActivitiUtil.getTaskService();

		Map<String, Object> taskVariables = 
				taskService.getVariablesLocal(taskInstance.getId(), taskVarNames);
		Date taskAssignmentTime = (Date) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_ASSIGNMENT_TIME);
		Date taskInProgressTime = (Date) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_IN_PROGRESS_TIME);
		Date taskCanStartTime = (Date) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_CAN_START_TIME);
		boolean taskIsWaiting = false;
		if( taskVariables.get(ConstantsCBS.VARIABLE_TYPE_TASK_IS_WAITING) != null) {
			taskIsWaiting = (Boolean) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_TASK_IS_WAITING);
		}
		String taskWaitingReason = (String) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_TASK_WAITING_REASON); 
		task.setAssignmentTime(taskAssignmentTime); 
		task.setInProgressTime(taskInProgressTime);
		task.setCanStartTime(taskCanStartTime);
		task.setWaiting(taskIsWaiting);
		task.setWaitingReason(taskWaitingReason);
		return task;
	}

	public static void addActiveTaskIdentityLinks(SecuredResource resource, TaskResponseCBS taskResponse, ProcessInstancesResponseCBS processResponse) {
		List<RestIdentityLink> result = new ArrayList<RestIdentityLink>();
		List<IdentityLink> identityLinks = ActivitiUtil.getTaskService().getIdentityLinksForTask(taskResponse.getId());

		String taskCandidateGroupMap = 
				processResponse.getVariables().get(ConstantsCBS.VARIABLE_TYPE_TASK_CANDIDATE_GROUP_MAP);
		identityLinks = addExtraIdentityLink(identityLinks, taskCandidateGroupMap, taskResponse.getTaskDefinitionKey(),
				taskResponse.getId(), taskResponse.getProcessDefinitionId(), taskResponse.getProcessInstanceId() );

		RestResponseFactory responseFactory = resource.getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
		for(IdentityLink link : identityLinks) {
			result.add(responseFactory.createRestIdentityLink(resource, link));
		}
		taskResponse.setIdentityLinks(result);
	}

	public static List<IdentityLink> addExtraIdentityLink(List<IdentityLink> identityLinks, String taskCandidateGroupMap,
			String taskDefinitionKey, String taskId, String taskDefinitionId, String processInstanceId) {
		if(taskCandidateGroupMap != null) {
			String[] list = taskCandidateGroupMap.split(":");
			if( list[0].equals(taskDefinitionKey) ) {
				String candidateGroup = list[1];
				IdentityLink extraIdentityLink =
						new IdentityLinkCBS(IdentityLinkType.CANDIDATE, null, candidateGroup, taskId, taskDefinitionId, processInstanceId);
				identityLinks.add(extraIdentityLink);
			}
		}
		return identityLinks;
	}



	public static void addToCandidateAssigneeList(String processId, String userId) {
		String candidateAssignees = (String) ActivitiUtil.getRuntimeService().getVariable(processId, ConstantsCBS.VARIABLE_TYPE_CANDIDATE_LIST);
		if(candidateAssignees != null) {
			if(!candidateAssignees.contains(userId))
				candidateAssignees += userId + ConstantsCBS.SEPERATOR_COMMA;
		} else {
			candidateAssignees = userId + ConstantsCBS.SEPERATOR_COMMA;
		}
		ActivitiUtil.getRuntimeService().setVariable(processId, ConstantsCBS.VARIABLE_TYPE_CANDIDATE_LIST, candidateAssignees);
	}

}
