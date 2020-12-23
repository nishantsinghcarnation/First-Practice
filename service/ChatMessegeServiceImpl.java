package com.cts.cj.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.cts.cj.constants.CJCommonConstants;
import com.cts.cj.domain.ChatMessege;
import com.cts.cj.domain.User;
import com.cts.cj.dto.MessageRequestResponse;
import com.cts.cj.dto.MessageRequestResponsePaginated;
import com.cts.cj.dto.UserResponse;
import com.cts.cj.repository.ChatMessegeRepository;
import com.cts.cj.repository.UserRepository;
import com.cts.cj.util.BeanMapperUtil;

@Service
public class ChatMessegeServiceImpl implements ChatMessegeService {

	@Autowired
	ChatMessegeRepository chatMessegeRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	BeanMapperUtil beanMapperUtil;

	@Override
	public List<UserResponse> findAllMembers(Long userId) {
		List<User> users = chatMessegeRepository.findAllFriend(userId);
		return beanMapperUtil.mapAsUserResponseList(users);
	}

	@Override
	public MessageRequestResponsePaginated getMessagesByUser(Long recieverId, Long senderId, Integer pageNo,
			String timeStr) {

		PageRequest pageable = PageRequest.of(pageNo, CJCommonConstants.USER_MESSAGE_PAGE_SIZE);
		List<MessageRequestResponse> messageRequestResponse = beanMapperUtil.mapAsMessageRequestResponseList(
				chatMessegeRepository.findMessageByUser(recieverId, senderId, timeStr, pageable));

		MessageRequestResponsePaginated messageRequestResponsePaginated = new MessageRequestResponsePaginated(pageNo,
				timeStr, messageRequestResponse);
		return messageRequestResponsePaginated;
	}

	@Override
	public MessageRequestResponse saveUserMessage(MessageRequestResponse messageRequestResponse) {
		ChatMessege messege = new ChatMessege();
		messege.setSenderId(messageRequestResponse.getRecieverId());
		messege.setRecieverId(messageRequestResponse.getSenderId());
		messege.setMessege(messageRequestResponse.getMessage());
		messege.setCreated(LocalDateTime.now());
		chatMessegeRepository.save(messege);
		return beanMapperUtil.mapAsMessageRequestResponse(messege);
	}

	@Override
	public UserResponse findUserById(Long userId) {
		return beanMapperUtil.mapAsUserResponse(
				userRepository.getUserbyId(userId) != null ? userRepository.findById(userId).get() : null);

	}

	@Override
	public List<ChatMessege> getMessegesByUser(Long recieverId, Long senderId) {
		List<ChatMessege> messageRequestResponse =chatMessegeRepository.findMessegesByUser(recieverId, senderId);
		List<ChatMessege> editedChatMessege= new ArrayList<>();
		for(ChatMessege chatMesseges:messageRequestResponse) {
			ChatMessege chatMessege= new ChatMessege();
		 	chatMessege.setChatedDate(chatMesseges.getCreated().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
			chatMessege.setCreated(chatMesseges.getCreated());
			chatMessege.setMessege(chatMesseges.getMessege());
			chatMessege.setRecieverId(chatMesseges.getRecieverId());
			chatMessege.setSenderId(chatMesseges.getSenderId());
			chatMessege.setMessageId(chatMesseges.getMessageId());
			editedChatMessege.add(chatMessege);
		}
		
		return editedChatMessege;
	}

}
