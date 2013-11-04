package com.cbs.rest.api.process;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

/**
 * Send signal to a process instance.
 * In request body, "signalRef" must be specified, process variables are optional.
 *
 */
public class SignalReceivedResource extends SecuredResource {
	 
	  @Post
	  public ObjectNode getProcessInstances(Representation entity) {
	  String processInstanceId = (String) getRequest().getAttributes().get("processInstanceId");
	    
	    if(processInstanceId == null) {
	      throw new ActivitiIllegalArgumentException("No process instance id provided");
	    }

	    ExecutionEntity pi = (ExecutionEntity) ActivitiUtil.getRuntimeService().createProcessInstanceQuery()
	        .processInstanceId(processInstanceId).singleResult();

	    if (pi == null) {
	      throw new ActivitiObjectNotFoundException("Process instance with id" + processInstanceId + " could not be found", ProcessInstance.class);
	    }
	    
	    try {
	    	Map<String, Object> variablesMap = new HashMap<String, Object>();
			String startParams = entity.getText();
			JsonNode startJSON = new ObjectMapper().readTree(startParams);
			if( !startJSON.has("signalRef") )
				throw new ActivitiException("signalRef must be specified");
			String signalRef = startJSON.get("signalRef").asText();
			Iterator<String> keys = startJSON.getFieldNames();
			while( keys.hasNext() ) {
				String key = keys.next();
				if( !key.equals("signalRef") ) {
					String value = startJSON.path(key).getTextValue();
				    variablesMap.put(key, value);
				}
			}
			
	    	RuntimeService rs = ActivitiUtil.getRuntimeService();
			List<Execution>  executionList = ActivitiUtil.getRuntimeService()
						.createExecutionQuery()
						.processInstanceId(processInstanceId)
						.signalEventSubscriptionName(signalRef)
						.list();
			
			if( executionList.size() == 0 )
				throw new ActivitiException("No execution subscribed to signal reference " + signalRef + " is found");
			
			for(Execution execution : executionList) {
				if( variablesMap.isEmpty() ) 
					rs.signalEventReceived(signalRef, execution.getId());
			    else
					rs.signalEventReceived(signalRef, execution.getId(), variablesMap);
		    }
			
		} catch (Exception e) {
			 if(e instanceof ActivitiException) {
				 throw (ActivitiException) e;
			  }
			 throw new ActivitiException("Signal can not be sent to process instance id " + processInstanceId);
		}
	    
	    ObjectNode successNode = new ObjectMapper().createObjectNode();
	    successNode.put("success", true);
	    return successNode;
	  }
	  
}
