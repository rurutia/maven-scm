package com.cbs.persistence.mapper;

import java.util.List;

import com.cbs.persistence.domain.Email;
import com.cbs.persistence.domain.Event;

public interface EventMapper {
	
	public void createNewTable(String name);
	
	public List<Event> findAll();
	
	public void insertEvent(Event event);

}