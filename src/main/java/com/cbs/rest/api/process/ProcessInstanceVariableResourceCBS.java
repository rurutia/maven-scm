package com.cbs.rest.api.process;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

public class ProcessInstanceVariableResourceCBS extends SecuredResource {
  
  @Post
  public ProcessInstanceVariableResponseCBS setInstanceVariable(Representation entity) {
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
    	RuntimeService rs = ActivitiUtil.getRuntimeService();
    	Map<String, String> variablesMap = new HashMap<String, String>();
		String startParams = entity.getText();
		JsonNode startJSON = new ObjectMapper().readTree(startParams);
		Iterator<String> keys = startJSON.getFieldNames();
		while( keys.hasNext() ) {
			String key = keys.next();
			String value = startJSON.path(key).getTextValue();
		    rs.setVariable(processInstanceId, key, value);
		    variablesMap.put(key, value);
		}
		ProcessInstanceVariableResponseCBS response = new ProcessInstanceVariableResponseCBS(variablesMap);
		return response;
	} catch (Exception e) {
		 if(e instanceof ActivitiException) {
			 throw (ActivitiException) e;
		  }
		 throw new ActivitiException("Process instance with id " + processInstanceId + " can not take specified variables");
	}
    
  }
}
