package com.cts.cj.service;

import java.util.List;

import com.cts.cj.domain.EmailMessage;


public interface MessageService {
	
	/**
	 * @param message
	 * @return
	 */
	public EmailMessage saveEvent(EmailMessage message);  
	
	List<EmailMessage> getMessage();

}
