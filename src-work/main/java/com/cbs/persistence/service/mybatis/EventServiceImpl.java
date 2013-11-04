package com.cbs.persistence.service.mybatis;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cbs.persistence.domain.Event;
import com.cbs.persistence.mapper.EmailMapper;
import com.cbs.persistence.mapper.EventMapper;
import com.cbs.persistence.service.EventService;

@Service("eventService")
@Repository
@Transactional
public class EventServiceImpl implements EventService {

	@Autowired
	private EventMapper eventMapper;
	
	@Transactional(readOnly=false)
	public void createNewTable(String name) {
		eventMapper.createNewTable(name);
	}

	@Transactional(readOnly=true)
	public List<Event> findAll() {
		return eventMapper.findAll();
	}

	@Transactional(readOnly=false)
	public void insertEvent(Event event) {
		eventMapper.insertEvent(event);
		
	}


}
