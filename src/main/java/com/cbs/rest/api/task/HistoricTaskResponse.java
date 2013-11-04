package com.cbs.rest.api.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.DelegationState;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.engine.RestIdentityLink;
import org.activiti.rest.api.engine.variable.RestVariable;

/**
 * 
 * CBS customized historic task 
 * share same interface with active task response
 * @link TaskResponseCBS
 *
 */
public class HistoricTaskResponse {
	 protected String id;
	  protected String url;
	  protected String owner;
	  protected String assignee;
	  protected String name;
	  protected String description;
	  protected Date createTime;
	  protected Date endTime;
	  protected Date dueDate;
	  protected int priority;
	  protected String taskDefinitionKey;
      protected String subject;

	  
	  // References to other resources
	  protected String parentTaskId;
	  protected String parentTaskUrl;
	  protected String executionId;
	  protected String executionUrl;
	  protected String processInstanceId;
	  protected String processInstanceUrl;
	  protected String processDefinitionId;
	  protected String processDefinitionUrl;
	  
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
	  protected Date startTime;
	  protected String priorityReadable;
	  protected Date assignmentTime;
	  protected Date inProgressTime;
	  protected Date canStartTime;
	  protected boolean isWaiting;
      protected String waitingReason;
      protected String callingProcessTreeId;
      protected String processDeploymentId;
      
	  protected List<Comment> comments;
	  protected List<TaskAction> log;
	  
	  protected List<RestVariable> variables = new ArrayList<RestVariable>();
	  protected Map<String,String> variableMap;
	  protected List<RestIdentityLink> identityLinks;
	  
	  public HistoricTaskResponse(HistoricTaskInstance task) {
		  setId(task.getId());
		  setOwner(task.getOwner());
		  setAssignee(task.getAssignee());
		  setName(task.getName());
		  setDescription(task.getDescription());
		  setCreateTime(task.getStartTime());
		  setDueDate(task.getDueDate());
		  setPriority(task.getPriority());
		  setTaskDefinitionKey(task.getTaskDefinitionKey());
		  setParentTaskId(task.getParentTaskId());
		  setExecutionId(task.getExecutionId());
		  setProcessInstanceId(task.getProcessInstanceId());
		  setProcessDefinitionId(task.getProcessDefinitionId());
		  setEndTime(task.getEndTime());
		  setFormKey(task.getFormKey());
	  }
	  
	  protected String getDelegationStateString(DelegationState state) {
	    String result = null;
	    if(state != null) {
	      result = state.toString().toLowerCase();
	    }
	    return result;
	  }
	  
	  public String getId() {
	    return id;
	  }
	  public void setId(String id) {
	    this.id = id;
	  }
	  public String getUrl() {
	    return url;
	  }
	  public void setUrl(String url) {
	    this.url = url;
	  }
	  public String getOwner() {
	    return owner;
	  }
	  public void setOwner(String owner) {
	    this.owner = owner;
	  }
	  public String getAssignee() {
	    return assignee;
	  }
	  public void setAssignee(String assignee) {
	    this.assignee = assignee;
	  }
	  public String getName() {
	    return name;
	  }
	  public void setName(String name) {
	    this.name = name;
	  }
	  public String getDescription() {
	    return description;
	  }
	  public void setDescription(String description) {
	    this.description = description;
	  }
	  public Date getCreateTime() {
	    return createTime;
	  }
	  public void setCreateTime(Date createTime) {
	    this.createTime = createTime;
	  }
	  
	  public Date getStartTime() {
		return startTime;
	  }

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getDueDate() {
	    return dueDate;
	  }
	  public void setDueDate(Date dueDate) {
	    this.dueDate = dueDate;
	  }
	  public int getPriority() {
	    return priority;
	  }
	  public void setPriority(int priority) {
	    this.priority = priority;
	  }
	  public String getTaskDefinitionKey() {
	    return taskDefinitionKey;
	  }
	  public void setTaskDefinitionKey(String taskDefinitionKey) {
	    this.taskDefinitionKey = taskDefinitionKey;
	  }

	  public String getParentTaskId() {
	    return parentTaskId;
	  }

	  public void setParentTaskId(String parentTaskId) {
	    this.parentTaskId = parentTaskId;
	  }

	  public String getParentTaskUrl() {
	    return parentTaskUrl;
	  }

	  public void setParentTaskUrl(String parentTaskUrl) {
	    this.parentTaskUrl = parentTaskUrl;
	  }

	  public String getExecutionId() {
	    return executionId;
	  }

	  public void setExecutionId(String executionId) {
	    this.executionId = executionId;
	  }

	  public String getExecutionUrl() {
	    return executionUrl;
	  }

	  public void setExecutionUrl(String executionUrl) {
	    this.executionUrl = executionUrl;
	  }

	  public String getProcessInstanceId() {
	    return processInstanceId;
	  }

	  public void setProcessInstanceId(String processInstanceId) {
	    this.processInstanceId = processInstanceId;
	  }

	  public String getProcessInstanceUrl() {
	    return processInstanceUrl;
	  }

	  public void setProcessInstanceUrl(String processInstanceUrl) {
	    this.processInstanceUrl = processInstanceUrl;
	  }

	  public String getProcessDefinitionId() {
	    return processDefinitionId;
	  }

	  public void setProcessDefinitionId(String processDefinitionId) {
	    this.processDefinitionId = processDefinitionId;
	  }

	  public String getProcessDefinitionUrl() {
	    return processDefinitionUrl;
	  }

	  public void setProcessDefinitionUrl(String processDefinitionUrl) {
	    this.processDefinitionUrl = processDefinitionUrl;
	  }
	  
	  public List<RestVariable> getVariables() {
	    return variables;
	  }
	  
	  public void setVariables(List<RestVariable> variables) {
	    this.variables = variables;
	  }
	  
	  public void addVariable(RestVariable variable) {
	    variables.add(variable);
	  }
	  
		public String getPriorityReadable() {
			return PRIORITIES.get(this.priority);
		}
		
		public void setPriorityReadable(String priorityReadable) {
			this.priorityReadable = priorityReadable;
			this.setPriority(convertPriorityReadableToNum(priorityReadable));
		}
		
		static int convertPriorityReadableToNum(String priorityReadable) {
			int priority = 50;
			for(Object key : PRIORITIES.keySet()) {
				if(PRIORITIES.get(key).equals(priorityReadable.toUpperCase())) {
					priority = (Integer)key;
					return priority;
				}
			}
			return priority;
		}
		
		public Date getCanStartTime() {
			return canStartTime;
		}

		public void setCanStartTime(Date canStartTime) {
			this.canStartTime = canStartTime;
		}
		
		

//		public String getFormKey() {
//			String key = null;
//			if ( ActivitiUtil.getTaskService().createTaskQuery().taskId(this.getId()).singleResult() != null) {
//				key = ActivitiUtil.getFormService().getTaskFormData(this.getId()).getFormKey();
//			}
//			return key;
//		}
//
//		public void setFormKey(String formKey) {
//			this.formKey = formKey;
//		}
		
		public String getFormKey() {
			return formKey;
		}

		public void setFormKey(String formKey) {
			this.formKey = formKey;
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
