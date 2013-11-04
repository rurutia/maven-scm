package com.cbs.persistence.service;

import java.util.List;

import com.cbs.persistence.domain.Email;

public interface EmailService {
	
	public List<Email> findAll();
	
}
