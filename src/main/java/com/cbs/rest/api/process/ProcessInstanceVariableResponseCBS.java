package com.cbs.rest.api.process;

import java.util.Map;

public class ProcessInstanceVariableResponseCBS {
	
	Map<String, String> variables;
	
	ProcessInstanceVariableResponseCBS(Map<String, String> variables) {
		this.variables = variables;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}

}
