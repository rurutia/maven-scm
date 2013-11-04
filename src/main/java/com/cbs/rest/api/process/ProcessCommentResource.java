package com.cbs.rest.api.process;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cbs.persistence.domain.ProcessComment;
import com.cbs.persistence.service.ProcessCommentService;

/**
 * 
 * Process comments
 * Stored in ACT_HI_COMMENT table with task comments
 * DB format:
 * ID_: pc{epoch time of creation}
 * TYPE_: processComment
 * PROC_INST_ID_: process instance id
 * ACTION_: addProcessComment
 *
 */
public class ProcessCommentResource extends SecuredResource{
	
	@Get
	public List<ProcessComment> getProcessComments() {
		if(authenticate() == false) return null;
		
	    String processInstanceId = getAttribute("processInstanceId");
	    if (processInstanceId == null) {
	      throw new ActivitiIllegalArgumentException("The processInstanceId cannot be null");
	    }
	    
	    ApplicationContext ctx = 
				new ClassPathXmlApplicationContext("activiti-context.xml");
		ProcessCommentService service = ctx.getBean("processCommentService",ProcessCommentService.class);

		List<ProcessComment> list = new ArrayList<ProcessComment>();
		
		list = service.findByProcessId(processInstanceId);
		
		return list;
	}
	
	@Post
	public ProcessComment addComment(Representation entity) {
		if(authenticate() == false) return null;

		String processInstanceId = getAttribute("processInstanceId");
		if (processInstanceId == null) {
			throw new ActivitiIllegalArgumentException("The processInstanceId cannot be null");
		}

		ApplicationContext ctx = 
				new ClassPathXmlApplicationContext("activiti-context.xml");
		ProcessCommentService service = ctx.getBean("processCommentService",ProcessCommentService.class);

		String newComment;
		ProcessComment processComment = new ProcessComment();
		try {
			newComment = entity.getText();
			if(newComment != null) {
				String id = "pc" + System.currentTimeMillis()/100;
				processComment.setId(id);
				processComment.setUserId(loggedInUser);
				processComment.setProcessInstanceId(processInstanceId);
				newComment = newComment.substring("{\"comment\":".length()+1, newComment.length()-2);
				processComment.setMessage(newComment);
				service.insertProcessComment(processComment);
				
				processComment = service.findSingleById(id);
			}
		} catch (Exception e) {
			throw new ActivitiException("Failed to add comment for process:" + processInstanceId, e);
		}
		
		return processComment;
	}
	
	@Delete
	public void deleteComment(Representation entity) {
		ApplicationContext ctx = 
				new ClassPathXmlApplicationContext("activiti-context.xml");
		ProcessCommentService service = ctx.getBean("processCommentService",ProcessCommentService.class);
		
		String commentParams;
		String id = null;
		try {
			commentParams = entity.getText();
			JsonNode taskJSON = new ObjectMapper().readTree(commentParams);
			if(taskJSON.path("id") != null && taskJSON.path("id").getTextValue() != null) {
				id = taskJSON.path("id").getTextValue();
				service.deleteById(id);
			}
		} catch (Exception e) {
			throw new ActivitiException("Failed to delete comment with id: " + id, e);
		}
	}

}
