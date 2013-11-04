package com.cbs.rest.api.history;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.resource.Get;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import javax.xml.parsers.SAXParserFactory;

/**
 * Returns historic form properties specific to a completed task
 * 
 * For form property that has text values in model XML. Its text value
 * is retrieved.
 * e.g. An option value of 3 selected at exclusive gateway will becomes 
 * its real text value "cancel inquiry" 
 * 
 */
public class TaskHistoricFormPropertyResource extends SecuredResource {

	@Get
	public ObjectNode getHistoricFormProperties()
			throws ParserConfigurationException, SAXException, IOException {
		if (authenticate() == false) return null;

		String taskId = (String) getRequest().getAttributes().get("taskId");
		
		HistoricTaskInstance taskInst = ActivitiUtil.getHistoryService()
							.createHistoricTaskInstanceQuery().taskId(taskId)
							.singleResult();
		
		if(taskInst == null)
			throw new ActivitiException("task id " + taskId + " is not found");
		
		// historic form properties list
		List<HistoricDetail> list = ActivitiUtil.getHistoryService()
							.createHistoricDetailQuery().taskId(taskId)
							.formProperties().list();

		// taskDefinitionKey to identify user task in model XML
//		String taskDefinitionKey = taskInst.getTaskDefinitionKey();
		
		// get process definition to retrieve BPMN model XML
		ProcessDefinition processDefinition = 
				ActivitiUtil.getRepositoryService().createProcessDefinitionQuery()
							.processDefinitionId(taskInst.getProcessDefinitionId())
							.singleResult();
		
		// model XML read as stream to be passed to SAX parser
		InputStream inputStream =
				ActivitiUtil.getRepositoryService().getResourceAsStream(processDefinition.getDeploymentId(), processDefinition.getResourceName());
		
		ObjectNode propertyJSON = new ObjectMapper().createObjectNode();
		
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		
		// Parse all form properties against XML model to return real text value of the property if any
		if (list != null) {
			for (HistoricDetail historicDetail : list) {
				HistoricFormProperty property = (HistoricFormProperty) historicDetail;
				FormPropertyHandler handler = new FormPropertyHandler();
				handler.id = property.getPropertyId();
				handler.value = property.getPropertyValue();
				inputStream.reset();
				parser.parse(inputStream , handler);
				if(handler.outcome != null)
					propertyJSON.put(property.getPropertyId(), handler.value + "." + handler.outcome);
				else
					propertyJSON.put(property.getPropertyId(), property.getPropertyValue());
			}
		}
		return propertyJSON;
	}

	
	/**
	 *  Get the more meaningful option value from XML based on option integer value
	 *  e.g. for option 3, the outgoing flow sequence after gateway has a text value
	 *       of "followUpOutcome-3-Cancel_inquiry" which is stored in model XML.
	 *  Handler will return "cancel inquiry"  
	 *
	 */
	private class FormPropertyHandler extends DefaultHandler {
		String id;
		String value;
		String outcome;

		public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
			if (qName.equals("sequenceFlow")) {
				String sequenceId = attrs.getValue("id");
				if(sequenceId.contains(id)) {
					String[] outcomeList = sequenceId.split("-");
					if(outcomeList[1].equals(value)) {
						outcome = outcomeList[2].replace("_", " ");
					}
				}
			}
		}
		
	}
	
	// Predefined conventions to translate form property name to value
//	private String translatePropertyValue(String name, String value) {
//		// comma separated list of options
//		if(name.contains(",")) { 
//			int option = Integer.parseInt(value);
//			return name.split(",")[option];
//		}
//		return value;
//	}

}
