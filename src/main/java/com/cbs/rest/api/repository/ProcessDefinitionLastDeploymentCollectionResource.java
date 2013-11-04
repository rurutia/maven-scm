package com.cbs.rest.api.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.ProcessDefinitionQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.api.repository.ProcessDefinitionsPaginateList;
import org.restlet.data.Form;
import org.restlet.resource.Get;

/*
 * get process definitions from last deployment
 */
public class ProcessDefinitionLastDeploymentCollectionResource extends SecuredResource {

	private static final Map<String, QueryProperty> properties = new HashMap<String, QueryProperty>();

	static {
		properties.put("id", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_ID);
		properties.put("key", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_KEY);
		properties.put("category", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_CATEGORY);
		properties.put("name", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_NAME);
		properties.put("version", ProcessDefinitionQueryProperty.PROCESS_DEFINITION_VERSION);
		properties.put("deploymentId", ProcessDefinitionQueryProperty.DEPLOYMENT_ID);
	}

	@Get
	public DataResponse getProcessDefinitions() {
		if(authenticate() == false) return null;
		
		ProcessDefinitionQuery processDefinitionQuery = ActivitiUtil.getRepositoryService().createProcessDefinitionQuery();
		processDefinitionQuery.deploymentId(getLatestDeploymentId());
		Form query = getQuery();
		Set<String> names = query.getNames();

		// Populate filter-parameters
		if(names.contains("category")) {
			processDefinitionQuery.processDefinitionCategory(getQueryParameter("category", query));
		}
		if(names.contains("categoryLike")) {
			processDefinitionQuery.processDefinitionCategoryLike(getQueryParameter("categoryLike", query));
		}
		if(names.contains("categoryNotEquals")) {
			processDefinitionQuery.processDefinitionCategoryNotEquals(getQueryParameter("categoryNotEquals", query));
		}
		if(names.contains("key")) {
			processDefinitionQuery.processDefinitionKey(getQueryParameter("key", query));
		}
		if(names.contains("keyLike")) {
			processDefinitionQuery.processDefinitionKeyLike(getQueryParameter("keyLike", query));
		}
		if(names.contains("name")) {
			processDefinitionQuery.processDefinitionName(getQueryParameter("name", query));
		}
		if(names.contains("nameLike")) {
			processDefinitionQuery.processDefinitionNameLike(getQueryParameter("nameLike", query));
		}
		if(names.contains("resourceName")) {
			processDefinitionQuery.processDefinitionResourceName(getQueryParameter("resourceName", query));
		}
		if(names.contains("resourceNameLike")) {
			processDefinitionQuery.processDefinitionResourceNameLike(getQueryParameter("resourceNameLike", query));
		}
		if(names.contains("version")) {
			processDefinitionQuery.processDefinitionVersion(getQueryParameterAsInt("version", query));
		}
		if(names.contains("suspended")) {
			Boolean suspended = getQueryParameterAsBoolean("suspended", query);
			if(suspended != null) {
				if(suspended) {
					processDefinitionQuery.suspended();
				} else {
					processDefinitionQuery.active();
				}
			}
		}
		if(names.contains("latest")) {
			Boolean latest = getQueryParameterAsBoolean("latest", query);
			if(latest != null && latest) {
				processDefinitionQuery.latestVersion();
			}
		}
		if(names.contains("startableByUser")) {
			processDefinitionQuery.startableByUser(getQueryParameter("startableByUser", query));
		}

		return new ProcessDefinitionsPaginateList(this).paginateList(getQuery(), processDefinitionQuery, "name", properties);
	}

	private String getLatestDeploymentId() {
		List<Deployment> deployments = ActivitiUtil.getRepositoryService()
				.createDeploymentQuery()
				.orderByDeploymenTime().desc()
				.listPage(0, 1);
		
		return deployments.get(0).getId();
	}
}
