package com.cts.cj.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cts.cj.constants.EmailEventType;
import com.cts.cj.constants.EmailSendStatus;
import com.cts.cj.constants.CJMessageConstant;
import com.cts.cj.constants.Status;
import com.cts.cj.domain.EmailMessage;
import com.cts.cj.domain.Meeting;
import com.cts.cj.domain.User;
import com.cts.cj.repository.MeetingRepository;
import com.cts.cj.repository.UserRepository;
import com.cts.cj.util.CJUtil;
import com.cts.cj.util.CJUtilCommon;

@Service
public class MeetingServiceImpl implements MeetingService {

	@Autowired
	MeetingRepository meetingrepository;
	
	@Autowired
	UserService userService;
	
	@Autowired
	CJUtilCommon i2iCommonUtil;
	
	@Autowired
	UserRepository userRepository;

	@Override
	public Meeting saveEvent(Meeting meeting) {

		Meeting scheduledMeeting = new Meeting();
		scheduledMeeting.setTitle(meeting.getTitle());
		scheduledMeeting.setDescription(meeting.getDescription());
		scheduledMeeting.setStart(meeting.getStart());
		scheduledMeeting.setEnd(meeting.getEnd());
		scheduledMeeting.setUserId(meeting.getUserId());

		return meetingrepository.save(scheduledMeeting);
	}

	@Override
	public Meeting getMeeting(Long meetingId) {
		Meeting meeting= meetingrepository.getMeeting(meetingId);
		meeting.setMeetingTime(meeting.getStart(), meeting.getEnd());
		return meeting;
		
	}

	@Override
	public List<Meeting> getAllMeeting(Long userId) {
		List<Meeting> meeting= meetingrepository.getMeetingList(userId);
		List<Meeting> editedMeting= new ArrayList<>();
		for(Meeting meetingschedule:meeting) {
			Meeting meetingtim= new Meeting();
			meetingtim.setMeetingId(meetingschedule.getMeetingId());
			meetingtim.setDescription(meetingschedule.getDescription());
			meetingtim.setStart(meetingschedule.getStart());
			meetingtim.setEnd(meetingschedule.getEnd());
			meetingtim.setUserId(meetingschedule.getUserId());
			meetingtim.setTitle(meetingschedule.getTitle());
			meetingtim.setMeetingTime(meetingschedule.getStart(), meetingschedule.getEnd());
			editedMeting.add(meetingtim);
		}
		return editedMeting;
	}

	@Override
	public Meeting update(Meeting meeting) {
	Meeting existingMeeting = meetingrepository.getMeeting(meeting.getMeetingId());		
	    existingMeeting.setStart(meeting.getStart());
	    existingMeeting.setEnd(meeting.getEnd());
	    existingMeeting.setTitle(meeting.getTitle());
	    existingMeeting.setStatus(meeting.getStatus());
	    existingMeeting.setUserId(meeting.getUserId());
	    existingMeeting.setDescription(meeting.getDescription());
	    Meeting updatedMeeting= meetingrepository.save(existingMeeting);
	    updatedMeeting.setMeetingTime(updatedMeeting.getStart(), updatedMeeting.getEnd());  
	    return updatedMeeting;
	    

	}

	@Override
	public Meeting canCelMeeting(Meeting meeting) {
		List<EmailMessage> emailmsg = new ArrayList<>();
		Meeting existingMeeting = meetingrepository.getMeeting(meeting.getMeetingId());
		existingMeeting.setStatus(Status.INACTIVE);
		User user = userService.getById(meeting.getUserId());
		String cancelmsg = CJMessageConstant.CANCELL_MEETING;
		String cancellmsg = CJUtil.cancelMeeting(user, cancelmsg);
		EmailMessage msg = new EmailMessage();
		msg.setRecieverEmail(user.getEmailId());
		msg.setMessage(cancellmsg);
		msg.setSubject(CJMessageConstant.CANCELL_MEETING_SUBJECT);
		msg.setEmaileventType(EmailEventType.getByName("cancelmeeting"));
		msg.setEmailsendStatus(EmailSendStatus.PENDING);
		emailmsg.add(msg);
		//user.setEmailMessege(emailmsg);
		userRepository.save(user);
		return meetingrepository.save(existingMeeting);
		

	}

	

}
