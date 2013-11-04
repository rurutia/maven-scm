package com.cbs.rest.api.process;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.api.runtime.process.ProcessInstanceResponse;
import org.activiti.rest.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

import com.cbs.rest.api.utility.ConstantsCBS;


/**
 * 
 * Perform actions on process, currently supports"
 * activate/suspend process
 * enlist/unlist candidate from process
 * watch/unwatch process
 * 
 */
public class ProcessInstanceResourceCBS extends SecuredResource {

  @Get
  public ProcessInstanceResponse getProcessInstance() {
    if(!authenticate()) {
      return null;
    }
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createProcessInstanceResponse(this, getProcessInstanceFromRequest());
  }
  
  @Delete
  public void deleteProcessInstance() {
    if(!authenticate()) {
      return;
    }
    ProcessInstance processInstance = getProcessInstanceFromRequest();
    String deleteReason = getQueryParameter("deleteReason", getQuery());
    
    ActivitiUtil.getRuntimeService().deleteProcessInstance(processInstance.getId(), deleteReason);
    setStatus(Status.SUCCESS_NO_CONTENT);
  }
  
  @Put
  public ProcessInstanceResponse performProcessInstanceAction(ProcessInstanceActionRequestCBS actionRequest) {
    if(!authenticate()) {
      return null;
    }
    ProcessInstance processInstance = getProcessInstanceFromRequest();
    if(ProcessInstanceActionRequestCBS.ACTION_ACTIVATE.equals(actionRequest.getAction())) {
      return activateProcessInstance(processInstance);
    } else if(ProcessInstanceActionRequestCBS.ACTION_SUSPEND.equals(actionRequest.getAction())) {
      return suspendProcessInstance(processInstance);
    } else if(ProcessInstanceActionRequestCBS.ACTION_ENLIST_CANDIDATE.equals(actionRequest.getAction())) {
      String userId = actionRequest.getUserId();
      return enlistCandidate(processInstance, userId);
    } else if(ProcessInstanceActionRequestCBS.ACTION_UNLIST_CANDIDATE.equals(actionRequest.getAction())) {
      String userId = actionRequest.getUserId();
      return unlistCandidate(processInstance, userId);
    } else if(ProcessInstanceActionRequestCBS.ACTION_WATCH.equals(actionRequest.getAction())) {
      String userId = actionRequest.getUserId();
      return addWatcher(processInstance, userId);
    } else if(ProcessInstanceActionRequestCBS.ACTION_UNWATCH.equals(actionRequest.getAction())) {
      String userId = actionRequest.getUserId();
      return removeWatcher(processInstance, userId);
    }
    throw new ActivitiIllegalArgumentException("Invalid action: '" + actionRequest.getAction() + "'.");
  }
  
  protected ProcessInstanceResponse addWatcher(ProcessInstance processInstance, String userId) {
	String processId = processInstance.getProcessInstanceId();
	String watchers = (String) ActivitiUtil.getRuntimeService().getVariable(processId, ConstantsCBS.VARIABLE_TYPE_WATCHER_LIST);
	if( watchers == null)
		watchers = "";
	if(userId == null) {
		if(!watchers.contains(loggedInUser)) 
		  watchers += loggedInUser + ",";
	} else {
		if(!watchers.contains(userId)) 
		  watchers += userId + ",";
	}
	ActivitiUtil.getRuntimeService().setVariable(
			processId, 
			ConstantsCBS.VARIABLE_TYPE_WATCHER_LIST,
			watchers);
  
    ProcessInstanceResponse response =  getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
		  .createProcessInstanceResponse(this, processInstance);
    return response;
  }
  
  protected ProcessInstanceResponse removeWatcher(ProcessInstance processInstance, String userId) {
	String processId = processInstance.getProcessInstanceId();
    String watchers = (String) ActivitiUtil.getRuntimeService().getVariable(processId, ConstantsCBS.VARIABLE_TYPE_WATCHER_LIST);

	if(watchers == null)
	throw new ActivitiException("Watcher list does not exist.");
	if(userId == null) {
		if(watchers.contains(loggedInUser)) 
			watchers = watchers.replace(loggedInUser + ConstantsCBS.SEPERATOR_COMMA, "");
		else
    		throw new ActivitiException("loggedIn user not found in watcher list");
	} else {
		if(watchers.contains(userId)) {
			watchers = watchers.replace(userId + ConstantsCBS.SEPERATOR_COMMA, "");
	} else
    	throw new ActivitiException("userId " + userId + " not found in watcher list");
	}
	ActivitiUtil.getRuntimeService().setVariable(
			processId, 
			ConstantsCBS.VARIABLE_TYPE_WATCHER_LIST,
			watchers);
  
    ProcessInstanceResponse response =  getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
		  .createProcessInstanceResponse(this, processInstance);
    return response;
  }
  
  protected ProcessInstanceResponse enlistCandidate(ProcessInstance processInstance, String userId) {
	String processId = processInstance.getProcessInstanceId();
	String candidateAssignees = (String) ActivitiUtil.getRuntimeService().getVariable(processId, ConstantsCBS.VARIABLE_TYPE_CANDIDATE_LIST);
	if( candidateAssignees == null)
		candidateAssignees = "";
	if(userId == null) {
		if(!candidateAssignees.contains(loggedInUser)) 
			candidateAssignees += loggedInUser + ",";
	} else {
		if(!candidateAssignees.contains(userId)) 
			candidateAssignees += userId + ",";
	}
	ActivitiUtil.getRuntimeService().setVariable(
			processId, 
			ConstantsCBS.VARIABLE_TYPE_CANDIDATE_LIST,
			candidateAssignees);
  
    ProcessInstanceResponse response =  getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
		  .createProcessInstanceResponse(this, processInstance);
    return response;
  }
  
  protected ProcessInstanceResponse unlistCandidate(ProcessInstance processInstance, String userId) {
	String processId = processInstance.getProcessInstanceId();
    String candidateAssignees = (String) ActivitiUtil.getRuntimeService().getVariable(processId, ConstantsCBS.VARIABLE_TYPE_CANDIDATE_LIST);

	if(candidateAssignees == null)
	throw new ActivitiException("candidate assignee list does not exist.");
	if(userId == null) {
		if(candidateAssignees.contains(loggedInUser)) 
			candidateAssignees = candidateAssignees.replace(loggedInUser + ConstantsCBS.SEPERATOR_COMMA, "");
		else
    		throw new ActivitiException("loggedIn user not found in candidate assignee list");
	} else {
		if(candidateAssignees.contains(userId)) {
			candidateAssignees = candidateAssignees.replace(userId + ConstantsCBS.SEPERATOR_COMMA, "");
	} else
    	throw new ActivitiException("userId " + userId + " not found in candidate assignee list");
	}
	ActivitiUtil.getRuntimeService().setVariable(
			processId, 
			ConstantsCBS.VARIABLE_TYPE_CANDIDATE_LIST,
			candidateAssignees);
  
    ProcessInstanceResponse response =  getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
		  .createProcessInstanceResponse(this, processInstance);
    return response;
  }
  
  protected ProcessInstanceResponse activateProcessInstance(ProcessInstance processInstance) {
    if(!processInstance.isSuspended()) {
      throw new ResourceException(Status.CLIENT_ERROR_CONFLICT.getCode(), "Process instance with id '" + processInstance.getId() + "' is already active.", null, null);
    }
    ActivitiUtil.getRuntimeService().activateProcessInstanceById(processInstance.getId());
   
    ProcessInstanceResponse response =  getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createProcessInstanceResponse(this, processInstance);
    
    // No need to re-fetch the instance, just alter the suspended state of the result-object
    response.setSuspended(false);
    return response;
  }

  protected ProcessInstanceResponse suspendProcessInstance(ProcessInstance processInstance) {
    if(processInstance.isSuspended()) {
      throw new ResourceException(Status.CLIENT_ERROR_CONFLICT.getCode(), "Process instance with id '" + processInstance.getId() + "' is already suspended.", null, null);
    }
    ActivitiUtil.getRuntimeService().suspendProcessInstanceById(processInstance.getId());
    
    ProcessInstanceResponse response =  getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createProcessInstanceResponse(this, processInstance);
    
    // No need to re-fetch the instance, just alter the suspended state of the result-object
    response.setSuspended(true);
    return response;
  }
  
  
  protected ProcessInstance getProcessInstanceFromRequest() {
    String processInstanceId = getAttribute("processInstanceId");
    if (processInstanceId == null) {
      throw new ActivitiIllegalArgumentException("The processInstanceId cannot be null");
    }
    
   ProcessInstance processInstance = ActivitiUtil.getRuntimeService().createProcessInstanceQuery()
           .processInstanceId(processInstanceId).singleResult();
    if (processInstance == null) {
      throw new ActivitiObjectNotFoundException("Could not find a process instance with id '" + processInstanceId + "'.", ProcessInstance.class);
    }
    return processInstance;
  }
}
