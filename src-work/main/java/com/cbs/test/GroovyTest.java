package com.cbs.test;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.util.Arrays;

public class GroovyTest {
	
	public static void main(String[] args) {
//		Binding binding = new Binding();
//		binding.setVariable("foo", new Integer(2));
//		GroovyShell shell = new GroovyShell(binding);
//		Object value = shell.evaluate("println 'Hello World!'; x = 123; return foo * 10");
//		
//		System.out.println(value);
		
		String  validationScript;
		 Binding binding = new Binding();
		 binding.setVariable("foo", new Integer(2));
		
		
//		 binding.setVariable("properties", properties);
		 GroovyShell shell = new GroovyShell(binding);
		 validationScript = "if(true ){ println 'aaa';};return 10;";
		 Object value = shell.evaluate(validationScript);
		 System.out.println(value);
	
	}

}
