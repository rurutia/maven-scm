package com.cbs.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.api.ActivitiUtil;

public class Deployer {

	public static void main(String[] args) {
		ActivitiUtil.getIdentityService().setAuthenticatedUserId("kermit");
		try {
			ActivitiUtil.getRepositoryService().createDeployment()
			.addZipInputStream(new ZipInputStream(new FileInputStream("/home/dev/workspace/activiti-rest-cbs/src-work/com/cbs/test/business_model_v8.87.zip"))).deploy();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		RuntimeService runtimeService = activitiRule.getRuntimeService();
//		Map<String, Object> variableMap = new HashMap<String, Object>();
//		variableMap.put("name", "Activiti");
//		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProcess", variableMap);
//		assertNotNull(processInstance.getId());
//		System.out.println("id " + processInstance.getId() + " "
//				+ processInstance.getProcessDefinitionId());
	}

}
