package com.cbs.persistence.domain;

import java.util.Date;

/**
 * @author dev
 *
 */
public class ProcessComment {
	
	private String id;
	private String type = "processComment"; 
	private Date time;
	private String userId;
//	private String taskId;
	private String processInstanceId;
	private String action = "addProcessComment";
	private String message;
//	private String full_message;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getProcessInstanceId() {
		return processInstanceId;
	}
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	public String getAction() {
		return action;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
