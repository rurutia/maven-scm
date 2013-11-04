package com.cbs.persistence.service.mybatis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cbs.persistence.mapper.TimerMapper;
import com.cbs.persistence.service.TimerService;

@Service("timerService")
@Repository
@Transactional
public class TimerServiceImpl implements TimerService{
	
	@Autowired
	private TimerMapper timerMapper;

	@Transactional(readOnly=false)
	public void updateTimer(String dueDate, String executionId, String timerId) {
		System.out.println(dueDate);
		System.out.println(executionId);
		System.out.println(timerId);
		timerMapper.updateTimer(dueDate, executionId, timerId);
	}

}
