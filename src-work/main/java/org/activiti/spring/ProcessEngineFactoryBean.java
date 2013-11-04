package org.activiti.spring;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.SpringBeanFactoryProxyMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.cbs.persistence.service.EventService;

public class ProcessEngineFactoryBean implements FactoryBean<ProcessEngine>, DisposableBean, ApplicationContextAware {

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected ApplicationContext applicationContext;
  protected ProcessEngineImpl processEngine;
  
  public void destroy() throws Exception {
    if (processEngine != null) {
      processEngine.close();
    }
  }
  
  public ApplicationContext getApplicationContext() {
	  return applicationContext;
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	  this.applicationContext = applicationContext;
//	  EventService1 eventService = applicationContext.getBean("eventService",EventService1.class);
//	  eventService.createNewTable("ACT_CBS_EVENT");
  }

  public ProcessEngine getObject() throws Exception {
    initializeExpressionManager();
    initializeTransactionExternallyManaged();
    
    if (processEngineConfiguration.getBeans()==null) {
      processEngineConfiguration.setBeans(new SpringBeanFactoryProxyMap(applicationContext));
    }
    
    processEngine = (ProcessEngineImpl) processEngineConfiguration.buildProcessEngine();

    return processEngine;
  }

  protected void initializeExpressionManager() {
    if (processEngineConfiguration.getExpressionManager() == null && applicationContext != null) {
      processEngineConfiguration.setExpressionManager(
          new SpringExpressionManager(applicationContext, processEngineConfiguration.getBeans()));
    }
  }
  
  protected void initializeTransactionExternallyManaged() {
    if (processEngineConfiguration instanceof SpringProcessEngineConfiguration) { // remark: any config can be injected, so we cannot have SpringConfiguration as member
      SpringProcessEngineConfiguration engineConfiguration = (SpringProcessEngineConfiguration) processEngineConfiguration;
      if (engineConfiguration.getTransactionManager() != null) {
        processEngineConfiguration.setTransactionsExternallyManaged(true);
      }
    }
  }
  
  public Class<ProcessEngine> getObjectType() {
    return ProcessEngine.class;
  }

  public boolean isSingleton() {
    return true;
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

  
  public void setProcessEngineConfiguration(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
  }
}
