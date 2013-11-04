package com.cbs.rest.api.task;

import java.util.List;

import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.task.Task;
import org.activiti.spring.SpringProcessEngineConfigurationCBS;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TaskQueryCBS extends TaskQueryImpl {
	public void test(){
		  ApplicationContext ctx = 
					new ClassPathXmlApplicationContext("activiti-context.xml");
		  SpringProcessEngineConfigurationCBS processEngineConfiguration = ctx.getBean("processEngineConfiguration",SpringProcessEngineConfigurationCBS.class);
		  CommandExecutor commandExecutorTxRequired = processEngineConfiguration
				   .getCommandExecutorTxRequired();
		  
		  final TaskQueryImpl q = new TaskQueryImpl();
		  
		  List<Task> tasks = commandExecutorTxRequired.execute(
				   new Command<List<Task>>() {  
				     @Override  
				     public List<Task> execute(CommandContext commandContext) {  
				        return commandContext.getDbSqlSession()
				         .selectList("selectTaskByQueryCriteria", q);  
				     }  
				 });  
		  
	}
}
