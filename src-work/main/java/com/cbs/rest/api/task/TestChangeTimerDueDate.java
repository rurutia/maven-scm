package com.cbs.rest.api.task;

import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cbs.persistence.service.ProcessCommentService;

public class TestChangeTimerDueDate extends SecuredResource {
	
	  @Get
	  @SuppressWarnings("unchecked")
	  public void changeTimerDueDate() {
		    ApplicationContext ctx = 
					new ClassPathXmlApplicationContext("activiti-context.xml");
			ProcessCommentService service = ctx.getBean("processCommentService",ProcessCommentService.class);
			service.changeTimer();
	  }

}
