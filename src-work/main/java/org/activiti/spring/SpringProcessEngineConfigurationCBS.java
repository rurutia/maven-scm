package org.activiti.spring;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.db.IbatisVariableTypeHandler;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.variable.VariableType;
import org.apache.commons.lang.ObjectUtils;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.type.JdbcType;

public class SpringProcessEngineConfigurationCBS extends SpringProcessEngineConfiguration {
	
	  public static SpringProcessEngineConfigurationCBS config;
	  
	  protected void init() {
		    initHistoryLevel();
		    initExpressionManager();
		    initVariableTypes();
		    initBeans();
		    initFormEngines();
		    initFormTypes();
		    initScriptingEngines();
		    initBusinessCalendarManager();
		    initCommandContextFactory();
		    initTransactionContextFactory();
		    initCommandExecutors();
		    initServices();
		    initIdGenerator();
		    initDeployers();
		    initJobExecutor();
		    initDataSource();
		    initTransactionFactory();
		    initSqlSessionFactory();
		    initSessionFactories();
		    initJpa();
		    initDelegateInterceptor();
		    initEventHandlers();
		    initFailedJobCommandFactory();
		    initConfigurators();
		    SpringProcessEngineConfigurationCBS.config = this;
		  }
	
	  protected void initSqlSessionFactory() {
		    if (sqlSessionFactory==null) {
		      InputStream inputStream = null;
		      try {
		        inputStream = getMyBatisXmlConfigurationSteam();

		        // update the jdbc parameters to the configured ones...
		        Environment environment = new Environment("default", transactionFactory, dataSource);
		        Reader reader = new InputStreamReader(inputStream);
		        Properties properties = new Properties();
		        properties.put("prefix", databaseTablePrefix);
		        if(databaseType != null) {
		          properties.put("limitBefore" , DbSqlSessionFactory.databaseSpecificLimitBeforeStatements.get(databaseType));
		          properties.put("limitAfter" , DbSqlSessionFactory.databaseSpecificLimitAfterStatements.get(databaseType));
		          properties.put("limitBetween" , DbSqlSessionFactory.databaseSpecificLimitBetweenStatements.get(databaseType));
		          properties.put("limitOuterJoinBetween" , DbSqlSessionFactory.databaseOuterJoinLimitBetweenStatements.get(databaseType));
		          properties.put("orderBy" , DbSqlSessionFactory.databaseSpecificOrderByStatements.get(databaseType));
		          properties.put("limitBeforeNativeQuery" , ObjectUtils.toString(DbSqlSessionFactory.databaseSpecificLimitBeforeNativeQueryStatements.get(databaseType)));
		        }
		        XMLConfigBuilder parser = new XMLConfigBuilder(reader,"", properties);
		        Configuration configuration = parser.getConfiguration();
		        configuration.setEnvironment(environment);
		        configuration.getTypeHandlerRegistry().register(VariableType.class, JdbcType.VARCHAR, new IbatisVariableTypeHandler());
		        configuration = parser.parse();

		        sqlSessionFactory = new DefaultSqlSessionFactory(configuration);

		      } catch (Exception e) {
		        throw new ActivitiException("Error while building ibatis SqlSessionFactory: " + e.getMessage(), e);
		      } finally {
		        IoUtil.closeSilently(inputStream);
		      }
		    }
		  }

}
