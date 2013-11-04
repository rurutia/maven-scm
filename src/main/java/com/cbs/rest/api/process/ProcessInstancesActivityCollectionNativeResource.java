package com.cbs.rest.api.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.NativeHistoricProcessInstanceQuery;
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
import com.cbs.rest.api.task.TaskResponseCBS;
import com.cbs.rest.api.utility.ConstantsCBS;

/**
 * list processes with tasks and subprocesses using native query, 
 * process-type can be "in-progress" or "candidate"
 */
public class ProcessInstancesActivityCollectionNativeResource extends SecuredResource {

	Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
	ApplicationContext ctx = 
			new ClassPathXmlApplicationContext("activiti-context.xml");
	ProcessCommentService service = ctx.getBean("processCommentService",ProcessCommentService.class);
	String processType = null;
	
	public ProcessInstancesActivityCollectionNativeResource() {
		properties.put("id", HistoricProcessInstanceQueryProperty.PROCESS_INSTANCE_ID_);
		properties.put("processDefinitionId", HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_ID);
		properties.put("businessKey", HistoricProcessInstanceQueryProperty.BUSINESS_KEY);
		properties.put("startTime", HistoricProcessInstanceQueryProperty.START_TIME);
	}

	@Get
	public DataResponse getProcessInstancesNativeQuery() {
		if(authenticate() == false) return null;
		
		String selectClause = null;
		
	    processType = (String) getRequest().getAttributes().get("process-type");
	    if("in-progress".equals(processType)) {
	    	/*
		     *  query conditions:
		     *  1. Active process
		     *  2. Process has in-progress tasks assigned to current user
		     */
		    selectClause = 
			    	"select * FROM ACT_HI_PROCINST P where END_TIME_ is null and exists (select * from ACT_RU_TASK T inner join ACT_RU_VARIABLE V where T.PROC_INST_ID_=P.PROC_INST_ID_ and T.ID_=V.TASK_ID_ and V.NAME_='inProgressTime' and T.ASSIGNEE_=#{assignee})";	    		
//			selectClause = excludeSubprocesses == null || excludeSubprocesses.equals("false") ? 
//					"select * FROM ACT_HI_PROCINST P where END_TIME_ is null and exists (select * from ACT_RU_TASK T inner join ACT_RU_VARIABLE V where T.PROC_INST_ID_=P.PROC_INST_ID_ and T.ID_=V.TASK_ID_ and V.NAME_='inProgressTime' and T.ASSIGNEE_=#{assignee})"    		
//					: 
//					"select * FROM ACT_HI_PROCINST P where END_TIME_ is null " + 
//					"and SUPER_PROCESS_INSTANCE_ID_ IS NULL " +
//		    		"and exists (select * from ACT_RU_TASK T inner join ACT_RU_VARIABLE V where T.PROC_INST_ID_=P.PROC_INST_ID_ and T.ID_=V.TASK_ID_ and V.NAME_='inProgressTime' and T.ASSIGNEE_=#{assignee})";	    		
	    }
	    else if("candidate".equals(processType)) {
	    	/* 
			 * query conditions:
			 * 1. Active process
			 * 2. Current user in candidate list
			 * 3. Process must have active tasks
			 * 4. If in-progress tasks assigned to current user are not the only tasks in process
			 *	
			 */
			selectClause = 
					"select * FROM ACT_HI_PROCINST P where END_TIME_ is null and P.PROC_INST_ID_ in (select PROC_INST_ID_ from ACT_RU_VARIABLE where NAME_='candidateAssignees' and TEXT_ like '%" + loggedInUser + "%') and P.PROC_INST_ID_ in (select P.ID_ FROM ACT_HI_PROCINST P, ACT_RU_TASK T where P.PROC_INST_ID_=T.PROC_INST_ID_ and P.PROC_INST_ID_ in (select PROC_INST_ID_ from ACT_RU_VARIABLE where END_TIME_ is null and NAME_='candidateAssignees' and TEXT_ like #{candidatePattern}) and T.ID_ not in (select T.ID_ FROM ACT_HI_PROCINST P, ACT_RU_TASK T, ACT_RU_VARIABLE V where T.PROC_INST_ID_=P.PROC_INST_ID_ and T.ID_=V.TASK_ID_ and V.NAME_='inProgressTime' and T.ASSIGNEE_=#{assignee}))";

			//	      selectClause = excludeSubprocesses == null || excludeSubprocesses.equals("false") ?
			//				  "select * FROM ACT_HI_PROCINST P where END_TIME_ is null and P.PROC_INST_ID_ in (select PROC_INST_ID_ from ACT_RU_VARIABLE where NAME_='candidateAssignees' and TEXT_ like '%" + loggedInUser + "%') and P.PROC_INST_ID_ in (select P.ID_ FROM ACT_HI_PROCINST P, ACT_RU_TASK T where P.PROC_INST_ID_=T.PROC_INST_ID_ and P.PROC_INST_ID_ in (select PROC_INST_ID_ from ACT_RU_VARIABLE where END_TIME_ is null and NAME_='candidateAssignees' and TEXT_ like #{candidatePattern}) and T.ID_ not in (select T.ID_ FROM ACT_HI_PROCINST P, ACT_RU_TASK T, ACT_RU_VARIABLE V where T.PROC_INST_ID_=P.PROC_INST_ID_ and T.ID_=V.TASK_ID_ and V.NAME_='inProgressTime' and T.ASSIGNEE_=#{assignee}))"
			//	      		  :
			//				  "select * FROM ACT_HI_PROCINST P where END_TIME_ is null " +
			//		    	  "and SUPER_PROCESS_INSTANCE_ID_ IS NULL " + 
			//				  "and P.PROC_INST_ID_ in (select PROC_INST_ID_ from ACT_RU_VARIABLE where NAME_='candidateAssignees' and TEXT_ like '%" + loggedInUser + "%') and P.PROC_INST_ID_ in (select P.ID_ FROM ACT_HI_PROCINST P, ACT_RU_TASK T where P.PROC_INST_ID_=T.PROC_INST_ID_ and P.PROC_INST_ID_ in (select PROC_INST_ID_ from ACT_RU_VARIABLE where END_TIME_ is null and NAME_='candidateAssignees' and TEXT_ like #{candidatePattern}) and T.ID_ not in (select T.ID_ FROM ACT_HI_PROCINST P, ACT_RU_TASK T, ACT_RU_VARIABLE V where T.PROC_INST_ID_=P.PROC_INST_ID_ and T.ID_=V.TASK_ID_ and V.NAME_='inProgressTime' and T.ASSIGNEE_=#{assignee}))";
	    }
	    
		DataResponse dataResponse = new DataResponse();
		
		selectClause = sortOrder(selectClause, dataResponse);

		NativeHistoricProcessInstanceQuery query = 
				ActivitiUtil.getHistoryService().createNativeHistoricProcessInstanceQuery()
				.sql(selectClause)
				.parameter("candidatePattern", "%" + loggedInUser + "%")
				.parameter("assignee", loggedInUser)
				;

		int start = RequestUtil.getInteger(getQuery(), "start", 0);
		int size = RequestUtil.getInteger(getQuery(), "size", 20);
		List<ProcessInstancesResponseCBS> list = getResponseList(query, start, size);

		dataResponse.setData(list);
		dataResponse.setSize(list.size());
		dataResponse.setStart(start);
		dataResponse.setTotal(query.list().size());

		return dataResponse;
	}

	private String sortOrder(String selectClause, DataResponse response) {
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
		if (order.equals("asc") || order.equals("desc")) 
			selectClause += " order by " + qp.getName() + " " + order;
		else 
			throw new ActivitiIllegalArgumentException("Value for param 'order' is not valid : '" + order + "', must be 'asc' or 'desc'");
		response.setSort(sort);
		response.setOrder(order);
		return selectClause;
	}  

	private List<ProcessInstancesResponseCBS> getResponseList(NativeHistoricProcessInstanceQuery query, int start, int size) {
		List<ProcessInstancesResponseCBS> processResponseList = new ArrayList<ProcessInstancesResponseCBS>();

		for (Object instance : query.listPage(start, size)) {
			HistoricProcessInstance processInstance = (HistoricProcessInstance) instance;
			ProcessInstancesResponseCBS processResponse = new ProcessInstancesResponseCBS(processInstance);

			// set process variables
			processResponse.setVariables(getProcessVariables(processInstance));
			// set active tasks list
			TaskQuery taskQuery = ActivitiUtil.getTaskService().createTaskQuery();
			List<Task> taskList = taskQuery.processInstanceId(processInstance.getId())
					.orderByTaskCreateTime()
					.desc()
					.list();

			List<TaskResponseCBS> taskResponseList = new ArrayList<TaskResponseCBS>();
			for(Task taskInstance : taskList) {
				List<String> taskVarNames = new ArrayList<String>();
				taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_ASSIGNMENT_TIME);
				taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_IN_PROGRESS_TIME);
				taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_CAN_START_TIME);
				taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_TASK_IS_WAITING);
				taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_TASK_WAITING_REASON);
				TaskResponseCBS taskResponse =
						ProcessListSupport.addActiveTaskVariablesAsAttributes(taskInstance, taskVarNames);
				if("in-progress".equals(processType) && taskResponse.getInProgressTime() == null) {
					continue;
				}
				if("candidate".equals(processType) && taskResponse.getInProgressTime() != null 
						&& taskInstance.getAssignee() != null && taskInstance.getAssignee().equals(loggedInUser)) {
					continue;
				}

				// add identity links to task response
				ProcessListSupport.addActiveTaskIdentityLinks(this, taskResponse, processResponse);

				taskResponseList.add(taskResponse);
			}

			processResponse.setActiveTasks(taskResponseList);
			// add process comments total to process instance.
			int total = service.getCountByProcessId(processResponse.getId());
			processResponse.setTotalProcessComments(total);

			// add call activities to processes 
			HistoricActivityInstanceQuery activitiQuery = ActivitiUtil.getHistoryService().createHistoricActivityInstanceQuery();
			List<HistoricActivityInstance> callActivities = activitiQuery.processInstanceId(processInstance.getId()).activityType("callActivity").list();
			processResponse.setSubprocesses(callActivities);

			processResponseList.add(processResponse);		
		}
		return processResponseList;
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