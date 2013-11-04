package com.cbs.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cbs.persistence.service.EmailService;
import com.cbs.persistence.service.EventService;

public class MyBatis {

	public static void main(String[] args) {
		ApplicationContext ctx = 
				new ClassPathXmlApplicationContext("activiti-context.xml");
		EventService eventService = ctx.getBean("eventService",EventService.class);
		System.out.println(eventService);
//		eventService.createNewTable("event");
	}

}
