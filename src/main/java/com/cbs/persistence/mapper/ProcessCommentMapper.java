package com.cbs.persistence.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.cbs.persistence.domain.ProcessComment;

public interface ProcessCommentMapper {
	
	public List<ProcessComment> findByProcessId(String processId);
	
	public List<ProcessComment> findByProcessIdAndUserId(@Param("processId")String processId,
														 @Param("userId")String userId);
	
	public ProcessComment findSingleById(String id);
	
	public void insertProcessComment(ProcessComment processComment);
	
	public void deleteById(String id);
	
	public int getCountByProcessId(String processId);
	
	public void changeTimer();
	
}
