package com.cbs.process.sale;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

/**
 *  Utility Class to Update task due date
 *
 */

public class TaskDueDateSupport {
	
	public static Date afterDays(int days) {
		Date now = new Date();
		Date due = DateUtils.addDays(now, days);
		return due;
	}
	
	public static Date afterHours(int hours) {
		Date now = new Date();
		Date due = DateUtils.addHours(now, hours);
		return due;
	}
	
	public static Date afterSeconds(int seconds) {
		Date now = new Date();
		Date due = DateUtils.addDays(now, seconds);
		return due;
	}

}
