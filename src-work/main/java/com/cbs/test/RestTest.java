package com.cbs.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

public class RestTest {
	
	public static void main2(String[] args) throws IOException {
		URL url = new URL("https://secure.saasu.com/webservices/rest/r1/tasks?wsaccesskey=5E554AB1C4244C7A8A2331E5D300CDD4&FileUid=18100");
				
//	    url = new URL("https://secure.saasu.com/webservices/rest/r1/contactlist?wsaccesskey=5E554AB1C4244C7A8A2331E5D300CDD4&FileUid=18100");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Accept", "application/xml");

		
		String input = "<?xml version=\"1.0\" encoding=\"utf-8\"?><insertInvoice emailToContact=\"false\"><invoice uid=\"0\"></invoice></insertInvoice>";
		 
		OutputStream os = conn.getOutputStream();
		os.write(input.getBytes());
		os.flush();
		
		
		
		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}
 
		BufferedReader br = new BufferedReader(new InputStreamReader(
			(conn.getInputStream())));
 
		String output;
		System.out.println("Output from Server .... \n");
		while ((output = br.readLine()) != null) {
			System.out.println(output);
		}
	for(String name: conn.getHeaderFields().keySet()) {
		System.out.println("name:" + name);
		System.out.println("value:" + conn.getHeaderFields().get(name));
		
	    }
	}
	
	public static void main(String[] args) throws IOException {
		// Prepare the request  
//		ClientResource resource = new ClientResource("https://secure.saasu.com/webservices/rest/r1/tasks?wsaccesskey=5E554AB1C4244C7A8A2331E5D300CDD4&FileUid=18100");  
		ClientResource resource = new ClientResource("http://192.168.1.29:8081/activiti-rest-cbs/service/process-engine");  

		
		
		// Add the client authentication to the call  
		ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;  
		ChallengeResponse authentication = new ChallengeResponse(scheme,  
		        "Alex", "dev12345");  
		resource.setChallengeResponse(authentication);  
//		Form form = new Form();  
//		form.add("userId","kermit");
//		form.add("type","participant");
//		String text = "<?xml version=\"1.0\" encoding=\"utf-8\"?><insertInvoice emailToContact=\"false\"><invoice uid=\"0\"></invoice></insertInvoice>";
//		String text = FileUtils.readFileToString(new File("/home/dev/workspace/activiti-rest-cbs/data.xml"));
//		StringRepresentation s = new StringRepresentation(text);
//	    resource.post(s);

		// Send the HTTP GET request  
		resource.get();  

//    	Form allHeaders = (Form) resource.getRequest().getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
//    	System.out.println(allHeaders);
	    if (resource.getStatus().isSuccess()) {  

		    // Output the response entity on the JVM console  
		    resource.getResponseEntity().write(System.out);  

		    
//		    for(String key: resource.getResponseAttributes().keySet()) {
//		    	System.out.println("---" + key);
//		    	Form headers = (Form)resource.getResponseAttributes().get(key);
//		    	for(String headerKey: headers.getValuesMap().keySet()) {
//		    		System.out.println("name:" + headerKey);
//		    		System.out.println("value:" + headers.getValuesMap().get(headerKey));
//		    		
//		    	}
//		    }
		} else if (resource.getStatus()  
		        .equals(Status.CLIENT_ERROR_UNAUTHORIZED)) {  
		    // Unauthorized access  
		    System.out  
		            .println("Access authorized by the server, check your credentials");  
		} else {  
		    // Unexpected status  
		    System.out.println("An unexpected status was returned: "  
		            + resource.getStatus());  
		} 
		
	}

}
