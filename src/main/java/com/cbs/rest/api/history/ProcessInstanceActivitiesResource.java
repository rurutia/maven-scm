package com.cbs.rest.api.history;


import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.application.ActivitiRestServicesApplication;
import org.restlet.resource.Get;

import com.cbs.rest.api.history.HistoricProcessInstanceResponseCBS;
import com.cbs.rest.api.process.ProcessInstancesResponseCBS;

/**
 * 
 * get single CBS customized historic process instance
 * together with task list and subprocess list
 *
 */
public class ProcessInstanceActivitiesResource extends SecuredResource {
  
	@Get
//	public HistoricProcessInstanceResponseCBS getProcessInstance() {
	public ProcessInstancesResponseCBS getProcessInstance() {
		if(!authenticate()) {
			return null;
		}
		
//		HistoricProcessInstanceResponseCBS response = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactoryCBS()
//				.createHistoricProcessInstanceResponseCBS(this, getHistoricProcessInstanceFromRequest()); 
		
		ProcessInstancesResponseCBS response = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactoryCBS()
				.createHistoricProcessInstanceResponseCBS(this, getHistoricProcessInstanceFromRequest()); 
		
		return response;
	}
	
	protected HistoricProcessInstance getHistoricProcessInstanceFromRequest() {
		String processInstanceId = getAttribute("processInstanceId");
		if (processInstanceId == null) {
			throw new ActivitiIllegalArgumentException("The processInstanceId cannot be null");
		}

		HistoricProcessInstance processInstance = ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery()
				.processInstanceId(processInstanceId).singleResult();
		if (processInstance == null) {
			throw new ActivitiObjectNotFoundException("Could not find a process instance with id '" + processInstanceId + "'.", ProcessInstance.class);
		}
		return processInstance;
	}
  
}

