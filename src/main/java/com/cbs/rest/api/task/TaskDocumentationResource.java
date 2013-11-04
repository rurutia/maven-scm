package com.cbs.rest.api.task;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.resource.Get;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * Get task documentation from XML.
 *
 */
public class TaskDocumentationResource extends SecuredResource {
	
	@Get
	public ObjectNode getTaskDocumentation() throws IOException, ParserConfigurationException, SAXException {
		if(authenticate() == false) return null;
		String taskId = (String) getRequest().getAttributes().get("taskId");

		ObjectNode responseJSON = new ObjectMapper().createObjectNode();
		
		HistoricTaskInstance taskInst = ActivitiUtil.getHistoryService()
				.createHistoricTaskInstanceQuery().taskId(taskId)
				.singleResult();

		if(taskInst == null)
			throw new ActivitiException("task id " + taskId + " is not found");

		// taskDefinitionKey to identify user task in model XML
		String taskDefinitionKey = taskInst.getTaskDefinitionKey();

		// get process definition to retrieve BPMN model XML
		ProcessDefinition processDefinition = 
				ActivitiUtil.getRepositoryService().createProcessDefinitionQuery()
				.processDefinitionId(taskInst.getProcessDefinitionId())
				.singleResult();

		// model XML read as stream to be passed to SAX parser
		InputStream inputStream =
				ActivitiUtil.getRepositoryService().getResourceAsStream(processDefinition.getDeploymentId(), processDefinition.getResourceName());
				
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		
		TaskDocumentationHandler handler = new TaskDocumentationHandler();
		handler.taskDefinitionKey = taskDefinitionKey;
		
		parser.parse(inputStream , handler);
		
		responseJSON.put("data", handler.value);
		
		return responseJSON;
	}
	
	private class TaskDocumentationHandler extends DefaultHandler {
		String taskDefinitionKey;
		String value = "";
		boolean isTaskKeyFound = false;
		boolean isDocumentationFound = false;
		String tempValue;

		public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
			// locate <userTask> element that have same taskDefinitionKey 
			if (qName.equals("userTask")) {
				isTaskKeyFound = 
						attrs.getValue("id").equals(this.taskDefinitionKey) ? true : false;
			}
			
			if (qName.equals("documentation") && isTaskKeyFound) {
				isDocumentationFound = true;
			}
		}
		
		public void characters(char ch[], int start, int length) throws SAXException {
			if(isDocumentationFound) {
				this.tempValue = new String(ch, start, length);
				value += tempValue;
			}
		}
		
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if(isDocumentationFound) {
//				this.value = tempValue;
				isDocumentationFound = false;
			}
			
		}

		
	}
	
}

