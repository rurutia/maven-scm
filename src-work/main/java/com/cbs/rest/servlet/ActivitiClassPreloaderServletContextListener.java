//package com.cbs.rest.servlet;
//
//import java.io.IOException;
//
//import javax.servlet.ServletContextEvent;
//import javax.servlet.ServletContextListener;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * 
// * Pre-load classes that will be used by Activiti Model 
// *
// */
//
//public class ActivitiClassPreloaderServletContextListener implements ServletContextListener{
//
//	protected static final Logger LOGGER = LoggerFactory.getLogger(ActivitiClassPreloaderServletContextListener.class);
//	protected static final String PRELOADED_PACKAGES = "cbs_packages";
//
//	@SuppressWarnings("rawtypes")
//	public void contextInitialized(ServletContextEvent event) {
//		try {
//			LOGGER.info("Starting to load necessary classes before Activiti engine starts");
//			String preloaded_packages = event.getServletContext().getInitParameter(PRELOADED_PACKAGES);
//			for(String package_name : preloaded_packages.split("\n")) {
//				for(Class cbsClass : PackageClassLoader.getClasses(package_name.trim())) {
//					Class.forName(cbsClass.getName());
//					LOGGER.info(cbsClass.getName());
//				}
//			}
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//	}
//
//	public void contextDestroyed(ServletContextEvent event) {
//	}
//
//}
