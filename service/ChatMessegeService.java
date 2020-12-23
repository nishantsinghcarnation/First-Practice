package com.cts.cj.service;

import java.util.List;

import com.cts.cj.domain.ChatMessege;
import com.cts.cj.dto.MessageRequestResponse;
import com.cts.cj.dto.MessageRequestResponsePaginated;
import com.cts.cj.dto.UserResponse;

public interface ChatMessegeService {

	public List<UserResponse> findAllMembers(Long userId);
	
	public UserResponse findUserById(Long userId);
	
	public MessageRequestResponsePaginated getMessagesByUser(Long toUserId, Long fromUserId, Integer pageNo,
			String timeStr);
	
	public List<ChatMessege> getMessegesByUser(Long toUserId, Long fromUserId);

	public MessageRequestResponse saveUserMessage(MessageRequestResponse messageRequestResponse);
	


}
