/* not used at this stage
package com.cbs.rest.api.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.rest.api.AbstractPaginateList;
import org.activiti.rest.api.ActivitiUtil;

import com.cbs.rest.api.task.TaskResponseCBS;

public class ProcessActiveInstancesTasksPaginateList extends AbstractPaginateList {

  @SuppressWarnings("rawtypes")
  @Override
  protected List processList(List list) {
    List<ProcessInstancesResponseCBS> processResponseList = new ArrayList<ProcessInstancesResponseCBS>();
    HistoricTaskInstanceQuery taskQuery = ActivitiUtil.getHistoryService().createHistoricTaskInstanceQuery();

    for (Object instance : list) {
    	HistoricProcessInstance processInstance = (HistoricProcessInstance) instance;
    	
    	List<HistoricTaskInstance> taskList = taskQuery.processInstanceId(processInstance.getId())
    												.orderByHistoricTaskInstanceStartTime()
    												.asc()
    												.list();
    	
    	List<HistoryTaskResponse> customerTaskList = new ArrayList<HistoryTaskResponse>(); 
    	for(HistoricTaskInstance taskInstance : taskList) {
    		customerTaskList.add(new TaskResponseCBS(taskInstance));
    	}
    	// customized process instance
    	ProcessInstancesResponseCBS customerProcessInstance
    			= new ProcessInstancesResponseCBS(processInstance);
    	// set task list
    	customerProcessInstance.setTasks(customerTaskList);
    	// set process variables
    	customerProcessInstance.setVariables(getProcessVariables(processInstance));

    	processResponseList.add(customerProcessInstance);		
    }
    return processResponseList;
  }
  
  private Map<String, String> getProcessVariables(HistoricProcessInstance processInstance) {
	  Map<String, String> variableMap = new HashMap<String, String>();
	  String id = processInstance.getId();
	  RuntimeService rs = ActivitiUtil.getRuntimeService();
  	  Map<String, Object> variables = rs.getVariables(id);
	  for( String key : variables.keySet()) {
		  String value = ( variables.get(key) == null) ? "null" : variables.get(key).toString();
		  variableMap.put(key, value);
  	  }
	  return variableMap;
  }
}
*/