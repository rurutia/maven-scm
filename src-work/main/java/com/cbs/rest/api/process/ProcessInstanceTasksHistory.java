package com.cbs.rest.api.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.AbstractQuery;
import org.activiti.engine.impl.HistoricProcessInstanceQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
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

public class ProcessInstanceTasksHistory extends SecuredResource {

	Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
	ApplicationContext ctx = 
			new ClassPathXmlApplicationContext("activiti-context.xml");
	ProcessCommentService service = ctx.getBean("processCommentService",ProcessCommentService.class);

	public ProcessInstanceTasksHistory() {
		properties.put("id", HistoricProcessInstanceQueryProperty.PROCESS_INSTANCE_ID_);
		properties.put("processDefinitionId", HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_ID);
		properties.put("businessKey", HistoricProcessInstanceQueryProperty.BUSINESS_KEY);
		properties.put("startTime", HistoricProcessInstanceQueryProperty.START_TIME);
	}

	@Get
	public DataResponse getProcessInstances() {
		if(authenticate() == false) return null;
	    HistoricProcessInstanceQuery query = ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery();
	    query = query.finished();
	    
	    String processDefinitionKey = getQuery().getValues("processDefinitionKey");
		String tags = getQuery().getValues("tags");
		
		if(processDefinitionKey != null) {
			query = query.processDefinitionKey(processDefinitionKey);
		}
		
		if(tags != null) {
			for(String tag: tags.split(ConstantsCBS.SEPERATOR_COMMA)) {
				query = query.variableValueLike("tags", "%" + tag + "%");
			}
		}

		DataResponse dataResponse = new DataResponse();

		// apply "sort" and "order" parameters to query
		sortOrder(query, dataResponse);

		int start = RequestUtil.getInteger(getQuery(), "start", 0);
		int size = RequestUtil.getInteger(getQuery(), "size", 20);
		List<ProcessInstancesResponseCBS> list = getResponseList(query, start, size);
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
		TaskQuery taskQuery = ActivitiUtil.getTaskService().createTaskQuery();
		
		Map<String, List<String>> processMap = new HashMap<String, List<String>>();
		
		List<ProcessInstancesResponseCBS> openProcessResponseList = new ArrayList<ProcessInstancesResponseCBS>();

//		for(HistoricProcessInstance processInstance: ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery().unfinished().list()){
//			String superProcessId = processInstance.getSuperProcessInstanceId(); 
//			if(superProcessId != null) {
//				List<String> subprocesses = processMap.get(superProcessId);
//				if(subprocesses == null) {
//					subprocesses = new ArrayList<String>();
//				}
//				subprocesses.add(processInstance.getId());
//				processMap.put(superProcessId, subprocesses);
//			}
////			System.out.println("-------------" + processMap);
//			ProcessInstancesResponseCBS processResponse = new ProcessInstancesResponseCBS(processInstance);
//			openProcessResponseList.add(processResponse);
//		}
		
		List<HistoricProcessInstance> processes = query.listPage(start, size);
		for (HistoricProcessInstance processInstance : processes) {
			// set active tasks list
//			List<Task> taskList = taskQuery.processInstanceId(processInstance.getId())
//					.orderByTaskCreateTime()
//					.desc()
//					.list();
//			List<TaskResponseCBS> taskResponseList = new ArrayList<TaskResponseCBS>(); 
//			// add task local variables to task as attributes
//			for(Task taskInstance : taskList) {
//				List<String> taskVarNames = new ArrayList<String>();
//				taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_ASSIGNMENT_TIME);
//				taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_IN_PROGRESS_TIME);
//				taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_CAN_START_TIME);
//				TaskResponseCBS taskResponse =
//						ProcessListSupport.addActiveTaskVariablesAsAttributes(taskInstance, taskVarNames);
//				// add identity links to task response
//	    		ProcessListSupport.addActiveTaskIdentityLinks(this, taskResponse);
//	    		taskResponseList.add(taskResponse);
//			}

			ProcessInstancesResponseCBS processResponse = new ProcessInstancesResponseCBS(processInstance);
//			processResponse.setActiveTasks(taskResponseList);
			// set process variables
			processResponse.setVariables(getProcessVariables(processInstance));
			// add process comments total to process instance.
//			int total = service.getCountByProcessId(processResponse.getId());
//			processResponse.setTotalProcessComments(total);

			processResponseList.add(processResponse);		
		}
		
//		for(ProcessInstancesResponseCBS processResponse : processResponseList) {
//			for(String superProcessId : processMap.keySet()) {
//				if( processResponse.getId().equals(superProcessId) ) {
//					System.out.println("super id:" + processResponse.getId());
//					List<String> subProcessIds = processMap.get(superProcessId);
//					List<ProcessInstancesResponseCBS> subProcessResponseList
//												= getProcessesByIds(subProcessIds, openProcessResponseList);
//					System.out.println(subProcessResponseList);
//					processResponse.setSubprocesses(subProcessResponseList);
//					break;
//				}
//			}
//		}
		
		return processResponseList;
	}
	
//	private List<ProcessInstancesResponseCBS> getProcessesByIds(List<String> processIds, List<ProcessInstancesResponseCBS> processResponseList){
//		List<ProcessInstancesResponseCBS> subProcessResponseList = new ArrayList<ProcessInstancesResponseCBS>();
//		for(ProcessInstancesResponseCBS processResponse : processResponseList) {
//			if(processIds.contains(processResponse.getId())) {
//				subProcessResponseList.add(processResponse);
//			}
//		}
//		return subProcessResponseList;
//	}

//	private Map<String, String> getProcessVariables(HistoricProcessInstance processInstance) {
//		Map<String, String> variableMap = new HashMap<String, String>();
//		String id = processInstance.getId();
//		RuntimeService rs = ActivitiUtil.getRuntimeService();
//		Map<String, Object> variables = rs.getVariables(id);
//		for( String key : variables.keySet()) {
//			String value = ( variables.get(key) == null) ? "null" : variables.get(key).toString();
//			variableMap.put(key, value);
//		}
//		return variableMap;
//	}
	
	private Map<String, String> getProcessVariables(HistoricProcessInstance processInstance) {
		Map<String, String> variableMap = new HashMap<String, String>();
		String id = processInstance.getId();
//		RuntimeService rs = ActivitiUtil.getRuntimeService();
//		Map<String, Object> variables = rs.getVariables(id);
//		for( String key : variables.keySet()) {
//			String value = ( variables.get(key) == null) ? "null" : variables.get(key).toString();
//			variableMap.put(key, value);
//		}
		
		List<HistoricVariableInstance> vars = ActivitiUtil.getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(id).list();
		for(HistoricVariableInstance var : vars) {
			if(var.getVariableName() != null && var.getValue() !=  null )
			variableMap.put(var.getVariableName(), var.getValue().toString());
		}

		return variableMap;
	}

}

