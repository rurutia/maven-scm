package com.cbs.rest.api.task;

import java.util.List;
import java.util.Map;

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.form.EnumFormType;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.resource.Get;
import com.cbs.rest.api.form.FormTypeCBS;

/**
 * Return task form properties including CBS customized form types 
 * such as "option"
 *
 */
public class TaskPropertiesResourceCBS extends SecuredResource {
	
	@Get
	public ObjectNode getTaskProperties() {
		if(authenticate() == false) return null;
		String taskId = (String) getRequest().getAttributes().get("taskId");
		TaskFormData taskFormData = ActivitiUtil.getFormService().getTaskFormData(taskId);

		ObjectNode responseJSON = new ObjectMapper().createObjectNode();

		ArrayNode propertiesJSON = new ObjectMapper().createArrayNode();
		
		if(taskFormData != null) {

			List<FormProperty> properties = taskFormData.getFormProperties();
			for (FormProperty property : properties) {
				ObjectNode propertyJSON = new ObjectMapper().createObjectNode();
				propertyJSON.put("name", property.getName());
				propertyJSON.put("id", property.getId());

				if(property.getValue() != null) {
					propertyJSON.put("value", property.getValue());
				} else {
					propertyJSON.putNull("value");
				}
				
				if(propertyJSON.get("type") == null) {
					if(property.getType() != null) {
						propertyJSON.put("type", property.getType().getName());
						// for customized form property type, attach data and rewrite name based on original property name
						// e.g. "Please choose option:, accepted with artwork, not accepted with artwork, callback, no answer, order without artwork, cancel"
						if(property.getType() instanceof FormTypeCBS) {
							FormTypeCBS cbsFormType = (FormTypeCBS)property.getType();
							cbsFormType.setRawPropertyData(property.getName());
							propertyJSON.put("data", new ObjectMapper().createObjectNode().POJONode(cbsFormType.getProcessedPropertyData()));
							propertyJSON.put("name", cbsFormType.getPropertyName());
						}
						
						if(property.getType() instanceof EnumFormType) {
							@SuppressWarnings("unchecked")
							Map<String, String> valuesMap = (Map<String, String>) property.getType().getInformation("values");
							if(valuesMap != null) {
								ArrayNode valuesArray = new ObjectMapper().createArrayNode();
								propertyJSON.put("enumValues", valuesArray);

								for (String key : valuesMap.keySet()) {
									ObjectNode valueJSON = new ObjectMapper().createObjectNode();
									valueJSON.put("id", key);
									valueJSON.put("name", valuesMap.get(key));
									valuesArray.add(valueJSON);
								}
							}
						}

					} else {
						propertyJSON.put("type", "String");
					}
				}
				
				propertyJSON.put("required", property.isRequired());
				propertyJSON.put("readable", property.isReadable());
				propertyJSON.put("writable", property.isWritable());

				propertiesJSON.add(propertyJSON);
			}
		}

		responseJSON.put("data", propertiesJSON);

		return responseJSON;
	}

}
