package com.cbs.rest.api.process;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.runtime.process.BaseExecutionVariableResource;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import com.cbs.rest.api.utility.ConstantsCBS;


/**
 * 
 * Update customer keys(contact uids) of single or multiple processes
 *
 */
public class ProcessInstanceVariableContactResource extends BaseExecutionVariableResource {
	
	@Post
	public Object updateProcessVariableByContactUid(Representation representation) {
		if (authenticate() == false) return null;

		// Get "contact uid" from Rest URL
		String contactuid = getAttribute("contactuid");
		if (contactuid == null) {
			throw new ActivitiIllegalArgumentException("The contact uid cannot be null");
		}
		
		// Get "action" from Rest URL
		String action = getAttribute("action");
		if (action == null) {
			throw new ActivitiIllegalArgumentException("The action against process cannot be null");
		}
        
		// Result to be returned in JSON format
		Object result = null;
		
		try {
			String bodyParams = representation.getText();
			JsonNode bodyJson = new ObjectMapper().readTree(bodyParams);
			String processIds = bodyJson.path("processIds").getTextValue();
			
			Set<String> processIdsSet = new HashSet<String>(Arrays.asList(processIds.split(ConstantsCBS.SEPERATOR_COMMA)));
			
			RuntimeService rs = ActivitiUtil.getRuntimeService();

			List<ProcessInstance> processes = rs.createProcessInstanceQuery()
												.processInstanceIds(processIdsSet).list();

			for(ProcessInstance process: processes) {
				rs.setVariable(process.getId(), "customerKey", contactuid);
			}
			
		} catch (IOException ioe) {
		throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, ioe);
	}

		return result;
	}

}
