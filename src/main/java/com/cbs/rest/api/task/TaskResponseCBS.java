package com.cbs.rest.api.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.task.Comment;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.engine.RestIdentityLink;
import org.activiti.rest.api.runtime.task.TaskResponse;

/**
 * CBS customized active task response
 * share same interface with historic task response
 * @link HistoricTaskResponse
 */
public class TaskResponseCBS extends TaskResponse{

	private static final Map<Integer, String> PRIORITIES;
    static
    {
    	PRIORITIES = new HashMap<Integer, String>();
    	PRIORITIES.put(25, "LOW");
    	PRIORITIES.put(50, "NORMAL");
    	PRIORITIES.put(75, "HIGH");
    	PRIORITIES.put(100, "EMERGENCY");
    }
    
    // CBS additional task fields
    protected String formKey;
    protected Date endTime;
    protected String priorityReadable;
    protected Date assignmentTime;
    protected Date inProgressTime;
    protected Date canStartTime;
    protected boolean isDeadline;
    protected Date priorityChangeTime;
    protected int priorityFuture;
    protected String priorityFutureReadable;
    protected int timerCount;
    protected List<Comment> comments;
	protected List<TaskAction> log;
	protected int totalProcessComments;
	protected String subject;
    protected boolean isWaiting;
    protected String waitingReason;
    protected String callingProcessTreeId;
    protected String processDeploymentId;

    protected Map<String,String> variableMap;
    List<RestIdentityLink> identityLinks;

	public TaskResponseCBS(Task task) {
		super(task);
	}

	public String getFormKey() {
		String key = null;
		if(this.endTime == null)
			key = ActivitiUtil.getFormService().getTaskFormData(this.getId()).getFormKey();
		return key;
	}

	public void setFormKey(String formKey) {
		this.formKey = formKey;
	}

	public String getPriorityReadable() {
		return PRIORITIES.get(this.priority);
	}

	public void setPriorityReadable(String priorityReadable) {
		this.priorityReadable = priorityReadable;
		this.setPriority(convertPriorityReadableToNum(priorityReadable));
	}

	public static int convertPriorityReadableToNum(String priorityReadable) {
		int priority = 50;
		if(!PRIORITIES.containsValue(priorityReadable.toUpperCase())) {
			return -1;
		}
		for(Object key : PRIORITIES.keySet()) {
			if(PRIORITIES.get(key).equals(priorityReadable.toUpperCase())) {
				priority = (Integer)key;
				return priority;
			}
		}
		return priority;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getAssignmentTime() {
		return assignmentTime;
	}

	public void setAssignmentTime(Date assignmentTime) {
		this.assignmentTime = assignmentTime;
	}

	public Date getInProgressTime() {
		return inProgressTime;
	}

	public void setInProgressTime(Date inProgressTime) {
		this.inProgressTime = inProgressTime;
	}

	public Date getCanStartTime() {
		return canStartTime;
	}

	public void setCanStartTime(Date canStartTime) {
		this.canStartTime = canStartTime;
	}

	public boolean isDeadline() {
		return isDeadline;
	}

	public void setDeadline(boolean isDeadline) {
		this.isDeadline = isDeadline;
	}

	public Date getPriorityChangeTime() {
		return priorityChangeTime;
	}

	public void setPriorityChangeTime(Date priorityChangeTime) {
		this.priorityChangeTime = priorityChangeTime;
	}

	public int getPriorityFuture() {
		return priorityFuture;
	}

	public void setPriorityFuture(int priorityFuture) {
		this.priorityFuture = priorityFuture;
	}

	public String getPriorityFutureReadable() {
		return PRIORITIES.get(this.priorityFuture);
	}

	public void setPriorityFutureReadable(String priorityFutureReadable) {
		this.priorityFutureReadable = priorityFutureReadable;
		this.setPriority(convertPriorityReadableToNum(priorityFutureReadable));
	}

	public int getTimerCount() {
		return timerCount;
	}

	public void setTimerCount(int timerCount) {
		this.timerCount = timerCount;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = new ArrayList<Comment>();
		for(Comment comment : comments ){
			this.comments.add(new TaskComment(comment));
		}
	}

	public List<TaskAction> getLog() {
		return log;
	}

	public void setLog(List<TaskAction> log) {
		this.log = log;
	}

	public int getTotalProcessComments() {
		return totalProcessComments;
	}

	public void setTotalProcessComments(int totalProcessComments) {
		this.totalProcessComments = totalProcessComments;
	}

	public Map<String, String> getVariableMap() {
		return variableMap;
	}

	public void setVariableMap(Map<String, String> variableMap) {
		this.variableMap = variableMap;
	}

	public List<RestIdentityLink> getIdentityLinks() {
		return identityLinks;
	}

	public void setIdentityLinks(List<RestIdentityLink> identityLinks) {
		this.identityLinks = identityLinks;
	}

	public String getSubject() {
		String subject = null;
		if(this.getDescription() != null && !this.getDescription().equals(""))
			subject = this.getDescription().split("\n")[0];
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public boolean isWaiting() {
		return isWaiting;
	}

	public void setWaiting(boolean isWaiting) {
		this.isWaiting = isWaiting;
	}

	public String getWaitingReason() {
		return waitingReason;
	}

	public void setWaitingReason(String waitingReason) {
		this.waitingReason = waitingReason;
	}

	public String getCallingProcessTreeId() {
		return callingProcessTreeId;
	}

	public void setCallingProcessTreeId(String callingProcessTreeId) {
		this.callingProcessTreeId = callingProcessTreeId;
	}

	public String getProcessDeploymentId() {
		return processDeploymentId;
	}

	public void setProcessDeploymentId(String processDeploymentId) {
		this.processDeploymentId = processDeploymentId;
	}
	
}