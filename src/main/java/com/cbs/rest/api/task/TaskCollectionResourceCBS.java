package com.cbs.rest.api.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.NativeTaskQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.RequestUtil;
import org.activiti.rest.api.RestResponseFactory;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.api.engine.RestIdentityLink;
import org.activiti.rest.application.ActivitiRestServicesApplication;
import org.apache.commons.lang.WordUtils;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.restlet.data.Form;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cbs.persistence.service.ProcessCommentService;
import com.cbs.rest.api.RestResponseFactoryCBS;
import com.cbs.rest.api.indentity.IdentityLinkCBS;
import com.cbs.rest.api.process.ProcessListSupport;
import com.cbs.rest.api.utility.ConstantsCBS;

/**
 * List of active tasks which can be
 * sorted by:
 * "priority", "due date"
 * filter by:
 * "assignee", "task name keyword", "min & max can start dates" , "waiting"
 * 
 */
public class TaskCollectionResourceCBS extends SecuredResource {
	
  protected static final Logger LOGGER = LoggerFactory.getLogger(TaskCollectionResourceCBS.class); 
  
  private long total_overdue;
  private int page_overdue = 0;
  
  ApplicationContext ctx = 
			new ClassPathXmlApplicationContext("activiti-context.xml");
  ProcessCommentService service = ctx.getBean("processCommentService",ProcessCommentService.class);

  TaskService taskService = ActivitiUtil.getTaskService();
  
  @Get
  public DataResponseTasks getTasks() {
	long m1 = System.currentTimeMillis();
    
	if(authenticate() == false) return null;

	// get query parameters
	Form query = getQuery();
	// read query terms from parameters
    String assignee = query.getValues("assignee");
    
//    String strMinDueDate = query.getValues("minDueDate");
//    String strMaxDueDate = query.getValues("maxDueDate");
    
    // GMT Epoch milliseconds
    String strMinStartDate = query.getValues("minStartDate");
    String strMaxStartDate = query.getValues("maxStartDate");

    String strSortPriority = query.getValues("sortPriority");
    String strSortDueDate = query.getValues("sortDueDate");
    String strTaskNameKeyword = query.getValues("taskNameKeyword");
    strTaskNameKeyword = (strTaskNameKeyword != null) && strTaskNameKeyword.equals("") ? null : strTaskNameKeyword;
    
    TaskQuery taskQuery = taskService.createTaskQuery();
    taskQuery = taskQuery.dueBefore(new Date());
    if(assignee != null) {
    	if(!assignee.equals("all") && !assignee.equals("unassigned"))
    		taskQuery = taskQuery.taskAssignee(assignee);
    	if(!assignee.equals("all") && assignee.equals("unassigned"))
    		taskQuery = taskQuery.taskUnassigned();
    }
    total_overdue = taskQuery.count();
    
    String outerWhereClause = getQueryWhereClause(query);
    
    DataResponseTasks dataResponse = new DataResponseTasks();
    
    taskQuery = taskService.createTaskQuery();
    
    String selectClause = null;
    
    if (strSortPriority != null && strSortPriority.equals("asc") ){
    	selectClause = "SELECT T.*,V.LONG_ AS `CANSTART`,V2.TEXT_ AS `TAGS` from ACT_RU_TASK T LEFT JOIN ACT_RU_VARIABLE V ON V.NAME_='canStartTime' and V.TASK_ID_=T.ID_ LEFT JOIN ACT_RU_VARIABLE V2 ON V2.NAME_='tags' and V2.PROC_INST_ID_=T.PROC_INST_ID_" + outerWhereClause + " ORDER BY PRIORITY_ ASC, DUE_DATE_ ASC";
//    	selectClause = "SELECT T.*,V.LONG_ AS `CANSTART`,V2.TEXT_ AS `TAGS`,V3.LONG_ AS `WAITING` from ACT_RU_TASK T LEFT JOIN ACT_RU_VARIABLE V ON V.NAME_='canStartTime' and V.TASK_ID_=T.ID_ LEFT JOIN ACT_RU_VARIABLE V2 ON V2.NAME_='tags' and V2.PROC_INST_ID_=T.PROC_INST_ID_ LEFT JOIN ACT_RU_VARIABLE V3 ON V3.NAME_='waiting' and V3.TASK_ID_=T.ID_ " + outerWhereClause + " ORDER BY PRIORITY_ ASC, DUE_DATE_ ASC";
    }
    if (strSortPriority != null && strSortPriority.equals("desc") ){
    	selectClause = "SELECT T.*,V.LONG_ AS `CANSTART`,V2.TEXT_ AS `TAGS` from ACT_RU_TASK T LEFT JOIN ACT_RU_VARIABLE V ON V.NAME_='canStartTime' and V.TASK_ID_=T.ID_ LEFT JOIN ACT_RU_VARIABLE V2 ON V2.NAME_='tags' and V2.PROC_INST_ID_=T.PROC_INST_ID_" + outerWhereClause + " ORDER BY PRIORITY_ DESC, DUE_DATE_ ASC";
//    	selectClause = "SELECT T.*,V.LONG_ AS `CANSTART`,V2.TEXT_ AS `TAGS`,V3.LONG_ AS `WAITING` from ACT_RU_TASK T LEFT JOIN ACT_RU_VARIABLE V ON V.NAME_='canStartTime' and V.TASK_ID_=T.ID_ LEFT JOIN ACT_RU_VARIABLE V2 ON V2.NAME_='tags' and V2.PROC_INST_ID_=T.PROC_INST_ID_ LEFT JOIN ACT_RU_VARIABLE V3 ON V3.NAME_='waiting' and V3.TASK_ID_=T.ID_ " + outerWhereClause + " ORDER BY PRIORITY_ DESC, DUE_DATE_ ASC";
    }
    
	if(selectClause == null) {
		selectClause  = 
//			  "SELECT * FROM (SELECT * FROM (SELECT T.* ,V.LONG_ AS `CANSTART`,V2.TEXT_ AS `TAGS`,V3.LONG_ AS `WAITING` FROM ACT_RU_TASK T LEFT JOIN ACT_RU_VARIABLE V ON V.NAME_='canStartTime' and V.TASK_ID_=T.ID_ LEFT JOIN ACT_RU_VARIABLE V2 ON V2.NAME_='tags' and V2.PROC_INST_ID_=T.PROC_INST_ID_ LEFT JOIN ACT_RU_VARIABLE V3 ON V3.NAME_='waiting' and V3.TASK_ID_=T.ID_ where PRIORITY_ = 100 ORDER BY DUE_DATE_ DESC) AS A UNION ALL SELECT * FROM (SELECT T.* ,V.LONG_ AS `CANSTART`,V2.TEXT_ AS `TAGS`,V3.LONG_ AS `WAITING` FROM ACT_RU_TASK T LEFT JOIN ACT_RU_VARIABLE V ON V.NAME_='canStartTime' and V.TASK_ID_=T.ID_ LEFT JOIN ACT_RU_VARIABLE V2 ON V2.NAME_='tags' and V2.PROC_INST_ID_=T.PROC_INST_ID_ LEFT JOIN ACT_RU_VARIABLE V3 ON V3.NAME_='waiting' and V3.TASK_ID_=T.ID_ WHERE PRIORITY_ < 100 ORDER BY PRIORITY_ DESC, DUE_DATE_ ASC ) AS B) T";		
		      "SELECT * FROM (SELECT * FROM (SELECT T.* ,V.LONG_ AS `CANSTART`,V2.TEXT_ AS `TAGS` FROM ACT_RU_TASK T LEFT JOIN ACT_RU_VARIABLE V ON V.NAME_='canStartTime' and V.TASK_ID_=T.ID_ LEFT JOIN ACT_RU_VARIABLE V2 ON V2.NAME_='tags' and V2.PROC_INST_ID_=T.PROC_INST_ID_ where PRIORITY_ = 100 ORDER BY DUE_DATE_ DESC) AS A UNION ALL SELECT * FROM (SELECT T.* ,V.LONG_ AS `CANSTART`,V2.TEXT_ AS `TAGS` FROM ACT_RU_TASK T LEFT JOIN ACT_RU_VARIABLE V ON V.NAME_='canStartTime' and V.TASK_ID_=T.ID_ LEFT JOIN ACT_RU_VARIABLE V2 ON V2.NAME_='tags' and V2.PROC_INST_ID_=T.PROC_INST_ID_ WHERE PRIORITY_ < 100 ORDER BY PRIORITY_ DESC, DUE_DATE_ ASC ) AS B) T";		

	    if (strSortDueDate != null ) {
	        selectClause = selectClause.replaceAll("ORDER BY DUE_DATE_ ASC", "ORDER BY DUE_DATE_ " + strSortDueDate.toUpperCase());
	        selectClause = selectClause.replaceAll("ORDER BY PRIORITY_ DESC, DUE_DATE_ ASC", "ORDER BY DUE_DATE_ " + strSortDueDate.toUpperCase() + ", PRIORITY_ DESC");
		}
	    selectClause += outerWhereClause;
	}
    
	LOGGER.info(selectClause);
	
    NativeTaskQuery nativeQuery 
    	= taskService.createNativeTaskQuery()
    								   .sql(selectClause);	
    
//    if (strMinDueDate != null && strMaxDueDate != null) {
//    	nativeQuery = nativeQuery
//						.parameter("minDueDate", getQueryParameterAsDate("minDueDate", query))
//						.parameter("maxDueDate", getQueryParameterAsDate("maxDueDate", query));
//	}
    if (strMinStartDate != null && strMaxStartDate != null) {
    	nativeQuery = nativeQuery
						.parameter("minStartTime", strMinStartDate)
						.parameter("maxStartTime", strMaxStartDate);
	}
    
    dataResponse = executeQuery(nativeQuery, dataResponse);
    
    long m2 = System.currentTimeMillis();
    LOGGER.info("--------loading task list time in milliseconds------" + (m2 - m1));

    return dataResponse;
  }
  
  private String getQueryWhereClause(Form form) {
	  String assignee = form.getValues("assignee");
	  String strPriorityReadable = form.getValues("priorityReadable");

//	  String strMinDueDate = form.getValues("minDueDate");
//	  String strMaxDueDate = form.getValues("maxDueDate");
	  
	  String strSortPriority = form.getValues("sortPriority");
	  
	  String strMinStartDate = form.getValues("minStartDate");
	  String strMaxStartDate = form.getValues("maxStartDate");

//	  System.out.println("min start date:" + strMinStartDate);
//	  System.out.println("max start date:" + strMaxStartDate);

	  String strTaskNameKeyword = form.getValues("taskNameKeyword");
	  strTaskNameKeyword = (strTaskNameKeyword != null) && strTaskNameKeyword.equals("") ? null : strTaskNameKeyword;

	  String tags = form.getValues("tags");
	  
	  String waiting = form.getValues("waiting");
	  
	  int priority = -1;

	  if (strPriorityReadable != null) {
		  priority = TaskResponseCBS.convertPriorityReadableToNum(strPriorityReadable);
	  }

	  StringBuffer outerWhereClause = new StringBuffer("");
	  List<String> outerWhereConditions = new ArrayList<String>();

	  if(assignee != null && assignee.equals("unassigned")) {
		  outerWhereConditions.add("ASSIGNEE_ IS NULL");
	  }
	  if(assignee != null && !assignee.equals("unassigned") && !assignee.equals("all") ) {
		  outerWhereConditions.add("ASSIGNEE_ = '" + assignee + "'");
	  }
	  if(priority != -1) {
		  outerWhereConditions.add("PRIORITY_ =" + priority); 
	  }
	  if(strTaskNameKeyword != null) {
		  outerWhereConditions.add("T.NAME_ COLLATE UTF8_GENERAL_CI LIKE '%" + strTaskNameKeyword + "%'"); 
	  }
//	  if (strMinDueDate != null && strMaxDueDate != null) {
//		  outerWhereConditions.add("DUE_DATE_ > #{minDueDate} AND DUE_DATE_ < #{maxDueDate}");
//	  }
	  
	  if(strSortPriority == null && tags != null) {
		  for(String tag: tags.split(ConstantsCBS.SEPERATOR_COMMA)) {
			  outerWhereConditions.add("TAGS like " + "\"%" + tag + "%\"");
		  }
	  }
	  
	  if(strSortPriority != null && tags != null) {
		  for(String tag: tags.split(ConstantsCBS.SEPERATOR_COMMA)) {
			  outerWhereConditions.add("V2.TEXT_ like " + "\"%" + tag + "%\"");
		  }
	  }
	  
	  if (strSortPriority == null && strMinStartDate != null && strMaxStartDate != null) {
		  outerWhereConditions.add("(CANSTART between #{minStartTime} AND #{maxStartTime} OR CANSTART IS NULL)");
	  }
	  if (strSortPriority != null && strMinStartDate != null && strMaxStartDate != null) {
		  outerWhereConditions.add("(V.LONG_ between #{minStartTime} AND #{maxStartTime} OR V.LONG_ IS NULL)");
	  }
	  
	  if (strSortPriority != null && waiting != null && waiting.equals("false")) {
		  outerWhereConditions.add("(V3.LONG_=0 OR V3.LONG_ IS NULL)");
	  }
	  
	  if (strSortPriority == null && waiting != null && waiting.equals("false")) {
		  outerWhereConditions.add("(WAITING=0 OR WAITING IS NULL)");
	  }

	  if(outerWhereConditions.size() > 0) {
		  outerWhereClause.append(" WHERE " + outerWhereConditions.get(0));
		  for(int i = 1; i < outerWhereConditions.size(); i++) {
			  outerWhereClause.append(" AND " + outerWhereConditions.get(i));
		  }
	  }
	  return outerWhereClause.toString();
  }
  
  private DataResponseTasks executeQuery(NativeTaskQuery query, DataResponseTasks dataResponse) {
    int start = RequestUtil.getInteger(getQuery(), "start", 0);
    int size = RequestUtil.getInteger(getQuery(), "size", 10);
    List<TaskResponseCBS> list = getResponseList(query, start, size);
    dataResponse.setData(list);
    dataResponse.setSize(list.size());
    dataResponse.setStart(start);
    dataResponse.setTotal(query.list().size());
	dataResponse.setPage_overdue(page_overdue);
	dataResponse.setTotal_overdue(getTotalOverdue(query)); 
	return dataResponse;
  }
  
  private long getTotalOverdue(NativeTaskQuery query) {
	  total_overdue = 0;
	  for (Task task : query.list()) {
	    if(task.getDueDate() != null && task.getDueDate().before(new Date())) {
	    	total_overdue++;
	    }
	  }
	  return total_overdue;
  }
  
  private List<TaskResponseCBS> getResponseList(NativeTaskQuery query, int start, int size) {
	  List<TaskResponseCBS> responseList = new ArrayList<TaskResponseCBS>();
		RestResponseFactoryCBS restResponseFactory = (RestResponseFactoryCBS) this.getApplication(ActivitiRestServicesApplication.class).getRestResponseFactoryCBS();
		for (Object task : query.listPage(start, size)) {
			TaskResponseCBS taskResponse = restResponseFactory.createTaskReponseCBSSimple(this, (Task) task);
			String taskId = ((Task)task).getId();
			System.out.println(taskResponse.getProcessInstanceId());
			// add task local variables
			List<String> taskVarNames = new ArrayList<String>();
			taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_ASSIGNMENT_TIME);
			taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_CAN_START_TIME);
			taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_IS_DEADLINE);
			taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_IN_PROGRESS_TIME);
			taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_TASK_IS_WAITING);
			taskVarNames.add(ConstantsCBS.VARIABLE_TYPE_TASK_WAITING_REASON);
			
			Map<String, Object> taskVariables = taskService.getVariablesLocal(taskId, taskVarNames);
			
		    Date taskAssignmentTime = (Date) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_ASSIGNMENT_TIME);
		    Date taskInProgressTime = (Date) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_IN_PROGRESS_TIME);
		    Date taskCanStartTime = (Date) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_CAN_START_TIME);
		    boolean taskIsDeadline = false;
		    if( taskVariables.get(ConstantsCBS.VARIABLE_TYPE_IS_DEADLINE) != null) {
			  taskIsDeadline = (Boolean) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_IS_DEADLINE);
		    }
		    boolean taskIsWaiting = false;
		    if( taskVariables.get(ConstantsCBS.VARIABLE_TYPE_TASK_IS_WAITING) != null) {
		    	taskIsWaiting = (Boolean) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_TASK_IS_WAITING);
		    }
		    String taskWaitingReason = (String) taskVariables.get(ConstantsCBS.VARIABLE_TYPE_TASK_WAITING_REASON); 
		    
			taskResponse.setAssignmentTime(taskAssignmentTime);
			taskResponse.setInProgressTime(taskInProgressTime);
		    taskResponse.setCanStartTime(taskCanStartTime);
		    taskResponse.setDeadline(taskIsDeadline);
		    taskResponse.setWaiting(taskIsWaiting);
		    taskResponse.setWaitingReason(taskWaitingReason);

			// add process variables to task. 
			Map<String, Object> vars = ActivitiUtil.getRuntimeService().getVariables(taskResponse.getProcessInstanceId());
		    Map<String, String> variableMap = new HashMap<String,String>(); 
		    for(String name: vars.keySet()) {
		    	Object object = vars.get(name);
		    	String value = object == null ? null : object.toString();
		    	if("customerKey".equals(name) && "adhoc".equals(value)) {
		    		String adhocParentId = (String)ActivitiUtil.getRuntimeService()
		    				.getVariable(taskResponse.getProcessInstanceId(), ConstantsCBS.ADHOC_PARENT_PROCESS_ID);
		    		if(adhocParentId != null) {
		    			value = (String)ActivitiUtil.getHistoryService().createHistoricVariableInstanceQuery()
		    					.processInstanceId(adhocParentId)
		    					.variableName("customerKey")
		    					.singleResult()
		    					.getValue();
//		    			value = (String)ActivitiUtil.getRuntimeService().getVariable(adhocParentId, "customerKey");
		    		}
		    		
		    	}
		    	
		    	variableMap.put(name, value);
		    }
		    taskResponse.setVariableMap(variableMap);
		    
		    // add process comments total to task.
			int total = service.getCountByProcessId(((Task)task).getProcessInstanceId());
			taskResponse.setTotalProcessComments(total);
			
		    // if current task is overdue increment page overdue count
			if(taskResponse.getDueDate() != null && taskResponse.getDueDate().before(new Date())) {
		    	page_overdue++;
		    }
			
			// add identity links to task response
		    List<RestIdentityLink> result = new ArrayList<RestIdentityLink>();
		    List<IdentityLink> identityLinks = ActivitiUtil.getTaskService().getIdentityLinksForTask(taskId);
		    
		    String taskCandidateGroupMap = 
		    		taskResponse.getVariableMap().get(ConstantsCBS.VARIABLE_TYPE_TASK_CANDIDATE_GROUP_MAP);
		    identityLinks = ProcessListSupport.addExtraIdentityLink(identityLinks, taskCandidateGroupMap, taskResponse.getTaskDefinitionKey(),
					taskResponse.getId(), taskResponse.getProcessDefinitionId(), taskResponse.getProcessInstanceId() );
		    
		    RestResponseFactory responseFactory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
		    for(IdentityLink link : identityLinks) {
		      result.add(responseFactory.createRestIdentityLink(this, link));
		    }
			taskResponse.setIdentityLinks(result);
		   
		    responseList.add(taskResponse);
		}
	  
	  return responseList;
  }

  
}

