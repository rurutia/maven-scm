package com.cbs.rest.api.history;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.rest.api.history.HistoricProcessInstanceResponse;

/**
 * CBS customized HistoricProcessInstance to have associated tasks together
 * @link HistoricProcessInstanceResponse
 *
 */

public class HistoricProcessInstanceResponseCBS extends HistoricProcessInstanceResponse {

	List<Map<String, Object>> tasks;
	List<HistoricActivityInstance> subprocesses;

	public List<Map<String, Object>> getTasks() {
		return tasks;
	}

	public void setTasks(List<Map<String, Object>> tasks) {
		this.tasks = tasks;
	}

	public List<HistoricActivityInstanceCBS> getSubprocesses() {
		List<HistoricActivityInstanceCBS> list = new ArrayList<HistoricActivityInstanceCBS> ();
		if(subprocesses != null) {
			for(HistoricActivityInstance instance: subprocesses) {
				HistoricActivityInstanceCBS activityCBS = new HistoricActivityInstanceCBS(instance);	
				list.add(activityCBS);
			}
		}
		return list;
	}

	public void setSubprocesses(List<HistoricActivityInstance> subprocesses) {
		this.subprocesses = subprocesses;
	}

}
 