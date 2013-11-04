package com.cbs.rest.api.process;

import org.activiti.rest.api.RestActionRequest;

/**
 * CBS customized process action extending default Activiti class 
 * @link RestActionRequest 
 * 
 * Used by class
 * @link ProcessInstanceResourceCBS
 * to handle actions performed on process 
 * 
 */
public class ProcessInstanceActionRequestCBS extends RestActionRequest {

  public static final String ACTION_SUSPEND = "suspend";
  public static final String ACTION_ACTIVATE = "activate";
  // actions on candidate list
  public static final String ACTION_ENLIST_CANDIDATE= "enlist_candidate";
  public static final String ACTION_UNLIST_CANDIDATE = "unlist_candidate";
  //actions on watch list
  public static final String ACTION_WATCH= "watch";
  public static final String ACTION_UNWATCH= "unwatch";
  
  private String userId;

  public String getUserId() {
	  return userId;
  }

  public void setUserId(String userId) {
	  this.userId = userId;
  }

}

