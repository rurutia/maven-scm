package com.cbs.rest.api.indentity;

/**
 * Login response adds user group information 
 *
 */
public class LoginUserAndGroupInfoResponse {

	private boolean success;
	private String id;
	private String firstName;
	private String lastName;
	private String email;
	private Object groups;

	public boolean isSuccess() {
      return success;
    }

	public void setSuccess(boolean success) {
	  this.success = success;
	}
	
	public void setUserInfo(UserInfo userInfo) {
		this.id = userInfo.getId();
		this.firstName = userInfo.getFirstName();
		this.lastName = userInfo.getLastName();
		this.email = userInfo.getEmail();
	}

	public String getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmail() {
		return email;
	}

	public Object getGroups() {
		return groups;
	}

	public void setGroups(Object groups) {
		this.groups = groups;
	}

}
