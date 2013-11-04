package com.cbs.rest.api.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;

import com.cbs.rest.api.RestResponseFactoryCBS;
import com.cbs.rest.api.utility.ConstantsCBS;

/**
 * Apart from default task operation such as "claim", "unclaim", "assign"
 * "progress" and "assign-progress" are added to work with "in progress"
 * concept.
 * 
 */
public class TaskOperationResourceCBS extends SecuredResource {
	
  private TaskService taskService = ActivitiUtil.getTaskService();
	
  @Put
  public TaskResponseCBS executeTaskOperation(Representation entity) {
    if(authenticate() == false) return null;
    String taskId = (String) getRequest().getAttributes().get("taskId");
    Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
	String processId = task.getProcessInstanceId();
    String operation = (String) getRequest().getAttributes().get("operation");
    
    TaskResponseCBS taskResponse =  new RestResponseFactoryCBS().createTaskReponseComprehensive(this, task);
    
    if ("claim".equals(operation)) { 
      taskService.claim(taskId, loggedInUser);
    } 
    else if ("unclaim".equals(operation)) {
        String previousAssignee = taskService.createTaskQuery().taskId(taskId).singleResult().getAssignee();
        String assignmentChange = "from " + previousAssignee + " to unassigned";
        updateTaskLog(taskId, TaskAction.TYPE_CHANGE_ASSIGNMENT, assignmentChange);
        taskService.removeVariableLocal(task.getId(), ConstantsCBS.VARIABLE_TYPE_IN_PROGRESS_TIME);
        taskService.claim(taskId, null);
    } 
    else if ("complete".equals(operation)) {
      Map<String, String> variables = new HashMap<String, String>();
      try {
        if (entity != null) {
          String startParams = entity.getText();
          if (StringUtils.isNotEmpty(startParams)) {
            JsonNode startJSON = new ObjectMapper().readTree(startParams);
            Iterator<String> itName = startJSON.getFieldNames();
            while(itName.hasNext()) {
              String name = itName.next();
              JsonNode valueNode = startJSON.path(name);
              variables.put(name, valueNode.asText());
            }
          }
        }
      } catch (ActivitiIllegalArgumentException e) {
    	  throw new ActivitiException(e.getMessage());
      } catch(Exception e) {
        if(e instanceof ActivitiException) {
          throw (ActivitiException) e;
        }
        throw new ActivitiException("Did not receive the operation parameters", e);
      }
      variables.remove("taskId");
      taskService.removeVariableLocal(task.getId(), ConstantsCBS.VARIABLE_TYPE_IN_PROGRESS_TIME);
      ActivitiUtil.getFormService().submitTaskFormData(taskId, variables);
    } 
    else if ("assign".equals(operation)) {
    	String userId = null;
    	try {
    		String startParams = entity.getText();
    		JsonNode startJSON = new ObjectMapper().readTree(startParams);
    		userId = startJSON.path("userId").getTextValue();
    		if( null == ActivitiUtil.getIdentityService().createUserQuery().userId(userId).singleResult() )
    			throw new ActivitiIllegalArgumentException("user id " + userId + " not found");
    		taskService.setVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_ASSIGNMENT_TIME, new Date());
    	} catch(ActivitiIllegalArgumentException e) {
    		throw new ActivitiException(e.getMessage());
    	} catch(Exception e) {
    		throw new ActivitiException("Did not assign the operation parameters", e);
    	}
    	
    	String previousAssignee = taskService.createTaskQuery().taskId(taskId).singleResult().getAssignee();
        String oldAssignee = previousAssignee == null ? "unassigned" : previousAssignee;
        String assignmentChange = "from " + oldAssignee + " to " + userId;
        updateTaskLog(taskId, TaskAction.TYPE_CHANGE_ASSIGNMENT, assignmentChange);
        taskService.removeVariableLocal(task.getId(), ConstantsCBS.VARIABLE_TYPE_IN_PROGRESS_TIME);
    	taskService.setAssignee(taskId, userId);
    }
    // make an assigned task become "in progress"
    else if ("progress".equals(operation)) {
    	String userId = null;
    	try {
    		String startParams = entity.getText();
    		JsonNode startJSON = new ObjectMapper().readTree(startParams);
    		userId = startJSON.path("userId").getTextValue();
    		taskService.setVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_IN_PROGRESS_TIME, new Date());
    	} catch(ActivitiIllegalArgumentException e) {
    		throw new ActivitiException(e.getMessage());
    	} catch(Exception e) {
    		throw new ActivitiException("Did not assign the operation parameters", e);
    	}         
//    	addToCandidateAssigneeList(processId, userId);
    }
    else if ("assign-progress".equals(operation)) {
    	String userId = null;
    	try {
    		String startParams = entity.getText();
    		JsonNode startJSON = new ObjectMapper().readTree(startParams);
    		userId = startJSON.path("userId").getTextValue();
    		taskService.setVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_ASSIGNMENT_TIME, new Date());
    		taskService.setVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_IN_PROGRESS_TIME, new Date());
    	} catch(Exception e) {
    		throw new ActivitiException("Did not assign the operation parameters", e);
    	}
//    	addToCandidateAssigneeList(processId, userId);
    	taskService.setAssignee(taskId, userId);
    } 
    else if ("unprogress".equals(operation)) {
         taskService.removeVariableLocal(task.getId(), ConstantsCBS.VARIABLE_TYPE_IN_PROGRESS_TIME);
    } 
    else {
      throw new ActivitiIllegalArgumentException("'" + operation + "' is not a valid operation");
    }
    
    if ("complete".equals(operation)) {
    	taskResponse.setEndTime(new Date());
    } 
    else {
    	Task taskInstance = taskService.createTaskQuery().taskId(taskId).singleResult();
    	taskResponse =  new RestResponseFactoryCBS().createTaskReponseComprehensive(this, taskInstance);
    }
    
    return taskResponse;
  }

  @SuppressWarnings("unchecked")
  private void updateTaskLog(String taskId, String actionType, String actionDetail) {
	  ObjectMapper mapper = new ObjectMapper();
	  List<TaskAction> taskLog = (List<TaskAction>)taskService.getVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_TASK_LOG);
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
		  
		  taskLog.add(new TaskAction(new Date(), loggedInUser, TaskAction.TYPE_CHANGE_ASSIGNMENT, actionDetail));
	      taskLogJson = mapper.writeValueAsString(taskLog);
	      taskService.setVariableLocal(taskId, ConstantsCBS.VARIABLE_TYPE_TASK_LOG_JSON, taskLogJson);
	  } catch (Exception e) {
		  throw new ActivitiException(e.getMessage());
	  } 
  }
  
}
