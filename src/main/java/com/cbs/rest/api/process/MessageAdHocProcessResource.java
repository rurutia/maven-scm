package com.cbs.rest.api.process;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.Execution;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import com.cbs.rest.api.utility.ConstantsCBS;
import com.cbs.rest.api.utility.Utility;

/**
 * 
 * Create Ad-hoc activity(task or subprocess) when message is received.
 * Activity type is decided by process variable 
 *
 */
public class MessageAdHocProcessResource extends SecuredResource {

	private final static String NEW_ACTIVITY_TYPE = "newActivityType";
	
	@Post
	public ObjectNode createAdHocProcessActivity(Representation entity) {
		String adhocProcessId = (String) getRequest().getAttributes().get("adhocProcessId");

		if(adhocProcessId == null) {
			throw new ActivitiIllegalArgumentException("No adhocProcessId provided");
		}

		RuntimeService rs = ActivitiUtil.getRuntimeService();

		// get Ad-hoc process instance
		HistoricProcessInstance instance = (HistoricProcessInstance)ActivitiUtil.getHistoryService()
				.createHistoricProcessInstanceQuery()
				.processInstanceId(adhocProcessId)
				.singleResult();

		if (instance == null) {
			throw new ActivitiObjectNotFoundException("Ad-ho cprocess with id:" + adhocProcessId + " could not be found", Execution.class);
		}


		try {
			String startParams = entity.getText();
			JsonNode startJSON = new ObjectMapper().readTree(startParams);
			
			// get calling process from POST request body
			if( !startJSON.has("callingProcessId") )
				throw new ActivitiException("callingProcessId must be specified");
			String callingProcessId = startJSON.get("callingProcessId").asText(); 
			
			// activity is created by Ad-Hoc process or its subprocess itself(not main process or subprocess of main process), 
			// traverse to get top-level Ad-Hoc process id 
			if(callingProcessId.equals(adhocProcessId)) {
				while(instance.getSuperProcessInstanceId() != null) {
					adhocProcessId = instance.getSuperProcessInstanceId();
					instance = (HistoricProcessInstance)ActivitiUtil.getHistoryService()
							.createHistoricProcessInstanceQuery()
							.processInstanceId(adhocProcessId)
							.singleResult();
				}
			}
			
			// get execution of intermediate message event in Ad-hoc process
			Execution messageExecution = rs
					.createExecutionQuery()
					.processInstanceId(adhocProcessId)
					.messageEventSubscriptionName(ConstantsCBS.MESSAGE_CREATE_ADHOC_ACTIVITY)
					.singleResult();

			// get calling process tree (breadcrumb) and passed to Ad-Hoc process as process var which will then be passed to new activity 
			String callingProcessTreeId = getProcessTreeId(callingProcessId);
			rs.setVariable(adhocProcessId, ConstantsCBS.ADHOC_CALLING_PROCESS_TREE_ID, callingProcessTreeId);
			
			// create activity - task or process
			if( !startJSON.has(NEW_ACTIVITY_TYPE) )
				throw new ActivitiException("New activity type must be specified");
			String activityType = startJSON.get(NEW_ACTIVITY_TYPE).asText();
			
			// task assignee required for new task
			if(startJSON.has("newTaskAssignee") && !"unassigned".equals(startJSON.get("newTaskAssignee").asText())) {
				rs.setVariable(adhocProcessId, "newTaskAssignee", startJSON.get("newTaskAssignee").asText());
			}
			else {
				rs.removeVariable(adhocProcessId, "newTaskAssignee");
			}
			
			// task name required for new task
			if("task".equals(activityType)) {
				if(!startJSON.has("newTaskName"))
					throw new ActivitiException("newTaskName must be specified");
				rs.setVariable(adhocProcessId, "newTaskName", startJSON.get("newTaskName").asText());
			}
			
			// process definition key required for new process
			if("subprocess".equals(activityType)) {
				if(!startJSON.has("newProcessDefinitionKey"))
					throw new ActivitiException("newProcessDefinitionKey must be specified");
				rs.setVariable(adhocProcessId, "newProcessDefinitionKey", startJSON.get("newProcessDefinitionKey").asText());
				
				String newProcessNote = null;
				if(!startJSON.has("newProcessNote"))
					newProcessNote = "";
				else 
					newProcessNote = startJSON.get("newProcessNote").asText();
				rs.setVariable(adhocProcessId, "newProcessNote", newProcessNote);
			}
				
			// Ad-Hoc process uses NEW_ACTIVITY_TYPE to perform corresponding action after message is received
			rs.setVariable(adhocProcessId, NEW_ACTIVITY_TYPE, activityType);
			rs.messageEventReceived(ConstantsCBS.MESSAGE_CREATE_ADHOC_ACTIVITY, messageExecution.getId());

		} catch (Exception e) {
			if(e instanceof ActivitiException) {
				throw (ActivitiException) e;
			}
			throw new ActivitiException("Message can not be sent to process instance id " + adhocProcessId);
		}

		ObjectNode successNode = new ObjectMapper().createObjectNode();
		successNode.put("success", true);
		return successNode;
	}

	private String getProcessTreeId(String processId){
		String pid = processId;
		List<String> processNodes = new ArrayList<String>();
		HistoricProcessInstance processInstance = null;
		do {
			processInstance = ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery()
					.processInstanceId(pid)
					.singleResult();
			
			String processDefinitionName = Utility.getProcessDefinitionNameFromDefinitionId(processInstance.getProcessDefinitionId());
			processNodes.add(0, processDefinitionName + "-" + processInstance.getId() + "-" + processInstance.getEndTime());
			pid = processInstance.getSuperProcessInstanceId();
		} while(pid != null);
		
		return StringUtils.join(processNodes, ">");
	}
}

