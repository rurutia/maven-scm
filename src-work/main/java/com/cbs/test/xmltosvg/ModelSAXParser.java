package com.cbs.test.xmltosvg;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ModelSAXParser extends DefaultHandler {

	String pretag = null;
	int index = -1;
	List<BPMNShape> shapes;
	
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		File file = new File("/home/dev/workspace/activiti-rest-cbs/build/classes/com/cbs/test/xmltosvg/test.bpmn");
		FileInputStream fis = null;
		
		fis = new FileInputStream(file);
		
		SAXParserFactory factory = SAXParserFactory.newInstance();  
	    SAXParser parser = factory.newSAXParser();  
	    ModelSAXParser handler = new ModelSAXParser();  
	    parser.parse(fis, handler);  
	    
		if(fis != null)
			fis.close();
	}
	
    @Override  
    public void startDocument() throws SAXException {  
       System.out.println("SAX XML parsing starts...");
       shapes = new ArrayList<BPMNShape>();
    }

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if("startEvent".equals(qName)) {
			BPMNShape shape = new BPMNShape();
			shape.setId(attributes.getValue("id"));
			shape.setName(attributes.getValue("name"));
			shape.setType("startEvent");
			shapes.add(shape);
		}
		if("userTask".equals(qName)) {
			BPMNShape shape = new BPMNShape();
			shape.setId(attributes.getValue("id"));
			shape.setName(attributes.getValue("name"));
			shape.setType("userTask");
			shapes.add(shape);
		}
		if("bpmndi:BPMNShape".equals(qName)) {
			pretag = qName;
			for(BPMNShape shape : shapes) {
				if( shape.getId().equals(attributes.getValue("bpmnElement")) ) {
					index = shapes.indexOf(shape);
					break;
				}
			}
		}
		
		if("bpmndi:BPMNShape".equals(pretag) && "omgdc:Bounds".equals(qName)) {
			if(index != -1) {
				shapes.get(index).setLeft(attributes.getValue("x"));
				shapes.get(index).setWidth(attributes.getValue("width"));
				shapes.get(index).setTop(attributes.getValue("y"));
				shapes.get(index).setHeight(attributes.getValue("height"));
			}
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		System.out.println(shapes);
		pretag = null;
		index = -1;
	} 

}
