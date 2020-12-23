package com.cts.cj.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cts.cj.constants.EmailEventType;
import com.cts.cj.constants.EmailSendStatus;
import com.cts.cj.domain.EmailMessage;
import com.cts.cj.repository.MessageRepository;


@Service
public class MessageServiceImpl implements MessageService{
	
	
	@Autowired
	MessageRepository messagerepository;
	
	@Override
	public EmailMessage saveEvent(EmailMessage message) {
		
		EmailMessage eventmsg= new EmailMessage();
		eventmsg.setMessage(message.getMessage());
		eventmsg.setSubject(message.getSubject());
		eventmsg.setRecieverEmail(message.getRecieverEmail());
		eventmsg.setEmaileventType(EmailEventType.getByName("signup"));
		eventmsg.setEmailsendStatus(EmailSendStatus.PENDING);
	    return messagerepository.save(eventmsg);
		
		
}

	@Override
	public List<EmailMessage> getMessage() {
		return messagerepository.getMessage();
		
	}
}
