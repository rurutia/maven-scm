package com.cbs.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.HistoricProcessInstanceQueryProperty;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.query.NativeQuery;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.SecuredResource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.restlet.resource.Get;

public class Test extends SecuredResource {
	
	public static void main(String[] args) {
//		DelegateTask task = null;
//		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");String dueDate;
//		if(task.getVariable("dateCustomerReceiveSample")!=null){
//		dueDate=(String)task.getVariable("dateCustomerReceiveSample");
//		task.setDueDate(DateUtils.addDays(formatter.parse(dueDate),2));
//		}
//		if(task.getVariable("callbackDateTime")!=null){
//		dueDate=(String)task.getVariable("callbackDateTime");
//		task.setDueDate(formatter.parse(dueDate));
//		}
//		task.setVariable("sendSampleOutcome",null);task.setVariable("dateCustomerReceiveSample",null);task.setVariable("callbackDateTime",null);
//		List<Execution> list = ActivitiUtil.getRuntimeService().createExecutionQuery().processInstanceId("132659").list();
//		for(Execution e : list) {
////			System.out.println(e.getActivityId());
//		}
//		
//		
//		java.util.List<Object> activityList = new java.util.ArrayList<Object>();
//		java.util.List<HistoricActivityInstance> list2 = ActivitiUtil.getHistoryService().createHistoricActivityInstanceQuery()
//				.processInstanceId("135608").list();
//		for(HistoricActivityInstance activity : list2) {
//			if(activity.getEndTime() == null && (activity.getActivityType().equals("userTask") || activity.getActivityType().equals("callActivity")))
//			{
//			2013-10-19T23:21:59	System.out.println(activity.getActivityType());
//				activityList.add(activity); 
//			}
//		}
//		System.out.println(activityList.size());
		
		Execution execution = ActivitiUtil.getRuntimeService().createExecutionQuery().parentId("169308").singleResult();
System.out.println(execution.getId());
System.out.println(execution.getParentId());

//		taskid: 1541, 
//		2004 2102
//		
	}
	  
	public static void main3(String[] args) {
//		ActivitiUtil.getTaskService()
//		.createNativeTaskQuery().sql("delete from ACT_HI_COMMENT where TASK_ID_='259' AND PROC_INST_ID_='247'")
//		.singleResult();
		
		List<Deployment> deployments = ActivitiUtil.getRepositoryService().createDeploymentQuery().list();
		for(Deployment deployment : deployments) {
			System.out.println(deployment.getId());
			System.out.println(deployment.getName());
		}
		
		List<ProcessDefinition> definitions = ActivitiUtil.getRepositoryService().createProcessDefinitionQuery().list();
		for(ProcessDefinition def : definitions) {
			System.out.println(def.getId());
			System.out.println(def.getKey());
			System.out.println(def.getVersion());
			System.out.println(def.getName());
			System.out.println(def.getResourceName());
		}
		
//	    ActivitiUtil.getRuntimeService().createProcessInstanceQuery().
		
	}
	public static void main2(String[] args) {
	    System.out.println("test starts...");
	    TaskService taskService = ActivitiUtil.getTaskService();
	    RuntimeService rs = ActivitiUtil.getRuntimeService();
	    HistoryService hs = ActivitiUtil.getHistoryService();
	    ManagementService ms = ActivitiUtil.getManagementService();
	    String selectClause = 
//"select ID_, START_TIME_ FROM ACT_HI_PROCINST P where exists (select * from ACT_RU_TASK T where P.PROC_INST_ID_=T.PROC_INST_ID_)"	    		;
//"select ID_, START_TIME_ FROM ACT_HI_PROCINST P where exists (select * from ACT_RU_TASK T inner join ACT_RU_VARIABLE V where P.PROC_INST_ID_=T.PROC_INST_ID_ and T.ID_=V.TASK_ID_ and V.NAME_='inProgressTime' and T.ASSIGNEE_='kermit')"	    		;
"select ID_,PROC_INST_ID_,BUSINESS_KEY_,PROC_DEF_ID_,START_TIME_,END_TIME_,DURATION_,START_USER_ID_,START_ACT_ID_,END_ACT_ID_,SUPER_PROCESS_INSTANCE_ID_,DELETE_REASON_ FROM ACT_HI_PROCINST P where P.PROC_INST_ID_ in (select PROC_INST_ID_ from ACT_RU_VARIABLE where NAME_='candidateAssignees' and TEXT_ like '%kermit%') and  END_TIME_ is null and exists (select * from ACT_RU_TASK T inner join ACT_RU_VARIABLE V where T.PROC_INST_ID_=P.PROC_INST_ID_ and T.ID_=V.TASK_ID_ and ((V.NAME_='inProgressTime' and T.ASSIGNEE_<>'kermit') or V.NAME_ <>'inProgressTime'))"	    		;

	    		;
	    System.out.println("selectClause is " + selectClause);
	    List<HistoricProcessInstance> processes = 
	    		hs.createNativeHistoricProcessInstanceQuery()
	    		.sql(selectClause)
	    		.list();
	    System.out.println(processes.size());
	    for(HistoricProcessInstance ps : processes) {
	    	System.out.println(ps.getId());
	    	System.out.println(ps.getStartTime());
	    	System.out.println(ps.getStartUserId());
	    	System.out.println(ps.getEndTime());
	    	System.out.println("----------------------------");
	    }
//	    @SuppressWarnings("rawtypes")
//		NativeQuery query = taskService.createNativeTaskQuery()
//	            .sql("SELECT T1.ID_ FROM " + managementService.getTableName(Task.class) + " T1, "
//	  + managementService.getTableName(VariableInstanceEntity.class) + " V1 WHERE V1.TASK_ID_ = T1.ID_");
//	         
//	    for(Object task: query.list()) {
//	    	System.out.println( ((Task)task).getId() );
//	    }
	  }

}
