package com.cbs.rest.api.indentity;

import java.util.List;

import org.activiti.engine.identity.Group;

public class UserGroupInfo {
	String id;
	String name;
	String type;
	List<String> users;

	public UserGroupInfo(){}

	public UserGroupInfo(Group group) {
		setId(group.getId());
		setName(group.getName());
		setType(group.getType());
	}

	public String getId() {
		return id;
	}
	public UserGroupInfo setId(String id) {
		this.id = id;
		return this;
	}
	public String getName() {
		return name;
	}
	public UserGroupInfo setName(String name) {
		this.name = name;
		return this;
	}
	public String getType() {
		return type;
	}
	public UserGroupInfo setType(String type) {
		this.type = type;
		return this;
	}
	public List<String> getUsers() {
		return users;
	}
	public void setUsers(List<String> users) {
		this.users = users;
	}
	
}
