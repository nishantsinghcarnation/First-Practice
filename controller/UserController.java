package com.cts.cj.controller;

import java.util.List;

import javax.mail.MessagingException;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cts.cj.domain.ChangePassword;
import com.cts.cj.domain.Group;
import com.cts.cj.domain.Post;
import com.cts.cj.domain.Prospect;
import com.cts.cj.domain.ResetPassword;
import com.cts.cj.domain.SentInvitesList;
import com.cts.cj.domain.User;
import com.cts.cj.domain.UserConnection;
import com.cts.cj.dto.CommentDto;
import com.cts.cj.dto.EmailMessageDto;
import com.cts.cj.dto.GroupDto;
import com.cts.cj.dto.GroupMessageDto;
import com.cts.cj.dto.IsUniqueResponse;
import com.cts.cj.dto.PostDto;
import com.cts.cj.dto.PostUrlDto;
import com.cts.cj.dto.SearchDto;
import com.cts.cj.dto.UserDto;
import com.cts.cj.exception.UserAlreadyExistsException;
import com.cts.cj.exception.UserNotFoundException;
import com.cts.cj.repository.RegistorRepository;
import com.cts.cj.repository.UserRepository;
import com.cts.cj.service.PostService;
import com.cts.cj.service.UserService;

@RestController
public class UserController {

	public static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	UserService userService;

	@Autowired
	RegistorRepository registorRepository;

	@Autowired
	UserRepository userRepository;

	@Value("${i2i.secure.context_path}")
	private String secureContextPath;

	@Value("${i2i.non_secure.context_path}")
	private String nonsecureContextPath;

	@Autowired
	PostService pstService;
	/**
	 * @param name
	 * @param password
	 * @param emailid
	 * @param mobileNumber
	 * @return
	 * @throws UserAlreadyExistsException
	 * @throws MessagingException
	 */
	@CrossOrigin("*")
	@PostMapping(path = "${i2i.non_secure.context_path}"
			+ "/registerUser", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> RegisterUser(@RequestBody UserDto user)
			throws com.cts.cj.exception.UserAlreadyExistsException, MessagingException {

		logger.debug("Name: " + user.getFirstName() + " emailid:  " + user.getEmailId() + "  mobileNumber: "
				+ user.getMobileNumber());

		return new ResponseEntity<String>(userService.register(user), HttpStatus.OK);

	}

	/**
	 * @param emailId
	 * @return
	 * @throws Exception
	 */
	@CrossOrigin("*")
	@PostMapping(path = "${i2i.secure.context_path}/ua/sendInvitation/{emailIds}/{roleType}/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> sendInvitation(@PathVariable String emailIds, @PathVariable String roleType,
			@PathVariable String userId) throws Exception {
		return new ResponseEntity<String>(userService.sendInvite(emailIds, roleType, userId), HttpStatus.OK);
	}

	/**
	 * @return
	 */
	/*
	 * @CrossOrigin("*")
	 * 
	 * @PostMapping(path =
	 * "${i2i.secure.context_path}/ua/forgetPassword/{emailIds}", produces =
	 * MediaType.APPLICATION_JSON_UTF8_VALUE) public ResponseEntity<String>
	 * forgetpassword(@PathVariable String emailIds) throws Exception { return new
	 * ResponseEntity<String>(userService.forgetpassword(emailIds), HttpStatus.OK);
	 * }
	 */
	/**
	 * @return
	 */
	@CrossOrigin("*")
	@GetMapping(value = "${i2i.secure.context_path}/admin/getAllUsers", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<User>> getAllUsers() {
		List<User> users = userService.getAll();
		logger.info("Retriving the  list of  Users");
		if (users.isEmpty()) {
			return new ResponseEntity<List<User>>(HttpStatus.NO_CONTENT);

		}
		return new ResponseEntity<List<User>>(users, HttpStatus.OK);
	}

	/**
	 * @return
	 */
	@CrossOrigin("*")
	@GetMapping(value = "${i2i.secure.context_path}/ua/getUser/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<User> getUser(@PathVariable Long id) {
		User user = userService.getById(id);
		logger.info("Retrived the user on basis of ID");
		if (null == user) {
			return new ResponseEntity<User>(HttpStatus.NOT_FOUND);

		}
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}

	/**
	 * @param id
	 * @param user
	 * @return
	 */
	@CrossOrigin("*")
	@PutMapping(value = "${i2i.secure.context_path}/ua/updateUser", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Object> updateUser(@RequestBody User user) {
		User existingUser = userService.getById(user.getId());
		logger.info("Retrived the user on basis of ID [{}]", user.getId());
		if (existingUser == null) {
			logger.error("Unable to update. User with id [{}] not found.", user.getId());
			return new ResponseEntity<Object>("user is not found ", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Object>(userService.update(user), HttpStatus.OK);

	}

	/**
	 * @param id
	 * @param user
	 * @return
	 * @throws MessagingException
	 */
	@CrossOrigin("*")
	@PutMapping(value = "${i2i.secure.context_path}/ua/changePassword", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<User> changePassword(@RequestBody User user)
			throws UserNotFoundException, MessagingException {
		logger.info("Retrived the user on basis of ID");
		if (user == null) {
			logger.error("Unable to locate the user with this  ");
			String message = "Please provide the user infromation";
			throw new UserNotFoundException(String.format(message, new String[] { user.getEmailId() }));
		}
		return new ResponseEntity<User>(userService.changePassword(user), HttpStatus.OK);
	}

	@CrossOrigin("*")
	@GetMapping(value = "${i2i.non_secure.context_path}/verifyEmailForUnique/{emailId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<IsUniqueResponse> verifyEmailForUnique(@PathVariable("emailId") String emailId) {
		IsUniqueResponse res = null;
		User alreadyexist = userRepository.findByEmail(emailId);

		if (alreadyexist != null) {
			res = new IsUniqueResponse(false);
		} else {
			res = new IsUniqueResponse(true);
		}
		return new ResponseEntity<IsUniqueResponse>(res, HttpStatus.OK);
	}

	@CrossOrigin("*")
	@GetMapping(value = "${i2i.non_secure.context_path}"
			+ "/verifyMobileForUnique/{mobile}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<IsUniqueResponse> verifyMobileForUnique(@PathVariable("mobile") String mobile) {
		IsUniqueResponse res = null;
		User alreadyexist = userRepository.findByMobile(mobile);

		if (alreadyexist != null) {
			res = new IsUniqueResponse(false);
		} else {
			res = new IsUniqueResponse(true);
		}
		return new ResponseEntity<IsUniqueResponse>(res, HttpStatus.OK);
	}

	/**
	 * @param id
	 * @param user
	 * @return
	 */
	@CrossOrigin("*")
	@PutMapping(value = "${i2i.non_secure.context_path}"
			+ "/forgetPasword", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<User> forgetPasword(@RequestBody User user)
			throws com.cts.cj.exception.UserNotFoundException {
		logger.info("Retrived the user on basis of ID");
		if (user == null) {

			logger.error("Unable to locate the user with this  ");
			String message = "Please send the user infromation";
			throw new UserNotFoundException(message);

		}
		return new ResponseEntity<User>(userService.forgetPwd(user), HttpStatus.OK);
	}

	/**
	 * @param id
	 * @param user
	 * @return
	 */
	@CrossOrigin("*")
	@PutMapping(value = "${i2i.secure.context_path}"
			+ "/admin/changeStatus", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<User> changeStatus(@RequestBody User user) throws com.cts.cj.exception.UserNotFoundException {
		User alreadyexist = userRepository.findByEmail(user.getEmailId());
		logger.info("Retrived the user on basis of Email ID");
		if (alreadyexist == null) {
			logger.error("Unable to locate the user with this  ");
			String message = "The Email ID [%s] is not in system";
			throw new UserNotFoundException(String.format(message, new String[] { user.getEmailId() }));
		}
		return new ResponseEntity<User>(userService.updateUserStatus(user.getEmailId(), user.getStatus()),
				HttpStatus.OK);
	}

	@CrossOrigin("*")
	@PostMapping(path = "${i2i.non_secure.context_path}"
			+ "/registerProspect", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Prospect> RegisterProspect(@RequestBody Prospect prospect)
			throws com.cts.cj.exception.UserAlreadyExistsException {
		logger.debug(" emailid:  " + prospect.getEmailId());

		return new ResponseEntity<Prospect>(userService.registerProspect(prospect), HttpStatus.OK);

	}

	@CrossOrigin("*")
	@PostMapping(value = "nonsecure/forgotpassword/{emailId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> forgotpassword(@PathVariable String emailId) throws Exception {
		return new ResponseEntity<String>(userService.forgotpassword(emailId), HttpStatus.OK);
	}

	@CrossOrigin("*")
	@PostMapping("nonsecure/resetpassword")
	public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPassword resetPassword) throws Exception {
		return new ResponseEntity<String>(userService.resetPassword(resetPassword), HttpStatus.OK);
	}

	@CrossOrigin("*")
	@PostMapping(path = "${i2i.secure.context_path}/changepassword")
	public ResponseEntity<?> changePassword(@RequestBody ChangePassword changePassword) throws Exception {
		userService.changePassword(changePassword.getChangedPassword(), changePassword.getEmail(),
				changePassword.getCurrentPassword());
		return new ResponseEntity<>("Password has been changed successfully.", HttpStatus.OK);
	}

	@CrossOrigin("*")
	@GetMapping(path = "${i2i.secure.context_path}/display/sentInvitesList/{emailId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<SentInvitesList>> sentInvitesList(@PathVariable String emailId) {
		List<SentInvitesList> users = userService.sentInvitesList(emailId);
		logger.info("Retrieving the list of Connections for sent invitations");
		/*
		 * if (users.isEmpty()) { return new
		 * ResponseEntity<List<SentInvitesList>>(HttpStatus.NO_CONTENT); }
		 */
		return new ResponseEntity<List<SentInvitesList>>(users, HttpStatus.OK);

	}

	@CrossOrigin("*")
	@GetMapping(path = "${i2i.secure.context_path}/display/receivedInvitesList/{emailId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<SentInvitesList>> receivedInvitesList(@PathVariable String emailId) {
		List<SentInvitesList> users = userService.receivedInvitesList(emailId);
		logger.info("Retrieving the list of Connections for received invitations");
		return new ResponseEntity<List<SentInvitesList>>(users, HttpStatus.OK);
	}

	@CrossOrigin("*")
	@PostMapping(path = "${i2i.secure.context_path}/decline/{emailId}/{declinedEmail}")
	public ResponseEntity<?> decline(@PathVariable String emailId, @PathVariable String declinedEmail)
			throws Exception {
		return new ResponseEntity<String>(userService.declineRequest(emailId, declinedEmail), HttpStatus.OK);
	}

	@CrossOrigin("*")
	@PostMapping(path = "${i2i.secure.context_path}/ignore/{emailId}/{ignoredEmail}")
	public ResponseEntity<?> ignore(@PathVariable String emailId, @PathVariable String ignoredEmail) throws Exception {
		return new ResponseEntity<String>(userService.ignoreRequest(emailId, ignoredEmail), HttpStatus.OK);
	}

	@CrossOrigin("*")
	@PostMapping(path = "${i2i.secure.context_path}/confirm/{emailId}/{confirmedEmail}")
	public ResponseEntity<?> confirm(@PathVariable String emailId, @PathVariable String confirmedEmail)
			throws Exception {
		return new ResponseEntity<String>(userService.confirmRequest(emailId, confirmedEmail), HttpStatus.OK);
	}

	@CrossOrigin("*")
	@GetMapping(path = "${i2i.secure.context_path}/display/getConnections/{emailid}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<UserConnection>> getAllConnections(@PathVariable String emailid) {
		List<UserConnection> users = userService.getConnections(emailid);
		logger.info("Retriving the  list of Connections");
		if (users.isEmpty()) {
			return new ResponseEntity<List<UserConnection>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<UserConnection>>(users, HttpStatus.OK);
	}

	/*
	 * @CrossOrigin("*")
	 * 
	 * @GetMapping(value = "${i2i.secure.context_path}/ua/getUser/{id}", produces =
	 * MediaType.APPLICATION_JSON_UTF8_VALUE) public ResponseEntity<User>
	 * getUser(@PathVariable Long id) { User user = userService.getById(id);
	 * logger.info("Retrived the user on basis of ID"); if (null == user) { return
	 * new ResponseEntity<User>(HttpStatus.NOT_FOUND);
	 * 
	 * } return new ResponseEntity<User>(user, HttpStatus.OK); }
	 */

	@CrossOrigin("*")
	@GetMapping(path = "${i2i.secure.context_path}/ua/searchConnection/{loginuseremail}/{searchstring}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<SearchDto>> SearchUser(@PathVariable String loginuseremail,
			@PathVariable String searchstring) {
		logger.info("Retrived the name on basis of Search");
		return new ResponseEntity<List<SearchDto>>(userService.getUserByFirstName(loginuseremail, searchstring),
				HttpStatus.OK);
	}

	@CrossOrigin("*")
	@GetMapping(value = "${i2i.secure.context_path}/recommend/getAllRecommendations/{emailid}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<UserConnection>> getAllRecommendations(@PathVariable String emailid) {
		return new ResponseEntity<List<UserConnection>>(userService.getAllRecommendations(emailid), HttpStatus.OK);
	}

	@CrossOrigin("*")
	@PostMapping(path = "${i2i.secure.context_path}/ua/connectUser/{loginuseremailid}/{connecteduseremailid}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<User> connectUser(@PathVariable String loginuseremailid,
			@PathVariable String connecteduseremailid) throws Exception {
		return new ResponseEntity<User>(userService.connectUser(loginuseremailid, connecteduseremailid), HttpStatus.OK);
	}

	@CrossOrigin("*")
	@PostMapping(path = "${i2i.secure.context_path}/ua/sendMessage/")
	public ResponseEntity<String> sendMessage(@ModelAttribute EmailMessageDto messageDto) throws Exception {
		return new ResponseEntity<String>(userService.sendMessage(messageDto), HttpStatus.OK);
	}

	@CrossOrigin("*")
	@PutMapping(path = "${i2i.secure.context_path}/ua/deleteConnection/{loginuseremailId}/{deletedemailId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<User> deleteConnection(@PathVariable String loginuseremailId,
			@PathVariable String deletedemailId) throws Exception {

		return new ResponseEntity<User>(userService.deleteConnection(loginuseremailId, deletedemailId), HttpStatus.OK);
	}

	@CrossOrigin("*")
	@PutMapping(value = "${i2i.non_secure.context_path}/ua/updateqbid", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> updateQuickBoxUser(@RequestParam String userEmail, @RequestParam String qbid)
			throws Exception {
		return new ResponseEntity<String>(userService.updateQuickBoxId(userEmail, qbid), HttpStatus.OK);
	}

	@CrossOrigin("*")
	@GetMapping(path = "${i2i.secure.context_path}/ua/connectedUserList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<UserConnection>> connectedUserList(@RequestParam String email) throws Exception {
		return new ResponseEntity<List<UserConnection>>(userService.connectedUserList(email), HttpStatus.OK);
	}

	@CrossOrigin("*")
	@PostMapping(path = "${i2i.secure.context_path}/createGroup")
	public ResponseEntity<?> createGroup(@ModelAttribute GroupDto groupDto) throws Exception {
		return new ResponseEntity<String>(userService.createGroup(groupDto), HttpStatus.OK);
	}

	/*
	 * @CrossOrigin("*")
	 * 
	 * @PostMapping(path = "${i2i.secure.context_path}/createGroup") public
	 * ResponseEntity<?> getGroup(@RequestParam Long groupId) throws Exception {
	 * return new
	 * ResponseEntity<String>(userService.createGroup(createGroup.getGroupName(),
	 * createGroup.getDescription(),createGroup.getPhoto_url(),createGroup.
	 * getCreated_by(), createGroup.getUser_ids()), HttpStatus.OK); }
	 */

	@CrossOrigin("*")
	@PostMapping(path = "${i2i.non_secure.context_path}/grouplist")
	public ResponseEntity<List<Group>> groupList(@RequestParam Long createdBy) throws Exception {
		return new ResponseEntity<List<Group>>(userService.getGrouplist(createdBy), HttpStatus.OK);
	}
	@CrossOrigin("*")
	@PostMapping(path = "${i2i.non_secure.context_path}/deletegroup")
	public ResponseEntity<?> deleteGroup(@RequestParam Long groupId) throws Exception {
		return new ResponseEntity<String>(userService.deleteGroup(groupId), HttpStatus.OK);
	}

	@CrossOrigin("*")
	@PostMapping(path = "${i2i.secure.context_path}/sendMessageInGroup")
	public ResponseEntity<String> sendMessageInGroup(@ModelAttribute GroupMessageDto groupMessage) throws Exception {
		return new ResponseEntity<String>(userService.sendMessageInGroup(groupMessage), HttpStatus.OK);
	}
	
	@CrossOrigin("*")
	@PostMapping(path = "${i2i.secure.context_path}/editGroupMember")
	public ResponseEntity<?> editGroupMember(@ModelAttribute GroupDto groupDto) throws Exception {
		return new ResponseEntity<String>(userService.editGroupMember(groupDto), HttpStatus.OK);
	}
	
	@CrossOrigin("*")
	@PostMapping(path = "${i2i.secure.context_path}/deleteMember")
	public ResponseEntity<?> deleteGroupMember(@ModelAttribute GroupDto groupDto) throws Exception {
		return new ResponseEntity<String>(userService.removeGroupMember(groupDto), HttpStatus.OK);
	}
	
	@CrossOrigin("*")
	@PostMapping(path = "${i2i.secure.context_path}/post")
	public ResponseEntity<String> post(@ModelAttribute PostUrlDto post) throws Exception {
		return new ResponseEntity<String>(userService.post(post), HttpStatus.OK);
	}
	
	@CrossOrigin("*")
	@PostMapping(path = "${i2i.secure.context_path}/deletePost")
	public ResponseEntity<?> deletePost(@RequestParam Long postId) throws Exception {
		return new ResponseEntity<String>(userService.deletePost(postId), HttpStatus.OK);
	}
	
	@CrossOrigin("*")
	@PostMapping(path = "${i2i.secure.context_path}/postlist")
	public ResponseEntity<List<PostDto>> postList(@RequestParam Long createdBy) throws Exception {
	return new ResponseEntity<List<PostDto>>(pstService.getPost(createdBy), HttpStatus.OK);
	}
	
	@CrossOrigin("*")
	@GetMapping(path = "${i2i.secure.context_path}/viewPost")
	public ResponseEntity<Post> viewPost(@RequestParam Long postId) throws Exception {
	return new ResponseEntity<Post>(pstService.viewPost(postId), HttpStatus.OK);
	}
	
	@CrossOrigin("*")
	@PostMapping(path = "${i2i.secure.context_path}/postComment")
	public ResponseEntity<String> postComment(@RequestBody CommentDto pstdto) throws Exception {
		return new ResponseEntity<String>(pstService.postComment(pstdto), HttpStatus.OK);
	}
	
	@CrossOrigin("*")
	@PostMapping(path = "${i2i.secure.context_path}/deleteComment")
	public ResponseEntity<?> deleteComment(@RequestBody Long commentId) throws Exception {
		return new ResponseEntity<String>(pstService.deleteComment(commentId), HttpStatus.OK);
	}
}
