package com.cts.cj.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cts.cj.domain.ChatMessege;
import com.cts.cj.dto.MesiBoApiRequestData;
import com.cts.cj.dto.MessageRequestResponse;
import com.cts.cj.dto.UserResponse;
import com.cts.cj.service.ChatMessegeService;
import com.cts.cj.service.UserChatService;

@RestController
public class ChatMessegeController {
	
	@Value("${i2i.secure.context_path}")
	private String secureContextPath;
	
	
	@Value("${i2i.non_secure.context_path}")
	private String nonsecureContextPath;
	
	
	@Autowired
	ChatMessegeService chatMessegeService; 
	
	@Autowired
	UserChatService chatService; 
	
	/**
	 * @param userId
	 * @return
	 */
	@CrossOrigin("*")
	@GetMapping(path ="${i2i.secure.context_path}"+"/ua/members/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<UserResponse>> getUserMembers(@PathVariable Long userId) {
		return new ResponseEntity<List<UserResponse>>(chatMessegeService.findAllMembers(userId), HttpStatus.OK);
	}
	
	/**
	 * @param toUserId
	 * @param fromUserIdg
	 * @param pageNo
	 * @param timeStr
	 * @return
	 */
	/** method commented for future use @CrossOrigin("*")
	@GetMapping(path = "${i2i.secure.context_path}"+"/ua/messages/{toUserId}/{fromUserId}/{pageNo}/{timeStr}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<MessageRequestResponsePaginated> getUserMessages(@PathVariable Long toUserId,
			@PathVariable Long fromUserId, @PathVariable Integer pageNo, @PathVariable String timeStr) {
		return new ResponseEntity<MessageRequestResponsePaginated>(chatMessegeService.getMessagesByUser(toUserId, fromUserId, pageNo, timeStr), HttpStatus.OK);
	}**/
	
	@CrossOrigin("*")
	@GetMapping(path = "${i2i.secure.context_path}"+ "/ua/messages/{toUserId}/{fromUserId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<ChatMessege>> getUserMessages(@PathVariable Long toUserId,	@PathVariable Long fromUserId) {
		return new ResponseEntity<List<ChatMessege>>(chatMessegeService.getMessegesByUser(toUserId, fromUserId), HttpStatus.OK);
	}

	/**
	 * @param messageRequestResponse
	 * @return
	 */
	@CrossOrigin("*")
	@PostMapping(path ="${i2i.secure.context_path}"+"/ua/save-message", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<MessageRequestResponse> saveUserMessage(
			@RequestBody MessageRequestResponse messageRequestResponse) {
		return new ResponseEntity<MessageRequestResponse>(chatMessegeService.saveUserMessage(messageRequestResponse),HttpStatus.CREATED);
	}
	
	/**
	 * @param userId
	 * @return
	 */
	@CrossOrigin("*")
	@GetMapping(path ="${i2i.secure.context_path}"+"/ua/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
		return new ResponseEntity<UserResponse>(chatMessegeService.findUserById(userId), HttpStatus.OK);
	}
	
	  @CrossOrigin("*")	  
	  @PostMapping(path ="${i2i.secure.context_path}/mesiboApicall", produces = MediaType.APPLICATION_JSON_UTF8_VALUE) 
	  public ResponseEntity<String>  getMesiBoUserSession(@RequestBody MesiBoApiRequestData requestdata) { 
		  return new  ResponseEntity<String>(chatService.getMesiboUserDetails(requestdata.getOp(), requestdata.getToken(), requestdata.getAppId(), requestdata.getEmail()),  HttpStatus.OK);
	  }
	 
	
}
