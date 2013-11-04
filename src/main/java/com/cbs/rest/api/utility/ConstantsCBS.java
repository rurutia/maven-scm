package com.cbs.rest.api.utility;

/**
 * CBS activiti rest constants used across packages
 *
 */
public class ConstantsCBS {

	// task local variables
	public static String VARIABLE_TYPE_ASSIGNMENT_TIME = "assignmentTime";
	public static String VARIABLE_TYPE_IN_PROGRESS_TIME = "inProgressTime";
	public static String VARIABLE_TYPE_CAN_START_TIME = "canStartTime";
	public static String VARIABLE_TYPE_IS_DEADLINE = "isDeadline";
	public static String VARIABLE_TYPE_PRIORITY_CHANGE_TIME = "priorityChangeTime";
	public static String VARIABLE_TYPE_PRIORITY_FUTURE = "priorityFuture";
	public static String VARIABLE_TYPE_PRIORITY_FUTURE_READABLE = "priorityFutureReadable";
	public static String VARIABLE_TYPE_TIMER_COUNT = "timerCount";
	public static String VARIABLE_TYPE_TASK_LOG = "taskLog";
	public static String VARIABLE_TYPE_TASK_LOG_JSON = "taskLogJSON";
	public static String VARIABLE_TYPE_TASK_IS_WAITING = "waiting";
	public static String VARIABLE_TYPE_TASK_WAITING_REASON = "waitingReason";
	public static String VARIABLE_TYPE_CALLING_PROCESS_TREE = "callingProcessTreeId";
	public static String VARIABLE_PROCESS_DEPLOYMENT_ID = "processDeploymentId";

	// process variables
	public static String VARIABLE_TYPE_CANDIDATE_LIST = "candidateAssignees";
	public static String VARIABLE_TYPE_WATCHER_LIST = "watcherList";
	public static String VARIABLE_TYPE_TASK_CANDIDATE_GROUP_MAP = "taskCandidateGroupMap";

	// adhoc process related
	public final static String MESSAGE_CREATE_ADHOC_ACTIVITY = "callAdhoc";
	public final static String MESSAGE_CANCEL_ADHOC_TIMER = "cancelTimer";
	
	public final static String ADHOC_PROCESS_DEFINITION = "adhoc";
	public final static String ADHOC_CHILD_PROCESS_ID = "adhocChildId";
	public final static String ADHOC_PARENT_PROCESS_ID = "adhocParentId";
	public final static String ADHOC_CALLING_PROCESS_TREE_ID = "callingProcessTreeId";


	// model elements naming conventions
	public static String BOUNDARY_TIMER_PRIORITY_ID = "boundarytimer_priority_";

	public static String SEPERATOR_COMMA = ",";
	
	// process repsonse type
	public static String PROCESS_RESPONSE_TYPE_ADHOC_ACTIVE = "process_response_adhoc_active";
	public static String PROCESS_RESPONSE_TYPE_ADHOC_HISTORIC = "process_response_adhoc_historic";
	public static String PROCESS_RESPONSE_TYPE_SINGLE_HISTORIC = "process_response_single_historic";
	
	// to be removed
	public static String FORM_PROPERTY_TYPE_EMAIL = "email";
}
