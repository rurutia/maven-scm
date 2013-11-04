package com.cbs.test.bpmn;

import java.util.List;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.api.ActivitiUtil;

public class ActivitiExecution {

	public static void main(String[] args) {
		RuntimeService rs = ActivitiUtil.getRuntimeService();
		TaskService ts = ActivitiUtil.getTaskService();
//		rs.setVariableLocal("82", "receive_payment_execution_variable", "bar");
//		ts.setVariableLocal("85", "receive_payment_task_variable", "foo");
//		rs.setVariableLocal("82", "receive_payment_ex_variable", "bar");
//		ts.setVariable("85", "receive_payment_process_variable", "foobar");
		
//		DelegateExecution execution;

//		execution.getEngineServices().getRuntimeService().deleteProcessInstance(execution.getProcessInstanceId(), "artwork completed");
		
//		List<ProcessInstance> processes = rs.createProcessInstanceQuery().processInstanceId("78").list();
//	    for(ProcessInstance process : processes) {
//	    	System.out.println(process.getId());
//	    }
		
//		List<Execution> executions = rs.createExecutionQuery().processInstanceId("78").list();
//		for(Execution execution : executions) {
//			System.out.println(execution.getId());
//		}
		
		System.out.println(ts.createTaskQuery().taskId("85") .singleResult().getProcessInstanceId());
		
		List<HistoricActivityInstance> list = ActivitiUtil.getHistoryService().createHistoricActivityInstanceQuery().executionId("117").list();
		
	    System.out.println(rs.getActiveActivityIds("82"));
	    
		for(HistoricActivityInstance inst : list) {
			System.out.println(inst.getActivityName());
		}

	}
 	
}
