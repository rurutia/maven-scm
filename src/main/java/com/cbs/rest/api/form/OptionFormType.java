package com.cbs.rest.api.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.activiti.engine.form.AbstractFormType;

/**
 * Customized form type options
 * e.g. OptionFormType with name "Please choose option,order,call back,no answer,cancel
 * will have new property name "Please choose option" and a "data" field with a list of
 * ["option", "order", "call back", "no answer", "cancel"] in Json response for client 
 * web app to render
 */
public class OptionFormType extends AbstractFormType implements FormTypeCBS {
	 
	private static final long serialVersionUID = 1L;
	
	public static final String TYPE_NAME = "option";
	  private String propertyData;
	  private String propertyName;
	 
	  public String getName() {
	    return TYPE_NAME;
	  }
	  
	  public Object convertFormValueToModelValue(String propertyValue) {
	    Integer option = Integer.valueOf(propertyValue);
	    return option;
	  }
	 
	  public String convertModelValueToFormValue(Object modelValue) {
	    if (modelValue == null) {
	      return null;
	    }
	    return modelValue.toString();
	  }
	  
	  public Object getProcessedPropertyData() {
		  String[] options = this.propertyData.split(",");
		  this.setPropertyName(options[0]);
		  List<String> list = new ArrayList<String>(Arrays.asList(options));
		  list.remove(0);
		  return list;
	  }
	  
	  public void setRawPropertyData(String rawData) {
		  this.propertyData = rawData;			
	  }
	  
	  public String getPropertyName() {
		  return this.propertyName;
	  }
	
	  public void setPropertyName(String propertyName) {
		  this.propertyName = propertyName;
	  }

}
