package com.cbs.rest.api.indentity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineInfo;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.GroupQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

/**
 * Returns information of user and user groups
 * 
 */
public class LoginUserAndGroupInfoResource extends SecuredResource {

	@Post
	public LoginUserAndGroupInfoResponse login(Representation entity) throws IOException {
		// Get request post body in JSON format
	    String taskParams = entity.getText();
	    JsonNode taskJSON = new ObjectMapper().readTree(taskParams);
	    
		// Extract "userId" and "password" from JSON string
	    String userId = taskJSON.path("userId").getTextValue();
	    String password = taskJSON.path("password").getTextValue();
	    
		if(userId == null) {
			throw new ActivitiIllegalArgumentException("No user id supplied");
		}
		
		if(password == null) {
			throw new ActivitiIllegalArgumentException("No password supplied");
		}
	
		ProcessEngine pe = ActivitiUtil.getProcessEngine();
		
		if (pe != null) {
		    // Check userId against password 
			if (pe.getIdentityService().checkPassword(userId, password) == false) {
				throw new ActivitiException("Username and password does not match.");
			}
			
			// Set user info in response
		    User user = ActivitiUtil.getIdentityService().createUserQuery().userId(userId).singleResult();
		    LoginUserAndGroupInfoResponse loginResponse = new LoginUserAndGroupInfoResponse();
		    loginResponse.setSuccess(true);
		    loginResponse.setUserInfo(new UserInfo(user));
		    
		    // Set user group info in response
		    Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
		    properties.put("id", GroupQueryProperty.GROUP_ID);
		    properties.put("name", GroupQueryProperty.NAME);
		    properties.put("type", GroupQueryProperty.TYPE);
		    
		    Object groups = new UserGroupsPaginateList().paginateList(
		            getQuery(), ActivitiUtil.getIdentityService().createGroupQuery()
		                .groupMember(userId), "id", properties).getData();
			loginResponse.setGroups(groups);
			return loginResponse;
	
		} else {
			// error handling
			String message;
			ProcessEngineInfo pei = ActivitiUtil.getProcessEngineInfo();
			if (pei != null) {
				message = pei.getException();
			}
			else {
				message = "Can't find process engine which is needed to authenticate username and password.";
				List<ProcessEngineInfo> processEngineInfos = ProcessEngines.getProcessEngineInfos();
				if (processEngineInfos.size() > 0) {
					message += "\nHowever " + processEngineInfos.size() + " other process engine(s) were found: ";
				}
				for (ProcessEngineInfo processEngineInfo : processEngineInfos)
				{
					message += "Process engine '" + processEngineInfo.getName() + "' (" + processEngineInfo.getResourceUrl() + "):";
					if (processEngineInfo.getException() != null) {
						message += processEngineInfo.getException();
					}
					else {
						message += "OK";
					}
				}
			}
			throw new ActivitiException(message);
		}
	}
}
