package com.cbs.process.listener.execution;

import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

public class AdhocCheckExecutionListener implements ExecutionListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		String processId = execution.getProcessInstanceId();
		List<org.activiti.engine.history.HistoricActivityInstance> list = execution.getEngineServices().getHistoryService()
		.createHistoricActivityInstanceQuery()
		.processInstanceId(processId).unfinished().list();
		
		boolean canBeCompleted = true;
		org.activiti.engine.RuntimeService rs = execution.getEngineServices().getRuntimeService();
		String parentProcessId = (String)rs.getVariable(processId, "adhocParentId");
		String newActivityType = (String)rs.getVariable(processId, "newActivityType");
		if(!"parentProcessCompleted".equals(newActivityType)
		   && rs.createProcessInstanceQuery().processInstanceId(parentProcessId).singleResult() != null
		  ) {
			canBeCompleted = false;
		}
		else {
			for(org.activiti.engine.history.HistoricActivityInstance activity : list) {
				if( (activity.getActivityType().equals("userTask") || activity.getActivityType().equals("callActivity"))
					 && activity.getEndTime() == null) {
					canBeCompleted = false;
					break;
				}
			}			
		}
		if(canBeCompleted)
			execution.setVariable("customerKey", "adhoc");
		execution.setVariable("canBeCompleted", canBeCompleted);

	}

}
