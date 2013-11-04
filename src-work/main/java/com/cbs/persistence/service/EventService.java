package com.cbs.persistence.service;

import java.util.List;

import com.cbs.persistence.domain.Event;

public interface EventService {

	public void createNewTable(String name);
	
	public List<Event> findAll();
	
	public void insertEvent(Event event);
	
}
