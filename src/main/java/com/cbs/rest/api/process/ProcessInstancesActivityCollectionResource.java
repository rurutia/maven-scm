package com.cbs.rest.api.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.AbstractQuery;
import org.activiti.engine.impl.HistoricProcessInstanceQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.RequestUtil;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cbs.persistence.service.ProcessCommentService;
import com.cbs.rest.api.task.HistoricTaskResponse;
import com.cbs.rest.api.task.TaskResponseCBS;
import com.cbs.rest.api.utility.ConstantsCBS;

/**
 * 
 * list CBS customized active or historic processes
 * together with tasks and subprocesses
 *
 */
public class ProcessInstancesActivityCollectionResource extends SecuredResource {

	Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
	ApplicationContext ctx = 
			new ClassPathXmlApplicationContext("activiti-context.xml");
	ProcessCommentService service = ctx.getBean("processCommentService",ProcessCommentService.class);

	public ProcessInstancesActivityCollectionResource() {
		properties.put("id", HistoricProcessInstanceQueryProperty.PROCESS_INSTANCE_ID_);
		properties.put("processDefinitionId", HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_ID);
		properties.put("businessKey", HistoricProcessInstanceQueryProperty.BUSINESS_KEY);
		properties.put("startTime", HistoricProcessInstanceQueryProperty.START_TIME);
	}

	@Get
	public DataResponse getProcessInstances() {
		if(authenticate() == false) return null;
		String finished = getQuery().getValues("finished");
		String watchlist = getQuery().getValues("watchlist");
		String processDefinitionKey = getQuery().getValues("processDefinitionKey");
		String tags = getQuery().getValues("tags");
		String processId = getQuery().getValues("processId");
	    String customerKey = getQuery().getValues("customerKey");
	    String excludeSubprocesses = getQuery().getValues("excludeSubprocesses");

	    HistoricProcessInstanceQuery query = ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery();

    	List<String> excludeAdhocProcess = new ArrayList<String>();
    	excludeAdhocProcess.add(ConstantsCBS.ADHOC_PROCESS_DEFINITION);
    	
	    if(processId != null 
	    	|| (watchlist != null && watchlist.equals("true")) 
	    	|| processDefinitionKey != null ) {
	    	query = finished.equals("true") ? query.finished() : query.unfinished();
	    }
	    else {
	    	if(finished != null) {
		    	if(finished.equals("true")) {
		    		query = query.finished().processDefinitionKeyNotIn(excludeAdhocProcess);
		    	}
		    	else if(finished.equals("false")) {
		    		query = query.unfinished();
		    		// exclude shadow ad-hoc processes
		    		query = query.variableValueNotEquals("customerKey", "adhoc");
		    	}
		    }
		    else {
		    	query = query.processDefinitionKeyNotIn(excludeAdhocProcess);
		    }
	    }

	    query = watchlist != null && watchlist.equals("true") 
	    	 	? query.variableValueLike("watcherList", ("%" + loggedInUser + "%")) : query;

		if(processDefinitionKey != null) {
			query = query.processDefinitionKey(processDefinitionKey);
		}
		
		if(tags != null) {
			for(String tag: tags.split(ConstantsCBS.SEPERATOR_COMMA)) {
				query = query.variableValueLike("tags", "%" + tag + "%");
			}
		}
		
		if(processId != null) {
			query.processInstanceId(processId);
		}
		
		if( customerKey != null && !customerKey.equals("")) {
			query = query.variableValueLike("customerKey", ("%" + customerKey + "%"));
		}
		
		if( excludeSubprocesses != null) {
			query = query.excludeSubprocesses(Boolean.parseBoolean(excludeSubprocesses));
		}

		DataResponse dataResponse = new DataResponse();

		// apply "sort" and "order" parameters to query
		sortOrder(query, dataResponse);

		int start = RequestUtil.getInteger(getQuery(), "start", 0);
		int size = RequestUtil.getInteger(getQuery(), "size", 20);
		List<ProcessInstancesResponseCBS> list;

	    if(finished != null && finished.equals("false"))
	    	list = getResponseList(query, start, size);
	    else 
	    	list = getResponseListHistoric(query, start, size);
		
		dataResponse.setData(list);
		dataResponse.setSize(list.size());
		dataResponse.setStart(start);

		dataResponse.setTotal(query.count());

		return dataResponse;
	}

	@SuppressWarnings("rawtypes")
	private HistoricProcessInstanceQuery sortOrder(HistoricProcessInstanceQuery query, DataResponse response) {
		//		  String defaultSort = "id";
		String defaultSort = "startTime";
		String sort = getQuery().getValues("sort");
		if(sort == null) 
			sort = defaultSort;
		String order = getQuery().getValues("order");
		if(order == null) 
			order = "asc";
		// Sort order
		QueryProperty qp = properties.get(sort);
		if (qp == null) 
			throw new ActivitiIllegalArgumentException("Value for param 'sort' is not valid, '" + sort + "' is not a valid property");
		((AbstractQuery) query).orderBy(qp);
		if (order.equals("asc")) 
			query.asc();
		else if (order.equals("desc")) 
			query.desc();
		else 
			throw new ActivitiIllegalArgumentException("Value for param 'order' is not valid : '" + order + "', must be 'asc' or 'desc'");
		response.setSort(sort);
		response.setOrder(order);
		return query;
	}

	private List<ProcessInstancesResponseCBS> getResponseList(HistoricProcessInstanceQuery query, int start, int size) {
		List<ProcessInstancesResponseCBS> processResponseList = new ArrayList<ProcessInstancesResponseCBS>();
		
		List<HistoricProcessInstance> processes = query.listPage(start, size);
		for (HistoricProcessInstance processInstance : processes) {
			ProcessInstancesResponseCBS processResponse = new ProcessInstancesResponseCBS(processInstance);
			// set process variables
			processResponse.setVariables(getProcessVariables(processInstance));
			// set active tasks list
			List<TaskResponseCBS> taskResponseList = new ArrayList<TaskResponseCBS>();
			populateActiveTaskList(taskResponseList, processInstance.getId(), processResponse);
			
			processResponse.setActiveTasks(taskResponseList);

			// add call activities to processes 
		    HistoricActivityInstanceQuery activitiQuery = ActivitiUtil.getHistoryService().createHistoricActivityInstanceQuery();
		    List<HistoricActivityInstance> callActivities = activitiQuery.processInstanceId(processInstance.getId()).activityType("callActivity").list();
		    
		    processResponse.setSubprocesses(callActivities);
		    
			// add process comments total to process instance.
			int total = service.getCountByProcessId(processResponse.getId());
			processResponse.setTotalProcessComments(total);
			
			// add Ad-Hoc process
			processResponse.setAdhocProcess(getActiveAdhocProcess(processResponse.getId()));
		    
			processResponseList.add(processResponse);		
		}
		
		return processResponseList;
	}
	
	private ProcessInstancesResponseCBS getActiveAdhocProcess(String processId) {
		ProcessInstancesResponseCBS adhocProcessResponse = null;
		String adhocProcessId = (String)ActivitiUtil.getRuntimeService().getVariable(processId, ConstantsCBS.ADHOC_CHILD_PROCESS_ID);
		if(adhocProcessId != null) {
			HistoricProcessInstance instance = ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery()
												.processInstanceId(adhocProcessId)
												.singleResult();
			adhocProcessResponse = new ProcessInstancesResponseCBS(instance);
			// set process variables
			adhocProcessResponse.setVariables(getProcessVariables(instance));
			// set active tasks list
			List<TaskResponseCBS> taskResponseList = new ArrayList<TaskResponseCBS>();
			TaskQuery taskQuery = ActivitiUtil.getTaskService().createTaskQuery();
			List<Task> taskList = taskQuery.processInstanceId(adhocProcessId)
					.orderByTaskCreateTime()
					.desc()
					.list();

			// add task local variables to task as attributes
			for(Task taskInstance : taskList) {
				List<String> taskVarNames = new ArrayList<String>();
				taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_CAN_START_TIME);
				taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_CALLING_PROCESS_TREE);
				TaskResponseCBS task = new TaskResponseCBS(taskInstance);
				TaskService taskService = ActivitiUtil.getTaskService();
				
				Map<String, Object> taskVariables = 
						taskService.getVariablesLocal(taskInstance.getId(), taskVarNames);
			    Date taskCanStartTime = (Date) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_CAN_START_TIME);
			    String taskCallingProcessTreeId = (String) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_CALLING_PROCESS_TREE);
			    task.setCanStartTime(taskCanStartTime);
			    task.setCallingProcessTreeId(taskCallingProcessTreeId);
				
				// add identity links to task response
	    		ProcessListSupport.addActiveTaskIdentityLinks(this, task, adhocProcessResponse);
	    		taskResponseList.add(task);
			}
			adhocProcessResponse.setActiveTasks(taskResponseList);
			
			// add process comments total to process instance.
			int total = service.getCountByProcessId(adhocProcessResponse.getId());
			adhocProcessResponse.setTotalProcessComments(total);
			
			// add call activities to adhoc processe
		    HistoricActivityInstanceQuery activitiQuery = ActivitiUtil.getHistoryService().createHistoricActivityInstanceQuery();
		    List<HistoricActivityInstance> callActivities = activitiQuery.processInstanceId(adhocProcessId).activityType("callActivity").list();
		    adhocProcessResponse.setSubprocesses(callActivities);
		    // adhoc process response needs extra query to retrieve calling process tree info
		    adhocProcessResponse.setProcessResponseType(ConstantsCBS.PROCESS_RESPONSE_TYPE_ADHOC_ACTIVE);
		}
		return adhocProcessResponse;
	}
	
	private void populateActiveTaskList(List<TaskResponseCBS> taskResponseList, String processId, ProcessInstancesResponseCBS processResponse) {
		TaskQuery taskQuery = ActivitiUtil.getTaskService().createTaskQuery();
		List<Task> taskList = taskQuery.processInstanceId(processId)
				.orderByTaskCreateTime()
				.desc()
				.list();

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
	}

	private Map<String, String> getProcessVariables(HistoricProcessInstance processInstance) {
		Map<String, String> variableMap = new HashMap<String, String>();
		String id = processInstance.getId();
		RuntimeService rs = ActivitiUtil.getRuntimeService();
		Map<String, Object> variables = rs.getVariables(id);
		for( String key : variables.keySet()) {
			String value = ( variables.get(key) == null) ? "null" : variables.get(key).toString();
			if("customerKey".equals(key) && "adhoc".equals(value)) {
				String adhocParentId = (String)rs.getVariable(id, ConstantsCBS.ADHOC_PARENT_PROCESS_ID);
				value = (String)rs.getVariable(adhocParentId, "customerKey");
			}
			variableMap.put(key, value);
		}
		return variableMap;
	}
	
	private List<ProcessInstancesResponseCBS> getResponseListHistoric(HistoricProcessInstanceQuery query, int start, int size) {
		List<ProcessInstancesResponseCBS> processResponseList = new ArrayList<ProcessInstancesResponseCBS>();
	    
		List<HistoricProcessInstance> processes = query.listPage(start, size);
		for (HistoricProcessInstance processInstance : processes) {
			ProcessInstancesResponseCBS processResponse = new ProcessInstancesResponseCBS(processInstance);
			// set process variables
			processResponse.setVariables(getProcessVariablesHistoric(processInstance));
			
			// set historic tasks list
	    	List<HistoricTaskResponse> taskResponseList = new ArrayList<HistoricTaskResponse>(); 
	    	populateHistoricTaskList(taskResponseList, processInstance.getId(), processResponse);
	    	
			processResponse.setTasks(taskResponseList);

			// add process comments total to process instance.
			int total = service.getCountByProcessId(processResponse.getId());
			processResponse.setTotalProcessComments(total);
			
			// add call activities to processes 
		    HistoricActivityInstanceQuery activitiQuery = ActivitiUtil.getHistoryService().createHistoricActivityInstanceQuery();
		    List<HistoricActivityInstance> callActivities = activitiQuery.processInstanceId(processInstance.getId()).activityType("callActivity").list();

		    processResponse.setSubprocesses(callActivities);
		    
			// add Ad-Hoc process
			processResponse.setAdhocProcess(getHistoricAdhocProcess(processResponse.getId()));

			processResponseList.add(processResponse);		
		}
		return processResponseList;
	}

	private ProcessInstancesResponseCBS getHistoricAdhocProcess(String processId) {
		ProcessInstancesResponseCBS adhocProcessResponse = null;
		HistoricVariableInstance historicVariable = ActivitiUtil.getHistoryService()
				.createHistoricVariableInstanceQuery()
				.processInstanceId(processId)
				.variableName(ConstantsCBS.ADHOC_CHILD_PROCESS_ID)
				.singleResult();
				
		if(historicVariable != null) {
			String adhocProcessId = (String)historicVariable.getValue();
			HistoricProcessInstance instance = ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery()
												.processInstanceId(adhocProcessId)
												.singleResult();
			adhocProcessResponse = new ProcessInstancesResponseCBS(instance);
			// set process variables
			adhocProcessResponse.setVariables(getProcessVariablesHistoric(instance));
			// set historic tasks list
			List<HistoricTaskResponse> taskResponseList = new ArrayList<HistoricTaskResponse>();
			HistoricTaskInstanceQuery taskQuery = ActivitiUtil.getHistoryService().createHistoricTaskInstanceQuery();
			List<HistoricTaskInstance> taskList = taskQuery.processInstanceId(adhocProcessId)
					.orderByHistoricTaskInstanceStartTime()
					.desc()
					.list();

			// add task local variables to task as attributes
			for(HistoricTaskInstance taskInstance : taskList) {
				List<String> taskVarNames = new ArrayList<String>();
				taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_CALLING_PROCESS_TREE);
				HistoricTaskResponse task = new HistoricTaskResponse(taskInstance);

				historicVariable = ActivitiUtil.getHistoryService()
						.createHistoricVariableInstanceQuery()
						.taskId(task.getId())
						.variableName(ConstantsCBS.VARIABLE_TYPE_CALLING_PROCESS_TREE)
						.singleResult();
				if(historicVariable != null) {
					String taskCallingProcessTreeId = (String)historicVariable.getValue(); 	
					 task.setCallingProcessTreeId(taskCallingProcessTreeId);
				}
	    		taskResponseList.add(task);
			}
			adhocProcessResponse.setTasks(taskResponseList);
			
			// add process comments total to process instance.
			int total = service.getCountByProcessId(adhocProcessResponse.getId());
			adhocProcessResponse.setTotalProcessComments(total);
			
			// add call activities to adhoc processe
		    HistoricActivityInstanceQuery activitiQuery = ActivitiUtil.getHistoryService().createHistoricActivityInstanceQuery();
		    List<HistoricActivityInstance> callActivities = activitiQuery.processInstanceId(adhocProcessId).activityType("callActivity").list();
		    adhocProcessResponse.setSubprocesses(callActivities);
		    // adhoc process response needs extra query to retrieve calling process tree info
		    adhocProcessResponse.setProcessResponseType(ConstantsCBS.PROCESS_RESPONSE_TYPE_ADHOC_HISTORIC);
		}
		return adhocProcessResponse;
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
	
	private void populateHistoricTaskList(List<HistoricTaskResponse> taskResponseList, String processId, ProcessInstancesResponseCBS processResponse) {
	    HistoricTaskInstanceQuery taskQuery = ActivitiUtil.getHistoryService().createHistoricTaskInstanceQuery();
    	List<HistoricTaskInstance> taskList = taskQuery.processInstanceId(processId)
				.orderByHistoricTaskInstanceStartTime()
				.desc()
				.list();
    	for(HistoricTaskInstance taskInstance : taskList) {
    		HistoricTaskResponse historicTaskInstance = new HistoricTaskResponse(taskInstance);
    		taskResponseList.add(historicTaskInstance);
    	}
	}

}

