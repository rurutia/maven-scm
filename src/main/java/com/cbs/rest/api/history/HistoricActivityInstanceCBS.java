package com.cbs.rest.api.history;

import java.util.Date;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.rest.api.ActivitiUtil;

/**
 * 
 * Customized Activity Instance Response to return
 * extra information such as calledProcessInstanceName
 *
 */
public class HistoricActivityInstanceCBS implements HistoricActivityInstance{
	
	HistoricActivityInstance activityInstance;
	
	protected String calledProcessInstanceName;
	protected String callingProcessTreeId;
	
	public HistoricActivityInstanceCBS(HistoricActivityInstance activityInstance) {
		this.activityInstance = activityInstance;
	}
	
	public String getCalledProcessInstanceName() {
		HistoricProcessInstance historicProcessInstance =  ActivitiUtil.getHistoryService()
														    .createHistoricProcessInstanceQuery()
															.processInstanceId(activityInstance.getCalledProcessInstanceId())
															.singleResult();
		String calledProcessInstanceDefinitionId;
		
		if(historicProcessInstance != null) {
			calledProcessInstanceDefinitionId = historicProcessInstance.getProcessDefinitionId();
		}
		else
			return "process cannot be found";
		
		return ActivitiUtil.getRepositoryService()
				.createProcessDefinitionQuery()
				.processDefinitionId(calledProcessInstanceDefinitionId)
				.singleResult()
				.getName();
	}

	public String getCallingProcessTreeId() {
		return callingProcessTreeId;
	}

	public void setCallingProcessTreeId(String callingProcessTreeId) {
		this.callingProcessTreeId = callingProcessTreeId;
	}

	@Override
	public String getId() {
		return activityInstance.getId();
	}

	@Override
	public String getActivityId() {
		return activityInstance.getActivityId();
	}

	@Override
	public String getActivityName() {
		return activityInstance.getActivityName();
	}

	@Override
	public String getActivityType() {
		return activityInstance.getActivityType();
	}

	@Override
	public String getProcessDefinitionId() {
		return activityInstance.getProcessDefinitionId();
	}

	@Override
	public String getProcessInstanceId() {
		return activityInstance.getProcessInstanceId();
	}

	@Override
	public String getExecutionId() {
		return activityInstance.getExecutionId();
	}

	@Override
	public String getTaskId() {
		return activityInstance.getTaskId();
	}

	@Override
	public String getCalledProcessInstanceId() {
		return activityInstance.getCalledProcessInstanceId();
	}

	@Override
	public String getAssignee() {
		return activityInstance.getAssignee();
	}

	@Override
	public Date getStartTime() {
		return activityInstance.getStartTime();
	}

	@Override
	public Date getEndTime() {
		return activityInstance.getEndTime();
	}

	@Override
	public Long getDurationInMillis() {
		return activityInstance.getDurationInMillis();
	}

}
