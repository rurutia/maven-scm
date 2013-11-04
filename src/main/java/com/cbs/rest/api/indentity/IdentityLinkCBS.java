package com.cbs.rest.api.indentity;

import org.activiti.engine.task.IdentityLink;

public class IdentityLinkCBS implements IdentityLink {
	
	String type;
	String userId;
	String groupId;
	String taskId;
	String processDefinitionId;
	String processInstanceId;
	
	public IdentityLinkCBS(String type, String userId, String groupId, String taskId, String processDefinitionId, String processInstanceId) {
		this.type = type;
		this.userId = userId;
		this.groupId = groupId;
		this.taskId = taskId;
		this.processDefinitionId = processDefinitionId;
		this.processInstanceId = processInstanceId;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getProcessDefinitionId() {
		return processDefinitionId;
	}
	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}
	public String getProcessInstanceId() {
		return processInstanceId;
	}
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

}
