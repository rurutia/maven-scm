package com.cbs.rest.api.process;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

public class MessageReceivedResource extends SecuredResource {
	 
	  @Post
	  public ObjectNode getProcessInstances(Representation entity) {
		String executionId = (String) getRequest().getAttributes().get("executionId");
	    
	    if(executionId == null) {
	      throw new ActivitiIllegalArgumentException("No execution id provided");
	    }

	    ExecutionEntity pi = (ExecutionEntity) ActivitiUtil.getRuntimeService().createExecutionQuery()
	    						.executionId(executionId).singleResult();

	    if (pi == null) {
	      throw new ActivitiObjectNotFoundException("Execution with id" + executionId + " could not be found", Execution.class);
	    }
	    
	    try {
	    	Map<String, Object> variablesMap = new HashMap<String, Object>();
			String startParams = entity.getText();
			JsonNode startJSON = new ObjectMapper().readTree(startParams);
			if( !startJSON.has("messageName") )
				throw new ActivitiException("messageName must be specified");
			String messageName = startJSON.get("messageName").asText();
			Iterator<String> keys = startJSON.getFieldNames();
			while( keys.hasNext() ) {
				String key = keys.next();
				if( !key.equals("signalRef") ) {
					String value = startJSON.path(key).getTextValue();
				    variablesMap.put(key, value);
				}
			}

		    ActivitiUtil.getRuntimeService().messageEventReceived(messageName, executionId);
			
		} catch (Exception e) {
			 if(e instanceof ActivitiException) {
				 throw (ActivitiException) e;
			  }
			 throw new ActivitiException("Message can not be sent to process instance id " + executionId);
		}
	    
	    ObjectNode successNode = new ObjectMapper().createObjectNode();
	    successNode.put("success", true);
	    return successNode;
	  }
	  
}

