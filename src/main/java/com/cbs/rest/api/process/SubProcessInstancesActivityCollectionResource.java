package com.cbs.rest.api.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cbs.persistence.service.ProcessCommentService;
import com.cbs.rest.api.task.HistoricTaskResponse;
import com.cbs.rest.api.task.TaskResponseCBS;
import com.cbs.rest.api.utility.ConstantsCBS;

public class SubProcessInstancesActivityCollectionResource extends SecuredResource {
	
	ApplicationContext ctx = 
			new ClassPathXmlApplicationContext("activiti-context.xml");
	ProcessCommentService service = ctx.getBean("processCommentService",ProcessCommentService.class);
	
	@Get
	public DataResponse getSubProcessInstances() {
		if(authenticate() == false) return null;

		DataResponse dataResponse = new DataResponse();
		
		String processId = getQuery().getValues("processId");
		
		if(processId == null) {
			throw new ActivitiIllegalArgumentException("No process instance id provided");
		}

		HistoricProcessInstance processInstance = ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery()
													.processInstanceId(processId)
													.singleResult();

		if (processInstance == null) {
			throw new ActivitiObjectNotFoundException("Process instance with id" + processId + " could not be found", ProcessInstance.class);
		}
		
		List<ProcessInstancesResponseCBS> processResponseList = new ArrayList<ProcessInstancesResponseCBS>();
		traverseProcessTree(processId, processResponseList);

		dataResponse.setData(processResponseList);
		dataResponse.setSize(processResponseList.size());
		dataResponse.setStart(0);
		dataResponse.setTotal(processResponseList.size());

		return dataResponse;
	}
	
	private void traverseProcessTree(String superProcessInstanceId, List<ProcessInstancesResponseCBS> processResponseList) {
		List<HistoricProcessInstance> subprocesses = ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery()
																.superProcessInstanceId(superProcessInstanceId)
																.list();
		
		for (HistoricProcessInstance processInstance : subprocesses) {
			String processId = processInstance.getId();
			traverseProcessTree(processId, processResponseList);
			ProcessInstancesResponseCBS processResponse = new ProcessInstancesResponseCBS(processInstance);
			
			if(processInstance.getEndTime() == null)
				setActiveProcessResponse(processInstance, processResponse);
			else
				setHistoricProcessResponse(processInstance, processResponse);
			
			// add process comments total to process instance.
			int total = service.getCountByProcessId(processId);
			processResponse.setTotalProcessComments(total);
			
			// add call activities to processes 
		    HistoricActivityInstanceQuery activitiQuery = ActivitiUtil.getHistoryService().createHistoricActivityInstanceQuery();
		    List<HistoricActivityInstance> callActivities = activitiQuery.processInstanceId(processId).activityType("callActivity").list();
		    processResponse.setSubprocesses(callActivities);
		    
			processResponseList.add(0, processResponse);
		}
	}
	
	private void setActiveProcessResponse(HistoricProcessInstance processInstance, ProcessInstancesResponseCBS processResponse) {
		// set process variables
		processResponse.setVariables(getProcessVariables(processInstance));
		// set active tasks list
		TaskQuery taskQuery = ActivitiUtil.getTaskService().createTaskQuery();
		List<Task> taskList = taskQuery.processInstanceId(processInstance.getId())
				.orderByTaskCreateTime()
				.desc()
				.list();
		
		List<TaskResponseCBS> taskResponseList = new ArrayList<TaskResponseCBS>(); 
		// add task local variables to task as attributes
		for(Task taskInstance : taskList) {
			List<String> taskVarNames = new ArrayList<String>();
			taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_ASSIGNMENT_TIME);
			taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_IN_PROGRESS_TIME);
			taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_CAN_START_TIME);
			taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_TASK_IS_WAITING);
			taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_TASK_WAITING_REASON);
			TaskResponseCBS taskResponse =
					ProcessListSupport.addActiveTaskVariablesAsAttributes(taskInstance, taskVarNames);
			// add identity links to task response
    		ProcessListSupport.addActiveTaskIdentityLinks(this, taskResponse, processResponse);
    		taskResponseList.add(taskResponse);
		}
		
		processResponse.setActiveTasks(taskResponseList);
	}
	
	private void setHistoricProcessResponse(HistoricProcessInstance processInstance, ProcessInstancesResponseCBS processResponse) {
		// set process variables
		processResponse.setVariables(getProcessVariablesHistoric(processInstance));
		// set historic tasks list
	    HistoricTaskInstanceQuery taskQuery = ActivitiUtil.getHistoryService().createHistoricTaskInstanceQuery();
		List<HistoricTaskInstance> taskList = taskQuery.processInstanceId(processInstance.getId())
				.orderByHistoricTaskInstanceStartTime()
				.desc()
				.list();
    	
    	List<HistoricTaskResponse> taskResponseList = new ArrayList<HistoricTaskResponse>(); 
    	for(HistoricTaskInstance taskInstance : taskList) {
    		HistoricTaskResponse historicTaskInstance = new HistoricTaskResponse(taskInstance);
    		taskResponseList.add(historicTaskInstance);
    	}

		// set historic tasks list
		processResponse.setTasks(taskResponseList);
	}


	private Map<String, String> getProcessVariables(HistoricProcessInstance processInstance) {
		Map<String, String> variableMap = new HashMap<String, String>();
		String id = processInstance.getId();
		RuntimeService rs = ActivitiUtil.getRuntimeService();
		Map<String, Object> variables = rs.getVariables(id);
		for( String key : variables.keySet()) {
			String value = ( variables.get(key) == null) ? "null" : variables.get(key).toString();
			variableMap.put(key, value);
		}
		return variableMap;
	}
	
	private Map<String, String> getProcessVariablesHistoric(HistoricProcessInstance processInstance) {
		Map<String, String> variableMap = new HashMap<String, String>();
		String id = processInstance.getId();
		List<HistoricVariableInstance> vars = ActivitiUtil.getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(id).list();
		for(HistoricVariableInstance var : vars) {
			if(var.getVariableName() != null && var.getValue() !=  null )
				variableMap.put(var.getVariableName(), var.getValue().toString());
		}
		return variableMap;
	}
	
}
