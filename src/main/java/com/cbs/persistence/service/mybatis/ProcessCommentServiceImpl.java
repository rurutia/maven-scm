package com.cbs.persistence.service.mybatis;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cbs.persistence.domain.ProcessComment;
import com.cbs.persistence.mapper.ProcessCommentMapper;
import com.cbs.persistence.service.ProcessCommentService;

@Service("processCommentService")
@Repository
@Transactional
public class ProcessCommentServiceImpl implements ProcessCommentService {
	
	@Autowired
	private ProcessCommentMapper processCommentMapper;
	
	@Transactional(readOnly=true)
	public List<ProcessComment> findByProcessId(String processId){
		return processCommentMapper.findByProcessId(processId);
	}
	
	@Transactional(readOnly=true)
	public List<ProcessComment> findByProcessIdAndUserId(String processId, String userId) {
		return processCommentMapper.findByProcessIdAndUserId(processId, userId);
	}
	
	@Transactional(readOnly=true)
	public ProcessComment findSingleById(String id) {
		return processCommentMapper.findSingleById(id);
	}
	
	@Transactional(readOnly=false)
	public void insertProcessComment(ProcessComment processComment) {
		processCommentMapper.insertProcessComment(processComment);
	}
	
	@Transactional(readOnly=false)
	public void deleteById(String id) {
		processCommentMapper.deleteById(id);
	}

	@Transactional(readOnly=true)
	public int getCountByProcessId(String processId){
		return processCommentMapper.getCountByProcessId(processId);
	}

	@Transactional(readOnly=false)
	public void changeTimer() {
		processCommentMapper.changeTimer();
	}

}
