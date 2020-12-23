package com.cts.cj.service;

import java.util.List;

import com.cts.cj.domain.Meeting;

public interface MeetingService {

	/**
	 * @param message
	 * @return
	 */
	public Meeting saveEvent(Meeting meeting);

	public Meeting getMeeting(Long meetingId);

	List<Meeting> getAllMeeting(Long userId);

	public Meeting update(Meeting meeting);

	public Meeting canCelMeeting(Meeting meeting);

}
