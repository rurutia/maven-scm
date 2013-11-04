package com.cbs.rest.api.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.RequestUtil;
import org.activiti.rest.api.SecuredResource;
import org.apache.commons.lang.time.DateFormatUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cbs.persistence.service.TimerService;
import com.cbs.rest.api.RestResponseFactoryCBS;
import com.cbs.rest.api.utility.ConstantsCBS;

/**
 * Get single active task response
 * @link TaskResponseCBS
 * 
 * Update a task 
 * Apart from default attributes, additional attributes such as 
 * "priorityReadable", "comment", "can start time" are supported
 *
 */
public class TaskResourceCBS extends SecuredResource {
	
  @Get
  public TaskResponseCBS getTaskInstance() {
    if(authenticate() == false) return null;
    
	String taskId = (String) getRequest().getAttributes().get("taskId");
	
    TaskService taskService = ActivitiUtil.getTaskService();
    Task taskInstance = taskService.createTaskQuery().taskId(taskId).singleResult();
    
    return new RestResponseFactoryCBS().createTaskReponseComprehensive(this, taskInstance);
  }

  @Post
  @SuppressWarnings("unchecked")
  public TaskResponseCBS updateTask(Representation entity) {
    if(authenticate() == false) return null;
    TaskService taskService = ActivitiUtil.getTaskService();
    
    String taskId = (String) getRequest().getAttributes().get("taskId");
    Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
    String processId = task.getProcessInstanceId();
    
    ObjectMapper mapper = new ObjectMapper();
    List<TaskAction> taskLog = (List<TaskAction>) taskService.getVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_TASK_LOG);
    String taskLogJson = (String)taskService.getVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_TASK_LOG_JSON);
    try {
    	if(taskLog == null && taskLogJson == null) 
    		taskLog = new ArrayList<TaskAction>();
    	else if(taskLog == null && taskLogJson != null) 
    		taskLog = mapper.readValue(taskLogJson,new TypeReference<ArrayList<TaskAction>>(){});
    	else if(taskLog != null && taskLogJson == null) {
    		taskLogJson = mapper.writeValueAsString(taskLog);
    		taskService.setVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_TASK_LOG_JSON, taskLogJson);
    	}
    	else if(taskLog != null && taskLogJson != null) 
    		taskLog = mapper.readValue(taskLogJson,new TypeReference<ArrayList<TaskAction>>(){});
    }catch (Exception e) {
    	throw new ActivitiException(e.getMessage());
    } 
    
    try {
      String taskParams = entity.getText();
      JsonNode taskJSON = new ObjectMapper().readTree(taskParams);
      
      String description = null;
      if(taskJSON.path("description") != null && taskJSON.path("description").getTextValue() != null) {
        description = taskJSON.path("description").getTextValue();
        task.setDescription(description);
      }
      
      String assignee = null;
      if(taskJSON.path("assignee") != null && taskJSON.path("assignee").getTextValue() != null) {
        assignee = taskJSON.path("assignee").getTextValue();
        task.setAssignee(assignee);
      }
      
      String owner = null;
      if(taskJSON.path("owner") != null && taskJSON.path("owner").getTextValue() != null) {
        owner = taskJSON.path("owner").getTextValue();
        task.setOwner(owner);
      }
      
      String priority = null;
      if(taskJSON.path("priority") != null && taskJSON.path("priority").getTextValue() != null) {
        priority = taskJSON.path("priority").getTextValue();
        task.setPriority(RequestUtil.parseToInteger(priority));
      }
      
      String priorityReadable = null;
      if(taskJSON.path("priorityReadable") != null && taskJSON.path("priorityReadable").getTextValue() != null) {
    	priorityReadable = taskJSON.path("priorityReadable").getTextValue();
        if(TaskResponseCBS.convertPriorityReadableToNum(priorityReadable) != task.getPriority()) {
            taskLog.add(new TaskAction(new Date(), loggedInUser, TaskAction.TYPE_PRIORITY, priorityReadable));
            taskLogJson = mapper.writeValueAsString(taskLog);
            taskService.setVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_TASK_LOG_JSON, taskLogJson);
        }
        task.setPriority(TaskResponseCBS.convertPriorityReadableToNum(priorityReadable));
      }
      
      String dueDateEpoch = null;
      if(taskJSON.path("dueDate") != null && taskJSON.path("dueDate").getTextValue() != null) {
        dueDateEpoch = taskJSON.path("dueDate").getTextValue();
        long epoch = Long.parseLong(dueDateEpoch);
        Date date = new Date(epoch * 1000);
        if( !(date.compareTo(task.getDueDate()) == 0) ) {
            taskLog.add(new TaskAction(new Date(), loggedInUser, TaskAction.TYPE_DUE_DATE, date.toString()));
            taskLogJson = mapper.writeValueAsString(taskLog);
            taskService.setVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_TASK_LOG_JSON, taskLogJson);
        }
        task.setDueDate(date);
      }
      
      String comment = null;
      if(taskJSON.path("comment") != null && taskJSON.path("comment").getTextValue() != null) {
    	comment = taskJSON.path("comment").getTextValue();
    	processId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
    	Authentication.setAuthenticatedUserId(loggedInUser);
    	taskService.addComment(taskId, processId, comment);
      }
      
      taskService.saveTask(task);
      
//      String priorityChangeTimeEpoch = null;
//      if(taskJSON.path("priorityChangeTime") != null && taskJSON.path("priorityChangeTime").getTextValue() != null) {
//    	  priorityChangeTimeEpoch = taskJSON.path("priorityChangeTime").getTextValue();
//    	  long epoch = Long.parseLong(priorityChangeTimeEpoch);
//    	  Date date = new Date(epoch * 1000);
//    	  String dateFormatted = DateFormatUtils.format(date,"yyyy-MM-dd HH:mm:ss'.0'");
//
//    	  // Timer and the task it attaches are within same execution
//    	  String executionId = task.getExecutionId();
//    	  // timer id and task id follow naming convention
//    	  String timerId = ConstantsCBS.BOUNDARY_TIMER_PRIORITY_ID + task.getTaskDefinitionKey();
//
//    	  ApplicationContext ctx = 
//    			  new ClassPathXmlApplicationContext("activiti-context.xml");
//    	  TimerService service = ctx.getBean("timerService", TimerService.class);
//    	  service.updateTimer(dateFormatted, executionId, timerId);
//
//    	  taskService.setVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_PRIORITY_CHANGE_TIME, date);
//      }
      
//      String priorityFutureReadable = null;
//      if(taskJSON.path("priorityFutureReadable") != null && taskJSON.path("priorityFutureReadable").getTextValue() != null) {
//    	priorityFutureReadable = taskJSON.path("priorityFutureReadable").getTextValue();
//    	taskService.setVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_PRIORITY_FUTURE,
//    			                      TaskResponseCBS.convertPriorityReadableToNum(priorityFutureReadable));
//      }
      
      String priorityChangeTimeEpoch = null;
      if(taskJSON.path("priorityChangeTime") != null && taskJSON.path("priorityChangeTime").getTextValue() != null) {
    	  RuntimeService runtimeService = ActivitiUtil.getRuntimeService();
    	  String priorityFutureReadable = null;
    	  priorityChangeTimeEpoch = taskJSON.path("priorityChangeTime").getTextValue();

    	  String cancelTimerExecutionId = (String)taskService.getVariableLocal(taskId, "cancelTimerExecutionId");

    	  if(taskJSON.path("priorityFutureReadable") != null && taskJSON.path("priorityFutureReadable").getTextValue() != null) {
    		  priorityFutureReadable = taskJSON.path("priorityFutureReadable").getTextValue();
    		  int futurePriority = TaskResponseCBS.convertPriorityReadableToNum(priorityFutureReadable);

    		  long epoch = Long.parseLong(priorityChangeTimeEpoch);
    		  Date futureDate = new Date(epoch * 1000);

    		  Date existingDate = (Date)taskService.getVariableLocal(taskId,  ConstantsCBS.VARIABLE_TYPE_PRIORITY_CHANGE_TIME);
    		  Integer existingFuturePriority = (Integer)taskService.getVariableLocal(taskId,  ConstantsCBS.VARIABLE_TYPE_PRIORITY_FUTURE);  
    		  // task has no future priority been set
    		  if(existingDate == null || existingFuturePriority == null) {
    			  System.out.println("create new timer...");
    			  createFuturePriorityTimer(runtimeService, taskService, processId, taskId, futureDate, priorityFutureReadable);
    		  }
    		  else { // if future priority has already been set
    			  if(existingDate.equals(futureDate) && existingFuturePriority == futurePriority) {
    				  System.out.println("------------date equal & priority equal, do nothing");
    			  }
    			  else if(existingDate.equals(futureDate) && existingFuturePriority != futurePriority) {
    				  System.out.println("------------date equal & priority not equal");  
    				  cancelTimerExecutionId = (String)taskService.getVariableLocal(taskId, "cancelTimerExecutionId");
    				  runtimeService.setVariable(cancelTimerExecutionId, "futurePriority", futurePriority);
    				  taskService.setVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_PRIORITY_FUTURE, futurePriority);
    			  } 
    			  else {
    				  // cancel old timer before creation
    	    		  cancelFutureTimer(runtimeService, taskService, taskId, cancelTimerExecutionId);
        			  createFuturePriorityTimer(runtimeService, taskService, processId, taskId, futureDate, priorityFutureReadable);
    			  }
    		  } 
    	  }
    	  else { // cancel timer
    		  cancelFutureTimer(runtimeService, taskService, taskId, cancelTimerExecutionId);
    	  }

      }
      
      
      String canStartTimeEpoch = null;
      if(taskJSON.path("canStartTime") != null && taskJSON.path("canStartTime").getTextValue() != null) {
    	  canStartTimeEpoch = taskJSON.path("canStartTime").getTextValue();
          long epoch = Long.parseLong(canStartTimeEpoch);
          Date date = new Date(epoch * 1000);
          Date oldCanStartTime = (Date)taskService.getVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_CAN_START_TIME);
          if( oldCanStartTime != null && !(date.compareTo(oldCanStartTime) == 0) ) {
            taskLog.add(new TaskAction(new Date(), loggedInUser, TaskAction.TYPE_CAN_START_TIME, date.toString()));
            taskLogJson = mapper.writeValueAsString(taskLog);
            taskService.setVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_TASK_LOG_JSON, taskLogJson);
	      }
          taskService.setVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_CAN_START_TIME, date);
      }
      
      boolean deadline = false;
      if(taskJSON.path("deadline") != null && taskJSON.path("deadline").getTextValue() != null) {
    	  if(taskJSON.path("deadline").getTextValue() != null && taskJSON.path("deadline").getTextValue().equals("true")){
    		  deadline = true;
    	  }
  		  taskService.setVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_IS_DEADLINE, deadline);
      }
      
      boolean waiting = false;
      if(taskJSON.path("waiting") != null && taskJSON.path("waiting").getTextValue() != null) {
    	  if(taskJSON.path("waiting").getTextValue() != null && taskJSON.path("waiting").getTextValue().equals("true")){
    		  waiting = true;
    	  }
  		  taskService.setVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_TASK_IS_WAITING, waiting);
      }
      
      String waitingReason = null;
      if(taskJSON.path("waitingReason") != null && taskJSON.path("waitingReason").getTextValue() != null) {
    	  if(!taskJSON.path("waitingReason").getTextValue().equals(""))
    		  waitingReason = taskJSON.path("waitingReason").getTextValue();
    	taskService.setVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_TASK_WAITING_REASON, waitingReason);
      }
      
      return new RestResponseFactoryCBS().createTaskReponseComprehensive(this, task);
      
    } catch (Exception e) {
      if(e instanceof ActivitiException) {
        throw (ActivitiException) e;
      }
      throw new ActivitiException("Failed to update task " + taskId, e);
    }
  }
  
  @Delete
  public void deleteTask(Representation entity) {
    if(authenticate() == false) return;
    String taskId = (String) getRequest().getAttributes().get("taskId");
    
    Task task = ActivitiUtil.getTaskService().createTaskQuery().taskId(taskId).singleResult();
    
    if(task == null) {
      throw new ActivitiObjectNotFoundException("Task not found for id " + taskId, Task.class);
    }
    
    ActivitiUtil.getTaskService().deleteTask(taskId);
  }
  
  private void createFuturePriorityTimer(RuntimeService rs, TaskService ts,
		  String processId, String taskId, Date futureDate, String priorityFutureReadable) {
	  String timerTriggerDate = DateFormatUtils.format(futureDate,"yyyy-MM-dd'T'HH:mm:ss");
	  String adhocProcessId = (String)rs.getVariable(processId, ConstantsCBS.ADHOC_CHILD_PROCESS_ID);
	  
	  // task is in Ad-Hoc (sub)process, get its top level Ad-Hoc process id from parent process first
	  if(adhocProcessId == null) {
		  String parentProcessId = (String)rs.getVariable(processId, ConstantsCBS.ADHOC_PARENT_PROCESS_ID); 
		  if(parentProcessId == null)
			  throw new ActivitiException("Failed to update task " + taskId + ". This task has no Ad-Hoc process");
		  else {
			  adhocProcessId = (String)ActivitiUtil.getHistoryService().createHistoricVariableInstanceQuery()
					  .processInstanceId(parentProcessId)
					  .variableName(ConstantsCBS.ADHOC_CHILD_PROCESS_ID)
					  .singleResult()
					  .getValue();
		  }
	  }
	
	  System.out.println("++++++++++++++adhoc process id:" + adhocProcessId);
	  System.out.println("------------priorityFutureReadable" + priorityFutureReadable);
	  // get execution of intermediate message event in Ad-hoc process
	  Execution messageExecution = rs
			  .createExecutionQuery()
			  .processInstanceId(adhocProcessId)
			  .messageEventSubscriptionName(ConstantsCBS.MESSAGE_CREATE_ADHOC_ACTIVITY)
			  .singleResult();

	  String activityType = "futureAction";
	  Map<String, Object> variables = new HashMap<String, Object>();
	  variables.put("newActivityType", activityType);
	  variables.put("timerTriggerDate", timerTriggerDate);
	  variables.put("targetTaskId", taskId);
	  variables.put("futurePriority", TaskResponseCBS.convertPriorityReadableToNum(priorityFutureReadable));
	  rs.setVariables(adhocProcessId, variables);

	  rs.messageEventReceived(ConstantsCBS.MESSAGE_CREATE_ADHOC_ACTIVITY, messageExecution.getId());

	  ts.setVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_PRIORITY_CHANGE_TIME, futureDate);  

	  ts.setVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_PRIORITY_FUTURE,
			  TaskResponseCBS.convertPriorityReadableToNum(priorityFutureReadable));  
  }
  
  private void cancelFutureTimer(RuntimeService runtimeService, TaskService taskService, String taskId, String cancelTimerExecutionId) {
	  System.out.println("-------message cancel id: " + taskService.getVariableLocal(taskId,"cancelTimerExecutionId"));
	  runtimeService.messageEventReceived(ConstantsCBS.MESSAGE_CANCEL_ADHOC_TIMER, cancelTimerExecutionId);
	  taskService.removeVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_PRIORITY_CHANGE_TIME);
	  taskService.removeVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_PRIORITY_FUTURE);
  }
  
  
  
}
