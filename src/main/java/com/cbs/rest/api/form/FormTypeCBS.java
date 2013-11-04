package com.cbs.rest.api.form;

import java.io.Serializable;

/**
 * CBS customized form types need to implement this interface
 * e.g. OptionFormType with name "Please choose option,order,call back,no answer,cancel
 *
 */

public interface FormTypeCBS extends Serializable {
	
	/**
	 * return data specific to customized form type for client web app to render
	 * @return data object
	 */
	 public Object getProcessedPropertyData();
	 
	 /**
	  * set raw data 
	  * @param rawData in String format e.g. form property name
	  */
	 public void setRawPropertyData(String rawData);
	 
	 public String getPropertyName();
	 
	 /**
	  * This method overwrites original form property name from activiti 
	  * @param new propertyName in String format 
	  */
	 public void setPropertyName(String propertyName);

}
