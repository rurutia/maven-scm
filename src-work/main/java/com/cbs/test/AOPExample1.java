package com.cbs.test;

import org.springframework.aop.framework.ProxyFactory;

public class AOPExample1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MessageWriter target = new MessageWriter();
		// create the proxy
		ProxyFactory pf = new ProxyFactory();
		pf.addAdvice(new MessageDecorator());
		pf.setTarget(target);
		MessageWriter proxy = (MessageWriter) pf.getProxy();
		// write the messages target.writeMessage();
		target.writeMessage1();
		System.out.println("");
		proxy.writeMessage1();

	}

}
