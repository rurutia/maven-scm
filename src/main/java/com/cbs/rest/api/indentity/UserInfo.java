package com.cbs.rest.api.indentity;

import org.activiti.engine.identity.User;

public class UserInfo {

	String id;
	String firstName;
	String lastName;
	String email;

	public UserInfo(){}

	public UserInfo(User user) {
		setId(user.getId());
		setEmail(user.getEmail());
		setFirstName(user.getFirstName());
		setLastName(user.getLastName());
	}

	public String getId() {
		return id;
	}
	public UserInfo setId(String id) {
		this.id = id;
		return this;
	}
	public String getFirstName() {
		return firstName;
	}
	public UserInfo setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}
	public String getLastName() {
		return lastName;
	}
	public UserInfo setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}
	public String getEmail() {
		return email;
	}
	public UserInfo setEmail(String email) {
		this.email = email;
		return this;
	}

}




