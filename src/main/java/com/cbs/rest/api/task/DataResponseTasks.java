package com.cbs.rest.api.task;

/**
 * 
 * Task list REST response 
 * Add number of "total overdue tasks" and number "page overdue tasks" to response
 */

public class DataResponseTasks {

	  Object data;
	  long total;
	  int start;
	  String sort;
	  String order;
	  int size;
	  
	  // fields added by CBS
	  long total_overdue;
	  int page_overdue;

	  public Object getData() {
	    return data;
	  }

	  public DataResponseTasks setData(Object data) {
	    this.data = data;
	    return this;
	  }

	  public long getTotal() {
	    return total;
	  }

	  public void setTotal(long total) {
	    this.total = total;
	  }

	  public int getStart() {
	    return start;
	  }

	  public void setStart(int start) {
	    this.start = start;
	  }

	  public String getSort() {
	    return sort;
	  }

	  public void setSort(String sort) {
	    this.sort = sort;
	  }

	  public String getOrder() {
	    return order;
	  }

	  public void setOrder(String order) {
	    this.order = order;
	  }

	  public int getSize() {
	    return size;
	  }

	  public void setSize(int size) {
	    this.size = size;
	  }
	  
	  public long getTotal_overdue() {
		return total_overdue;
	  }

	  public void setTotal_overdue(long total_overdue) {
		this.total_overdue = total_overdue;
	  }

	  public int getPage_overdue() {
		return page_overdue;
	  }

	  public void setPage_overdue(int page_overdue) {
		this.page_overdue = page_overdue;
	  }
}

