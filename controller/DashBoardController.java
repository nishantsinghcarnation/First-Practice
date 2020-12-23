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

import com.cts.cj.domain.PostPrevious;
import com.cts.cj.dto.SharePostResponse;
import com.cts.cj.dto.UserResponse;
import com.cts.cj.service.DasBoardService;

@RestController
public class DashBoardController {

	public static final Logger logger = LoggerFactory.getLogger(DashBoardController.class);

	@Autowired
	DasBoardService dasBoardService;

/**
 * @param userId
 * @param roleType
 * @return
 */
	@CrossOrigin("*")
	@GetMapping(path = "${i2i.secure.context_path}"+ "/ua/findMyMentorMenties/{userId}/{roleType}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<UserResponse>> getMyMentorMenties(@PathVariable Long userId,@PathVariable String roleType) {
		return new ResponseEntity<List<UserResponse>>(dasBoardService.findMyMentorMenties(userId, roleType),HttpStatus.OK);
	}

	/**
	 * @param post
	 * @return
	 */
	@CrossOrigin("*")
	@PostMapping(path = "${i2i.secure.context_path}"+ "/ua/createPost", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<PostPrevious> createPost(@RequestBody PostPrevious post) {
		return new ResponseEntity<PostPrevious>(dasBoardService.createPost(post), HttpStatus.OK);

	}
	
	/**
	 * @return
	 */
	@CrossOrigin("*")
	@GetMapping(value = "${i2i.secure.context_path}/ua/getAllPost/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<SharePostResponse>> getAllPost(@PathVariable Long id) {
		return new ResponseEntity<List<SharePostResponse>>(dasBoardService.getAllPost(id), HttpStatus.OK);

	}

}
