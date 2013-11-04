/* not used at this stage
package com.cbs.rest.api.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.HistoricProcessInstanceQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.SecuredResource;
import org.restlet.resource.Get;

import com.cbs.rest.api.task.TaskResponseCBS;

public class ProcessActiveInstancesTasksResource extends SecuredResource {
  
  Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();
  
  public ProcessActiveInstancesTasksResource() {
    properties.put("id", HistoricProcessInstanceQueryProperty.PROCESS_INSTANCE_ID_);
    properties.put("processDefinitionId", HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_ID);
    properties.put("businessKey", HistoricProcessInstanceQueryProperty.BUSINESS_KEY);
    properties.put("startTime", HistoricProcessInstanceQueryProperty.START_TIME);
  }
  
  @Get
  public DataResponse getProcessInstances() {
    if(authenticate() == false) return null;
    
    HistoricProcessInstanceQuery query = ActivitiUtil.getHistoryService().createHistoricProcessInstanceQuery();
    
    String processDefinitionId = getQuery().getValues("processDefinitionId");
    String processInstanceKey = getQuery().getValues("businessKey");
    String taskAssignee = getQuery().getValues("taskAssignee");
    String taskFinished = getQuery().getValues("taskFinished");
    
    query = processDefinitionId == null ? query : query.processDefinitionId(processDefinitionId);
    query = processInstanceKey == null ? query : query.processInstanceBusinessKey(processInstanceKey);
    query.unfinished();
    
    DataResponse response = null;

    response = new ProcessActiveInstancesTasksPaginateList().paginateList(getQuery(), query, "id", properties);
	// filter processes with task assignee and if task is completed 
    filterTasks(taskAssignee, taskFinished, response);
    
    String taskOrder = getQuery().getValues("orderByTask");
    if(taskOrder != null)
    	orderByTask(taskOrder, response);
    return response;
  }
  
  // order processes by active tasks, process with oldest active task comes first
  @SuppressWarnings("unchecked")
  private void orderByTask(String taskOrder, DataResponse response) {
	if( taskOrder.equals("old") ) {
		List<ProcessInstancesResponseCBS> processResponseList
				=  (List<ProcessInstancesResponseCBS>) response.getData();
		Collections.sort(processResponseList, new TaskIdComparator());
		response.setData(processResponseList);
		return;
	}
	else
		throw new ActivitiIllegalArgumentException("Value for param 'orderByTask' is not valid, '" + taskOrder + "' is not a valid property");
  }
  
  private class TaskIdComparator implements Comparator<ProcessInstancesResponseCBS> {

	public int compare(ProcessInstancesResponseCBS process1,
			ProcessInstancesResponseCBS process2) {
		TaskResponseCBS task1 = getOldestActiveTask(process1.getTasks());
		TaskResponseCBS task2 = getOldestActiveTask(process2.getTasks());
		int id1 = Integer.parseInt(task1.getId());
		int id2 = Integer.parseInt(task2.getId());
		return id1 < id2 ? -1 : ( id1 > id2 ? 1 : 0 );
	}
	
	private TaskResponseCBS getOldestActiveTask(
			List<TaskResponseCBS> list) {
		TaskResponseCBS oldestActiveTask = null;
		for(TaskResponseCBS task : list) {
			if(task.getEndTime() == null) {
				if(oldestActiveTask == null) 
					oldestActiveTask = task;
				else {
					if(Integer.parseInt(task.getId()) < Integer.parseInt(oldestActiveTask.getId()) )
						oldestActiveTask = task;
				}
			}
		}
		return oldestActiveTask;
	}
  }
  
  @SuppressWarnings("unchecked")
  private DataResponse filterTasks(String assignee, String taskFinished, DataResponse response ) {
		List<ProcessInstancesResponseCBS> processResponseList
											=  (List<ProcessInstancesResponseCBS>) response.getData();

		List<ProcessInstancesResponseCBS> filteredProcessResponseList 
					=  new ArrayList<ProcessInstancesResponseCBS>();

		List<TaskResponseCBS> customerTaskList = null;

	    for(ProcessInstancesResponseCBS customerProcess : processResponseList) {
	    	List<TaskResponseCBS> customerTaskAssignedToOneUserList
	    									= new ArrayList<TaskResponseCBS>();
	    	customerTaskList = customerProcess.getTasks();
	    	for(TaskResponseCBS customerTask : customerTaskList) {
	    		boolean isAdded = true;
	    		isAdded = isAdded && !( assignee != null && (customerTask.getAssignee() == null || !customerTask.getAssignee().equals(assignee)));
	    		isAdded = isAdded && !(taskFinished != null && taskFinished.equals("true") && customerTask.getEndTime() == null);
	    		isAdded = isAdded && !(taskFinished != null && taskFinished.equals("false") && customerTask.getEndTime() != null);
	    		if( isAdded) customerTaskAssignedToOneUserList.add(customerTask);
	    	}
	    	if( customerTaskAssignedToOneUserList.size() > 0) {
	    		customerProcess.setTasks(customerTaskAssignedToOneUserList);
	    		filteredProcessResponseList.add(customerProcess);
	    	}
	    }

	    response.setData(filteredProcessResponseList);

		return response;
	}


}
*/