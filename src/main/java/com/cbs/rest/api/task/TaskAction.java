package com.cbs.rest.api.task;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * Track actions performed against a task
 * User action such as changing "Due date", "Priority"
 * are saved as process variable
 *
 */

public class TaskAction implements Serializable{
	
	public static String TYPE_DUE_DATE = "due date";
	public static String TYPE_PRIORITY = "priority";
	public static String TYPE_CAN_START_TIME = "can start date";
	public static String TYPE_CHANGE_ASSIGNMENT = "assignment";
	
	private static final long serialVersionUID = 1L;
	
	private Date createTime;
	private String userId;
	private String type;
	private String value;
	
	public TaskAction() {
	}
	
	public TaskAction(Date createTime, String userId, String type, String value) {
		this.createTime = createTime;
		this.userId = userId;
		this.type = type;
		this.value = value;
	}
	
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

}
