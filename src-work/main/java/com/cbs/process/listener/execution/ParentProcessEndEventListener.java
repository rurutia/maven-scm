package com.cbs.process.listener.execution;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.runtime.Execution;

public class ParentProcessEndEventListener implements ExecutionListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		String superProcessId = execution.getEngineServices().getHistoryService().createHistoricProcessInstanceQuery()
				.processInstanceId(execution.getProcessInstanceId()).singleResult().getSuperProcessInstanceId();
		
		// only when top level process completes, send a message to Ad-Hoc process 
		if(superProcessId == null) { 
			RuntimeService rs = execution.getEngineServices().getRuntimeService();
			String adhocProcessId = (String)rs.getVariable(execution.getId(), "adhocChildId");
			String customerKey = (String)rs.getVariable(execution.getId(), "customerKey");
			rs.setVariable(adhocProcessId, "newActivityType", "parentProcessCompleted");
			rs.setVariable(adhocProcessId, "customerKey", customerKey);
			Execution messageExecution = rs.createExecutionQuery()
					.processInstanceId(adhocProcessId)
					.messageEventSubscriptionName("callAdhoc")
					.singleResult();
			rs.messageEventReceived("callAdhoc", messageExecution.getId());
		}

	}

}
