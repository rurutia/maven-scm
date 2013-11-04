package com.cbs.rest.api.indentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.AbstractQuery;
import org.activiti.engine.query.Query;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.api.AbstractPaginateList;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.RequestUtil;
import org.restlet.data.Form;

public class UserGroupsPaginateList extends AbstractPaginateList {

	@SuppressWarnings("rawtypes")
	@Override
	protected List processList(List list)  {
		List<UserGroupInfo> groupList = new ArrayList<UserGroupInfo>();
		for (Object instance : list) {
			Group group = (Group) instance;
			UserGroupInfo groupInfo = new UserGroupInfo(group);	
			List<User> users = ActivitiUtil.getIdentityService()
						.createUserQuery().memberOfGroup(group.getId())
						.list();
			
			List<String> userIds = new ArrayList<String>();
			for(User user : users)
				userIds.add(user.getId());
			
			groupInfo.setUsers(userIds);

			groupList.add(groupInfo);
		}
		return groupList;
	}
	
	 @SuppressWarnings("rawtypes")
	  public DataResponse paginateList(Form form, Query query,
	      String defaultSort, Map<String, QueryProperty> properties) {
	    
	    // Collect parameters
	    int start = RequestUtil.getInteger(form, "start", 0);
	    int size = RequestUtil.getInteger(form, "size", 100);
	    String sort = form.getValues("sort");
	    if(sort == null) {
	      sort = defaultSort;
	    }
	    String order = form.getValues("order");
	    if(order == null) {
	      order = "asc";
	    }

	    // Sort order
	    if (sort != null && properties.size() > 0) {
	      QueryProperty qp = properties.get(sort);
	      if (qp == null) {
	        throw new ActivitiIllegalArgumentException("Value for param 'sort' is not valid, '" + sort + "' is not a valid property");
	      }
	      ((AbstractQuery) query).orderBy(qp);
	      if (order.equals("asc")) {
	        query.asc();
	      }
	      else if (order.equals("desc")) {
	        query.desc();
	      }
	      else {
	        throw new ActivitiIllegalArgumentException("Value for param 'order' is not valid : '" + order + "', must be 'asc' or 'desc'");
	      }
	    }

	    // Get result and set pagination parameters
	    List list = processList(query.listPage(start, size));
	    DataResponse response = new DataResponse();
	    response.setStart(start);
	    response.setSize(list.size()); 
	    response.setSort(sort);
	    response.setOrder(order);
	    response.setTotal(query.count());
	    response.setData(list);
	    return response;
	  }

}
