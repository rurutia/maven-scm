package com.cbs.rest.api.task;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.activiti.engine.task.Comment;

/**
 * CBS implementation of default Comment class to provide 
 * formatted date and time instead of default Epoch time
 *
 */
public class TaskComment implements Comment {
	
	private String id;
	private String userId;
	private String taskId;
	private String timeFormatted;
	private Date time;
	private String processInstanceId;
	private String message;
	
	public TaskComment(Comment comment) {
		setId(comment.getId());
		setUserId(comment.getUserId());
		setTaskId(comment.getTaskId());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        setTimeFormatted(formatter.format(comment.getTime()));
        setTime(comment.getTime());
        setProcessInstanceId(comment.getProcessInstanceId());
        setMessage(comment.getFullMessage());
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getTimeFormatted() {
		return timeFormatted;
	}
	public void setTimeFormatted(String timeFormatted) {
		this.timeFormatted = timeFormatted;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public String getProcessInstanceId() {
		return processInstanceId;
	}
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getFullMessage() {
		return this.message;
	}

}