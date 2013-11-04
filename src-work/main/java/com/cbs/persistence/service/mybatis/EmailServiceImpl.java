package com.cbs.persistence.service.mybatis;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cbs.persistence.domain.Email;
import com.cbs.persistence.mapper.EmailMapper;
import com.cbs.persistence.service.EmailService;

@Service("emailService")
@Repository
@Transactional
public class EmailServiceImpl implements EmailService {
	
	@Autowired
	private EmailMapper emailMapper;
	
	@Transactional(readOnly=true)
	public List<Email> findAll() {
		List<Email> emails = emailMapper.findAll();
		return emails;
	}

}
