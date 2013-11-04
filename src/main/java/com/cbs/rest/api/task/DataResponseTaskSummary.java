package com.cbs.rest.api.task;

import java.util.List;
import java.util.Map;

/**
 * @author michaelyu
 *
 */
public class DataResponseTaskSummary {

	Map<String, Map<String, Integer>> data;

	public Map<String, Map<String, Integer>> getData() {
		return data;
	}

	public void setData(Map<String, Map<String, Integer>> data) {
		this.data = data;
	}

}
