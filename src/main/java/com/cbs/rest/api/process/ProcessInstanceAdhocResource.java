package com.cbs.rest.api.process;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.RestResponseFactory;
import org.activiti.rest.api.engine.variable.RestVariable;
import org.activiti.rest.api.runtime.process.BaseProcessInstanceResource;
import org.activiti.rest.api.runtime.process.ProcessInstanceCreateRequest;
import org.activiti.rest.api.runtime.process.ProcessInstanceResponse;
import org.activiti.rest.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Post;

import com.cbs.rest.api.utility.ConstantsCBS;

/**
 * 
 * Start and initialze an associated Ad-Hoc process when a new process is created
 *
 */

public class ProcessInstanceAdhocResource extends BaseProcessInstanceResource{

	  @Post
	  public ProcessInstanceResponse createProcessInstance(ProcessInstanceCreateRequest request) {
	    
	    if(request.getProcessDefinitionId() == null && request.getProcessDefinitionKey() == null && request.getMessage() == null) {
	      throw new ActivitiIllegalArgumentException("Either processDefinitionId, processDefinitionKey or message is required.");
	    }
	    
	    int paramsSet = ((request.getProcessDefinitionId() != null) ? 1 : 0)
	            + ((request.getProcessDefinitionKey() != null) ? 1 : 0)
	            + ((request.getMessage() != null) ? 1 : 0);
	    
	    if(paramsSet > 1) {
	      throw new ActivitiIllegalArgumentException("Only one of processDefinitionId, processDefinitionKey or message should be set.");
	    }
	    
	    RestResponseFactory factory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
	    
	    Map<String, Object> startVariables = null;
	    if(request.getVariables() != null) {
	      startVariables = new HashMap<String, Object>();
	      for(RestVariable variable : request.getVariables()) {
	        if(variable.getName() == null) {
	          throw new ActivitiIllegalArgumentException("Variable name is required.");
	        }
	        startVariables.put(variable.getName(), factory.getVariableValue(variable));
	      }
	    }
	    
	    // Actually start the instance based on key or id
	    try {
	      ProcessInstance instance = null;
	      if(request.getProcessDefinitionId() != null) {
	        instance = ActivitiUtil.getRuntimeService().startProcessInstanceById(
	                request.getProcessDefinitionId(), request.getBusinessKey(), startVariables);
	      } else if(request.getProcessDefinitionKey() != null){
	        instance = ActivitiUtil.getRuntimeService().startProcessInstanceByKey(
	                request.getProcessDefinitionKey(), request.getBusinessKey(), startVariables);
	      } else {
	        instance = ActivitiUtil.getRuntimeService().startProcessInstanceByMessage(
	                request.getMessage(), request.getBusinessKey(), startVariables);
	      }
	      
	      // start and initialize Ad-Hoc process
	      RuntimeService rs = ActivitiUtil.getRuntimeService();
	      ProcessInstance shadowInstance = null;
	      shadowInstance = rs.startProcessInstanceByKey(ConstantsCBS.ADHOC_PROCESS_DEFINITION, startVariables);
	      
	      Map<String, Object> variables = new HashMap<String, Object>();
	      variables.put("customerKey", "adhoc");
	      variables.put(ConstantsCBS.ADHOC_PARENT_PROCESS_ID, instance.getId());
	      rs.setVariables(shadowInstance.getId(), variables);
	      
	      // set Ad-Hoc process id in parent process
	      rs.setVariable(instance.getId(), ConstantsCBS.ADHOC_CHILD_PROCESS_ID, shadowInstance.getId());
	      
	      setStatus(Status.SUCCESS_CREATED);
	      return factory.createProcessInstanceResponse(this, instance);
	    } catch(ActivitiObjectNotFoundException aonfe) {
	      throw new ActivitiIllegalArgumentException(aonfe.getMessage(), aonfe);
	    }
	  }
	
}
