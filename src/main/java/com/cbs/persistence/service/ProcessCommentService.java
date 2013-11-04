package com.cbs.persistence.service;

import java.util.List;

import com.cbs.persistence.domain.ProcessComment;

public interface ProcessCommentService {
	
	public List<ProcessComment> findByProcessId(String processId);
	
	public List<ProcessComment> findByProcessIdAndUserId(String processId, String userId);

	public ProcessComment findSingleById(String id);
	
	public void insertProcessComment(ProcessComment processComment);
	
	public void deleteById(String id);
	
	public int getCountByProcessId(String processId);
	
	public void changeTimer();


}
