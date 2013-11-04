package com.cbs.rest.api.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.TaskService;
import org.activiti.engine.identity.User;
import org.activiti.engine.task.NativeTaskQuery;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.application.ActivitiRestServicesApplication;
import org.restlet.data.Form;
import org.restlet.resource.Get;

import com.cbs.rest.api.RestResponseFactoryCBS;
import com.cbs.rest.api.utility.ConstantsCBS;

public class TaskCollectionSummaryResource extends SecuredResource {

	TaskService taskService = ActivitiUtil.getTaskService();

	@Get
	public DataResponseTaskSummary getTasks() {
		if(authenticate() == false) return null;

		// get query parameters
		Form query = getQuery();

	    // GMT Epoch milliseconds
		String strMinStartDate = query.getValues("minStartDate");
		String strMaxStartDate = query.getValues("maxStartDate");

		String strTaskNameKeyword = query.getValues("taskNameKeyword");
		strTaskNameKeyword = (strTaskNameKeyword != null) && strTaskNameKeyword.equals("") ? null : strTaskNameKeyword;
		
		Map<String, Map<String, Integer>> userSummary= new HashMap<String, Map<String, Integer>>();
		
		List<User> users = ActivitiUtil.getIdentityService().createUserQuery().list();
		List<String> userIds = new ArrayList<String>();
		for(User user : users) {
			userIds.add(user.getId());
		}
		userIds.add("unassigned");
		userIds.add("all");
		
		for(String userId : userIds) {
			String selectClause = "SELECT T.*,V.LONG_ AS `CANSTART`,V2.TEXT_ AS `TAGS` from ACT_RU_TASK T LEFT JOIN ACT_RU_VARIABLE V ON V.NAME_='canStartTime' and V.TASK_ID_=T.ID_ LEFT JOIN ACT_RU_VARIABLE V2 ON V2.NAME_='tags' and V2.PROC_INST_ID_=T.PROC_INST_ID_";
//			String selectClause = "SELECT T.*,V.LONG_ AS `CANSTART`,V2.TEXT_ AS `TAGS`,V3.LONG_ AS `WAITING` from ACT_RU_TASK T LEFT JOIN ACT_RU_VARIABLE V ON V.NAME_='canStartTime' and V.TASK_ID_=T.ID_ LEFT JOIN ACT_RU_VARIABLE V2 ON V2.NAME_='tags' and V2.PROC_INST_ID_=T.PROC_INST_ID_ LEFT JOIN ACT_RU_VARIABLE V3 ON V3.NAME_='waiting' and V3.TASK_ID_=T.ID_";
			String outerWhereClause = getQueryWhereClause(query, userId);
			selectClause += outerWhereClause;
			NativeTaskQuery nativeQuery = taskService.createNativeTaskQuery()
													.sql(selectClause);
			if (strMinStartDate != null && strMaxStartDate != null) {
				nativeQuery = nativeQuery
						.parameter("minStartTime", strMinStartDate)
						.parameter("maxStartTime", strMaxStartDate);
			}
			
			Map<String, Integer> summary = executeQuery(nativeQuery);
			userSummary.put(userId, summary);
		}

		DataResponseTaskSummary response = new DataResponseTaskSummary();
		response.setData(userSummary);
		
		return response;
	}

	private String getQueryWhereClause(Form form, String assignee) {
		String strPriorityReadable = form.getValues("priorityReadable");

		String strMinStartDate = form.getValues("minStartDate");
		String strMaxStartDate = form.getValues("maxStartDate");

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
		
		if( assignee.equals("unassigned")) {
			outerWhereConditions.add("ASSIGNEE_ IS NULL");
		} 
		else if( !assignee.equals("all") ) {
			outerWhereConditions.add("ASSIGNEE_ = '" + assignee + "'");
		}
		
		if(priority != -1) {
			outerWhereConditions.add("PRIORITY_ =" + priority); 
		}
		if(strTaskNameKeyword != null) {
			outerWhereConditions.add("T.NAME_ COLLATE UTF8_GENERAL_CI LIKE '%" + strTaskNameKeyword + "%'"); 
		}
		
		if(tags != null) {
			for(String tag: tags.split(ConstantsCBS.SEPERATOR_COMMA)) {
				outerWhereConditions.add("V2.TEXT_ like " + "\"%" + tag + "%\"");
			}
		}

		if (strMinStartDate != null && strMaxStartDate != null) {
			outerWhereConditions.add("(V.LONG_ between #{minStartTime} AND #{maxStartTime} OR V.LONG_ IS NULL)");
		}
		
		if (waiting != null && waiting.equals("false")) {
			outerWhereConditions.add("(V3.LONG_=0 OR V3.LONG_ IS NULL)");
		}

		if(outerWhereConditions.size() > 0) {
			outerWhereClause.append(" WHERE " + outerWhereConditions.get(0));
			for(int i = 1; i < outerWhereConditions.size(); i++) {
				outerWhereClause.append(" AND " + outerWhereConditions.get(i));
			}
		}
		return outerWhereClause.toString();
	}

	private Map<String, Integer> executeQuery(NativeTaskQuery query) {
		Map<String, Integer> summary = new HashMap<String, Integer>();
		summary.put("total", query.list().size());
		summary.put("overdue", getOverdueTaskCountByAssignee(query));

		return summary;
	}

	private int getOverdueTaskCountByAssignee(NativeTaskQuery query) {
		int overdue = 0;
		RestResponseFactoryCBS restResponseFactory = (RestResponseFactoryCBS) this.getApplication(ActivitiRestServicesApplication.class).getRestResponseFactoryCBS();
		for (Object task : query.list()) {
			TaskResponseCBS taskResponse = restResponseFactory.createTaskReponseCBSSimple(this, (Task) task);
			if(taskResponse.getDueDate() != null && taskResponse.getDueDate().before(new Date())) {
				overdue++;
			}
		}

		return overdue;
	}

}


