package com.cts.cj.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cts.cj.domain.Meeting;
import com.cts.cj.service.MeetingService;


@RestController
public class MeetingController {
	
	
	
	@Autowired
	MeetingService meetingService;
	
	public static final Logger logger = LoggerFactory.getLogger(MeetingController.class);
	
	/**
	 * @param message
	 * @return
	 * @throws com.cts.cj.exception.EventAlreayScheduled
	 */
	@CrossOrigin("*")
	@PostMapping(path ="${i2i.secure.context_path}"+"/ua/saveMeeting", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Meeting> scheduleMeeting(@RequestBody Meeting meeting)
			throws com.cts.cj.exception.EventAlreayScheduled {
	     
			logger.debug("MeetingTitle: " + meeting.getTitle() + " userid:  " + meeting.getUserId());
	        return new ResponseEntity<Meeting>(meetingService.saveEvent(meeting), HttpStatus.OK);

	}
	
	
	/**
	 * @param meetingid
	 * @return
	 * @throws com.cts.cj.exception.EventAlreayScheduled
	 */
	@CrossOrigin("*")
	@GetMapping(path ="${i2i.secure.context_path}"+"/ua/getMeetingById/{meetingId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Meeting> getMeeting(@PathVariable Long meetingId)
			throws com.cts.cj.exception.EventAlreayScheduled {
	     	logger.debug("MeetingId: " +meetingId );
	        return new ResponseEntity<Meeting>(meetingService.getMeeting(meetingId), HttpStatus.OK);

	}
	
	
	
	/**
	 * @param userId
	 * @return
	 */
	@CrossOrigin("*")
	@GetMapping(value ="${i2i.secure.context_path}"+"/ua/getMeetingsByUserId/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<Meeting>> getMeetingsByUserId(@PathVariable Long userId) {
		List<Meeting> userMeetings = meetingService.getAllMeeting(userId);
		logger.info("Retriving the  list of  Users");
		if (userMeetings.isEmpty()) {
			return new ResponseEntity<List<Meeting>>(HttpStatus.NO_CONTENT);

		}
		return new ResponseEntity<List<Meeting>>(userMeetings, HttpStatus.OK);
	}
	
	
	/**
	 * @param meeting
	 * @return
	 * @throws com.cts.cj.exception.EventAlreayScheduled
	 */
	@CrossOrigin("*")
	@PostMapping(path ="${i2i.secure.context_path}"+"/ua/modifyMeeting", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Meeting> modifyMeeting(@RequestBody Meeting meeting)
			throws com.cts.cj.exception.EventAlreayScheduled {
		   			logger.debug("MeetingTitle: " + meeting.getTitle() + " userid:  " + meeting.getUserId());
	        return new ResponseEntity<Meeting>(meetingService.update(meeting), HttpStatus.OK);

	}
	
	/**
	 * @param meeting
	 * @return
	 * @throws com.cts.cj.exception.MeetingNotFoundException
	 */
	@CrossOrigin("*")
	@PostMapping(path ="${i2i.secure.context_path}"+"/ua/cancelMeeting", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Meeting> cancelMeeting(@RequestBody Meeting meeting)
			throws com.cts.cj.exception.MeetingNotFoundException {
		   
			logger.debug("MeetingTitle: " + meeting.getTitle() + " userid:  " + meeting.getUserId());
	        return new ResponseEntity<Meeting>(meetingService.canCelMeeting(meeting), HttpStatus.OK);

	}

}
