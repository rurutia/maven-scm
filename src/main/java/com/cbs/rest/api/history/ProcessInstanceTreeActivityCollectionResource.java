package com.cbs.rest.api.history;

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
import com.cbs.rest.api.process.ProcessInstancesResponseCBS;
import com.cbs.rest.api.process.ProcessListSupport;
import com.cbs.rest.api.task.HistoricTaskResponse;
import com.cbs.rest.api.task.TaskResponseCBS;
import com.cbs.rest.api.utility.ConstantsCBS;

/**
 * Providing one process id in the tree,
 * Retrieve full process tree with tasks and call activities(subprocess)
 *  
 */
public class ProcessInstanceTreeActivityCollectionResource extends SecuredResource {
	
	ApplicationContext ctx = 
			new ClassPathXmlApplicationContext("activiti-context.xml");
	ProcessCommentService service = ctx.getBean("processCommentService",ProcessCommentService.class);
	
	@Get
	public List<ProcessInstancesResponseCBS> ProcessInstanceTreeActivityCollection() {
		if(authenticate() == false) return null;
		
		String processId = (String) getRequest().getAttributes().get("processInstanceId");
		
		if(processId == null) {
			throw new ActivitiIllegalArgumentException("No process instance id is provided");
		}

		
		HistoricProcessInstance processInstance = ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery()
													.processInstanceId(processId)
													.singleResult();
		
		if (processInstance == null) {
			throw new ActivitiObjectNotFoundException("Process instance with id" + processId + " could not be found", ProcessInstance.class);
		}
		
		while(processInstance.getSuperProcessInstanceId() != null) {
			processId = processInstance.getSuperProcessInstanceId();
			processInstance = (HistoricProcessInstance)ActivitiUtil.getHistoryService()
					.createHistoricProcessInstanceQuery()
					.processInstanceId(processId)
					.singleResult();
		}

		
		List<ProcessInstancesResponseCBS> processResponseList = new ArrayList<ProcessInstancesResponseCBS>();
		
		traverseProcessTree(processId, processResponseList);
		
		ProcessInstancesResponseCBS processResponse = new ProcessInstancesResponseCBS(processInstance);
	    setHistoricProcessResponse(processInstance, processResponse);
		processResponseList.add(0, processResponse);

		return processResponseList;
	}
	
	private void traverseProcessTree(String superProcessInstanceId, List<ProcessInstancesResponseCBS> processResponseList) {
		List<HistoricProcessInstance> subprocesses = ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery()
																.superProcessInstanceId(superProcessInstanceId)
																.list();
		
		for (HistoricProcessInstance processInstance : subprocesses) {
			String processId = processInstance.getId();
			traverseProcessTree(processId, processResponseList);
			ProcessInstancesResponseCBS processResponse = new ProcessInstancesResponseCBS(processInstance);
			
		    setHistoricProcessResponse(processInstance, processResponse);
			
			processResponseList.add(0, processResponse);
		}
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
    		List<String> taskVarNames = new ArrayList<String>();
			taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_ASSIGNMENT_TIME);
			taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_CALLING_PROCESS_TREE);
			HistoricTaskResponse historicTaskInstance = ProcessListSupport.addHistoricTaskVariablesAsAttributes(taskInstance, taskVarNames);

    		taskResponseList.add(historicTaskInstance);
    	}

		// set historic tasks list
		processResponse.setTasks(taskResponseList);
		
		// add call activities to processes 
	    HistoricActivityInstanceQuery activitiQuery = ActivitiUtil.getHistoryService().createHistoricActivityInstanceQuery();
	    List<HistoricActivityInstance> callActivities = activitiQuery.processInstanceId(processInstance.getId()).activityType("callActivity").list();
	    processResponse.setSubprocesses(callActivities);
	    
		processResponse.setProcessResponseType(ConstantsCBS.PROCESS_RESPONSE_TYPE_SINGLE_HISTORIC);
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
