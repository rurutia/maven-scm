package com.cbs.persistence.mapper;

import org.apache.ibatis.annotations.Param;

public interface TimerMapper {
//	#{dueDate}
//	WHERE TYPE_="timer" and EXECUTION_ID_=#{executionId} and HANDLER_CFG_=#{timerId}
	public void updateTimer(@Param("dueDate") String dueDate,
			                @Param("executionId") String executionId,
	                        @Param("timerId") String timerId
	                       );

}
