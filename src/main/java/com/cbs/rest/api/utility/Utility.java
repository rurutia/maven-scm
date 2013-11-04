package com.cbs.rest.api.utility;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.task.Comment;
import org.activiti.rest.api.ActivitiUtil;

public class Utility<T> {
	
	public List<T> reverseList(List<T> myList) {
		List<T> invertedList = new ArrayList<T>();
		for (int i = myList.size() - 1; i >= 0; i--) {
			invertedList.add(myList.get(i));
		}
		return invertedList;
	}
	
	public static String getProcessDefinitionNameFromDefinitionId(String processDefinitionId) {
		return ActivitiUtil.getRepositoryService()
				.createProcessDefinitionQuery()
				.processDefinitionId(processDefinitionId)
				.singleResult()
				.getName();
	}
	  
}
