package com.cbs.test.mybatis;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cbs.persistence.domain.ProcessComment;
import com.cbs.persistence.service.ProcessCommentService;

public class ProcessCommentTest {

	public static void main(String[] args) {
	    ApplicationContext ctx = 
				new ClassPathXmlApplicationContext("activiti-context.xml");
		ProcessCommentService service = ctx.getBean("processCommentService",ProcessCommentService.class);
		ProcessComment processComment = new ProcessComment();
//		String pid = "1115";
//		processComment.setId("pc" + System.currentTimeMillis()/100);
//		processComment.setUserId("Tim");
//		processComment.setProcessInstanceId(pid);
//		processComment.setMessage("new comment process");
//		service.insertProcessComment(processComment);
		
//		service.deleteById("pc13753630227");
//		
//		for(ProcessComment comment : service.findByProcessIdAndUserId("1115","Tim")) {
//			System.out.println(comment.getId());
//		}
		
		System.out.println(service.findSingleById("pc13753672868").getId()); 

	}

}
