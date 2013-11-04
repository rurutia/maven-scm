package com.cbs.rest.api.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.api.ActivitiUtil;
import org.apache.commons.lang.StringUtils;

import com.cbs.rest.api.history.HistoricActivityInstanceCBS;
import com.cbs.rest.api.task.HistoricTaskResponse;
import com.cbs.rest.api.task.TaskResponseCBS;
import com.cbs.rest.api.utility.ConstantsCBS;
import com.cbs.rest.api.utility.Utility;

/**
 * CBS customized Process instance response
 *
 */
public class ProcessInstancesResponseCBS implements Serializable {

	private static final long serialVersionUID = 1L;

	String id;
	String businessKey;
	String processDefinitionId;
	Date startTime;
	Date endTime;
	String startUserId;
	String deleteReason;
	String superProcessId;
	List<HistoricTaskResponse> tasks;
	List<TaskResponseCBS> activeTasks;
	Map<String, String> variables;
	int totalProcessComments;
	
	List<HistoricActivityInstance> subprocesses;
	String processTreeId;
	String processResponseType;
	
	ProcessInstancesResponseCBS adhocProcess;

	public ProcessInstancesResponseCBS(HistoricProcessInstance processInstance) {
		this.setId(processInstance.getId());
		this.setBusinessKey(processInstance.getBusinessKey());
		this.setStartTime(processInstance.getStartTime());
		this.setEndTime(processInstance.getEndTime());
		this.setProcessDefinitionId(processInstance.getProcessDefinitionId());
		this.setStartUserId(processInstance.getStartUserId());
		this.setDeleteReason(processInstance.getDeleteReason());
		this.setSuperProcessId(processInstance.getSuperProcessInstanceId());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBusinessKey() {
		return businessKey;
	}

	public void setBusinessKey(String businessKey) {
		this.businessKey = businessKey;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public String getStartUserId() {
		return startUserId;
	}

	public void setStartUserId(String startUserId) {
		this.startUserId = startUserId;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getDeleteReason() {
		return deleteReason;
	}

	public void setDeleteReason(String deleteReason) {
		this.deleteReason = deleteReason;
	}

	//  public List<HistoricTaskResponse> getTasks() {
		//	  return tasks;
		//  }

	public Object getTasks() {
		if(this.tasks != null)
			return this.tasks;
		else
			return this.activeTasks;
	}

	public void setTasks(List<HistoricTaskResponse> tasks) {
		this.tasks = tasks;
	}

	public void setActiveTasks(List<TaskResponseCBS> activeTasks) {
		this.activeTasks = activeTasks;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}

	public int getTotalProcessComments() {
		return totalProcessComments;
	}

	public void setTotalProcessComments(int totalProcessComments) {
		this.totalProcessComments = totalProcessComments;
	}

	public List<HistoricActivityInstanceCBS> getSubprocesses() {
		List<HistoricActivityInstanceCBS> list = new ArrayList<HistoricActivityInstanceCBS> ();
		if(subprocesses != null) {
			for(HistoricActivityInstance instance: subprocesses) {
				HistoricActivityInstanceCBS activityCBS = new HistoricActivityInstanceCBS(instance);	
				
			    if(getProcessResponseType() != null 
			    		&& (getProcessResponseType().equals(ConstantsCBS.PROCESS_RESPONSE_TYPE_ADHOC_HISTORIC)
			    		|| getProcessResponseType().equals(ConstantsCBS.PROCESS_RESPONSE_TYPE_ADHOC_ACTIVE)
			    	    || getProcessResponseType().equals(ConstantsCBS.PROCESS_RESPONSE_TYPE_SINGLE_HISTORIC)
			    				)){
			    	String callingProcessTreeId  = null;
			    	HistoricVariableInstance variableInstance = ActivitiUtil.getHistoryService().createHistoricVariableInstanceQuery()
			    			.processInstanceId(activityCBS.getCalledProcessInstanceId())
			    			.variableName(ConstantsCBS.ADHOC_CALLING_PROCESS_TREE_ID)
			    			.singleResult();
			    	if(variableInstance != null)
			    		callingProcessTreeId = (String)variableInstance.getValue();
			    	activityCBS.setCallingProcessTreeId(callingProcessTreeId);
			    }
				
				list.add(activityCBS);
			}
		}
		return list;
	}

	public void setSubprocesses(List<HistoricActivityInstance> subprocesses) {
		this.subprocesses = subprocesses;
	}

	public String getSuperProcessId() {
		return superProcessId;
	}

	public void setSuperProcessId(String superProcessId) {
		this.superProcessId = superProcessId;
	}

	public String getProcessTreeId() {
		if(this.processTreeId == null){
			List<String> processNodes = new ArrayList<String>();
			if(this.superProcessId != null) {
				// process breadcrumb
				HistoricProcessInstance processInstance = ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery()
						.processInstanceId(getId())
						.singleResult();
				while(processInstance.getSuperProcessInstanceId() != null) {
					processInstance = ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery()
							.processInstanceId(processInstance.getSuperProcessInstanceId())
							.singleResult();
					String processDefinitionName = Utility.getProcessDefinitionNameFromDefinitionId(processInstance.getProcessDefinitionId());
					String isProcessFinished = processInstance.getEndTime() == null ? "null" : "finished";
					processNodes.add(0, processDefinitionName + "-" + processInstance.getId() + "-" + isProcessFinished);
				} 
				
				// Calling(Creating adhoc) process breadcrumb
				HistoricVariableInstance variableInstance = ActivitiUtil.getHistoryService().createHistoricVariableInstanceQuery()
						.processInstanceId(getId())
						.variableName(ConstantsCBS.ADHOC_CALLING_PROCESS_TREE_ID)
						.singleResult();
				if(variableInstance != null) {
					String[] callingProcessTree = ((String)variableInstance.getValue()).split(">");
					// check if every process in calling porcess tree is active 
					for(int i = callingProcessTree.length-1; i >= 0; i--) {
						String processId = callingProcessTree[i].split("-")[1];
						ProcessInstance instance = ActivitiUtil.getRuntimeService().createProcessInstanceQuery()
								.processInstanceId(processId).singleResult();
						if(instance == null && !processNodes.contains(callingProcessTree[i].replace("null", "finished"))) 
							processNodes.add(0, callingProcessTree[i].replace("null", "finished"));
						else if(instance != null && !processNodes.contains(callingProcessTree[i]))
							processNodes.add(0, callingProcessTree[i]);
					}
				}
			}
			else {
				// top level process created by other process needs to be breadcrumbed (e.g. Ad-hoc)
				HistoricVariableInstance variableInstance = ActivitiUtil.getHistoryService().createHistoricVariableInstanceQuery()
						.processInstanceId(getId())
						.variableName(ConstantsCBS.ADHOC_PARENT_PROCESS_ID)
						.singleResult();
				if(variableInstance != null) {
					String topLevelProcessId = (String)variableInstance.getValue();
					HistoricProcessInstance processInstance = ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery()
							.processInstanceId(topLevelProcessId)
							.singleResult();
					String processDefinitionName = Utility.getProcessDefinitionNameFromDefinitionId(processInstance.getProcessDefinitionId());
					String isProcessFinished = processInstance.getEndTime() == null ? "null" : "finished";
					String topLevelProcessDetail = processDefinitionName + "-" + processInstance.getId() + "-" + isProcessFinished;
					if(!processNodes.contains(topLevelProcessDetail))
						processNodes.add(0, topLevelProcessDetail);
				}
			}
			if(!processNodes.isEmpty())
				return StringUtils.join(processNodes, ">");
		}

		return this.processTreeId;
	}
	
	public String getProcessResponseType() {
		return processResponseType;
	}

	public void setProcessResponseType(String processResponseType) {
		this.processResponseType = processResponseType;
	}

	public void setProcessTreeId(String processTreeId) {
		this.processTreeId = processTreeId;
	}

	public ProcessInstancesResponseCBS getAdhocProcess() {
		return adhocProcess;
	}

	public void setAdhocProcess(ProcessInstancesResponseCBS adhocProcess) {
		this.adhocProcess = adhocProcess;
	}

}