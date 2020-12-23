package com.cts.cj.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cts.cj.constants.CJCommonConstants;
import com.cts.cj.constants.CJMessageConstant;
import com.cts.cj.constants.EmailEventType;
import com.cts.cj.constants.EmailSendStatus;
import com.cts.cj.constants.Status;
import com.cts.cj.constants.UserSource;
import com.cts.cj.domain.Comment;
import com.cts.cj.domain.EmailMessage;
import com.cts.cj.domain.Group;
import com.cts.cj.domain.Mail;
import com.cts.cj.domain.Occupation;
import com.cts.cj.domain.PasswordToken;
import com.cts.cj.domain.Post;
import com.cts.cj.domain.PostUrl;
import com.cts.cj.domain.Prospect;
import com.cts.cj.domain.ResetPassword;
import com.cts.cj.domain.Role;
import com.cts.cj.domain.SentInvitesList;
import com.cts.cj.domain.User;
import com.cts.cj.domain.UserConnection;
import com.cts.cj.dto.EmailMessageDto;
import com.cts.cj.dto.GroupDto;
import com.cts.cj.dto.GroupMessageDto;
import com.cts.cj.dto.PostUrlDto;
import com.cts.cj.dto.SearchDto;
import com.cts.cj.dto.UserDto;
import com.cts.cj.exception.UserAlreadyExistsException;
import com.cts.cj.repository.GroupRepository;
import com.cts.cj.repository.MessageRepository;
import com.cts.cj.repository.PasswordTokenRepository;
import com.cts.cj.repository.PostRepository;
import com.cts.cj.repository.PrivilegeRepository;
import com.cts.cj.repository.ProspectRepository;
import com.cts.cj.repository.RegistorRepository;
import com.cts.cj.repository.RoleRepository;
import com.cts.cj.repository.UserProfileRepository;
import com.cts.cj.repository.UserRepository;
import com.cts.cj.util.CJUtil;
import com.cts.cj.util.CJUtilCommon;
import com.cts.cj.util.EmailUtil;

@Service(value = "userService")
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {

	public static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PrivilegeRepository privilegeRepository;

	@Autowired
	private CJUtilCommon i2iCommonUtil;

	@Autowired
	private EmailUtil emailUtil;

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private RegistorRepository registorRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private ProspectRepository prospectRepository;

	@Autowired
	private MessageService messageService;

	@Autowired
	private UserProfileRepository userProfileRepository;

	@Autowired
	private PasswordTokenRepository tokenRepository;

	@Value("${domainurl}")
	private String domainUrl;

	@Value("${file.upload-dir}")
	private String fileStorageLocation;

	@Autowired
	private GroupRepository groupRepo;

	@Autowired
	private PostRepository postUrlRepo;
	
	@Autowired
	private PostService postService;
	
	@Autowired
	private ConnectionService connService;

	/*
	 * @Autowired User user;
	 * 
	 * @Autowired UserConnection userconnection;
	 * 
	 * /*
	 * 
	 * @Autowired private PasswordEncoder encoder;
	 */
	/*
	 * @Autowired private InvitationRepository invitationRepo;
	 */

	/*
	 * @Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;
	 */
	@Override
	public List<User> getAll() {
		List<User> userList = userRepository.getUserList();

		return userList;
	}

	@Override
	public void deleteAll(String emailid, String mobileNumber, String name) {
		userRepository.DeleteUser(emailid, mobileNumber, name);
	}

	/*
	 * private boolean userExists(String emailId) { return null !=
	 * userRepository.findByEmail(emailId); }
	 */
	/**
	 * User user
	 * 
	 * @throws UserAlreadyExistsException
	 * @throws MessagingException
	 */
	@Override
	public String register(UserDto user) throws UserAlreadyExistsException, MessagingException {

		boolean activate = true;
		boolean validInvitationCode = false;
		Long downline = user.getReferedBy() != null ? user.getReferedBy() : 0L;
		Optional<User> dbuser = userRepository.findByEmailandInviteCode(user.getEmailId(), user.getInvitationCode());
		if (dbuser.isPresent()) {
			if (dbuser.get().getInviteCode().equalsIgnoreCase(user.getInvitationCode())
					&& dbuser.get().getEmailId().equalsIgnoreCase(user.getEmailId())) {
				validInvitationCode = true;
				
				downline = dbuser.get().getReferedBy();
				if (dbuser.get().getStatus() == Status.ACTIVE) {
					String message = "User is already Registered with an email id %s.Please sign-in .";
					return "{\"error\": \" " + String.format(message, new String[] { user.getEmailId() }) + "\"}";
				}

			}
		} else {
			validInvitationCode = false;
		}

		if (!validInvitationCode) {
			String message = "Invalid invitation code/email %s .";
			return "{\"error\": \" " + String.format(message, new String[] { user.getEmailId() }) + "\"}";

		}

		int count = 6;
		String randomPassword = user.getPassword();// CJUtil.randomAlphaNumeric(count);

		List<Role> role = new ArrayList<>();
		List<EmailMessage> emailmsg = new ArrayList<>();
		Role roletype = new Role();
		roletype.setName(CJCommonConstants.USER_ROLE);
		role.add(roletype);
		// dbuser.get().setRole(role);
		dbuser.get().setFirstName(user.getFirstName());
		dbuser.get().setLastName(user.getLastName());
		dbuser.get().setMobileNumber(user.getMobileNumber());

		user.setPassword(randomPassword);
		if (null == user.getInviteBy()) {
			dbuser.get().setSrc(UserSource.NORMAL_SIGNUP.name());
		} else {
			UserSource src = CJUtilCommon.setUserSource(user.getInviteBy());
			dbuser.get().setSrc(src.name());
		}
		logger.debug("User status prior to registration is [{}], " + user.getStatus().name());
		if (activate || dbuser.get().getSrc().equalsIgnoreCase(UserSource.INVITED_BY_ADMIN.name())) {
			dbuser.get().setStatus(Status.ACTIVE);
		}
		// dbuser.get().setForceToChangePassword(true);
		if (StringUtils.isNoneEmpty(dbuser.get().getSrc())
				&& dbuser.get().getSrc().equalsIgnoreCase(UserSource.NORMAL_SIGNUP.name())) {
			dbuser.get().setSource(UserSource.NORMAL_SIGNUP);
		} else {
			UserSource src = CJUtilCommon.deCodeUserSource(dbuser.get().getSrc());
			dbuser.get().setSource(src);
		}
		// user.setPassword(randomPassword);
		if (activate) {
			EmailMessage msg = new EmailMessage();
			msg.setRecieverEmail(user.getEmailId());
			String approvalmsg = CJMessageConstant.SIGN_UP_MESSAGE_WITHOUT_APPROVAL;
			String messge = CJUtil.signupMessege(randomPassword, dbuser.get(), approvalmsg);
			try {
				Mail mail = new Mail();
				mail.setFrom(CJMessageConstant.CJ_EMAIL);
				mail.setTo(user.getEmailId());
				mail.setSubject(CJMessageConstant.SIGN_UP_SUBJECT);

				Map<String, Object> model = new HashMap<String, Object>();
				// model.put("token", token);
				model.put("userName",user.getFirstName());
				// model.put("signature", "https://giveCharity.com");
				// model.put("resetUrl", forgetPassword.getReturnUrl() + "?token=" +
				// token.getToken()+"&reqType=token");
				model.put("domainUrl", domainUrl);
				mail.setModel(model);
				emailUtil.sendEmail(mail,"Credential.html", null, null, false );
				//return "{\"emailSent\": \" " + flag + "\"}";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw e;
			}
		} else {
			dbuser.get().setStatus(Status.INACTIVE);
		}
		// encodePassword
		dbuser.get().setPassword(CJUtil.encodePassword(randomPassword));
		dbuser.get().setRegistrationDate(LocalDateTime.now());
		User usersaved = userRepository.save(dbuser.get());
		userRepository.createHasRoleRelationship(usersaved.getEmailId());

		/*
		 * if (usersaved != null) { UserProfile profile = new UserProfile(); User
		 * existedUser =userRepository.getUserbyId(usersaved.getId());
		 * if(existedUser!=null) { existedUser.setId(existedUser.getId());
		 * profile.setUserprofile(existedUser); profile.setUserId(existedUser.getId());
		 * userProfileRepository.save(profile); } }
		 */

		if (downline != null) {
			try {
				userRepository.createRelationship(downline, usersaved.getId());
				userRepository.connectUser(downline, usersaved.getId());

			} catch (Exception e) {
				logger.info("Relation is not developed with the referece" + e.getMessage());
			}
		}

		logger.info("User informaiton saved successfully ->" + usersaved);
		usersaved.setPassword(null);
		return "{\"registered\": \" true\"}";
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(username);
//		List<GrantedAuthority> authorities = getUserAuthorities(user.getRole());
		Collection<? extends GrantedAuthority> authorities = getUserAuthorities(user.getRole());
//		return buildUserForAuthentication(user, authorities);
//		new org.springframework.security.core.userdetails.User
		return new org.springframework.security.core.userdetails.User(user.getEmailId(), user.getPassword(),
				authorities);
	}

//	private List<GrantedAuthority> getUserAuthorities(List<Role> userRoles) {
//		Set<GrantedAuthority> roles = new HashSet<GrantedAuthority>();
//		for (Role role : userRoles) {
//			roles.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
//		}
//
//		List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>(roles);
//		return grantedAuthorities;
//	}

	private Collection<? extends GrantedAuthority> getUserAuthorities(List<Role> userRoles) {
		List<SimpleGrantedAuthority> roles = new ArrayList<>();
		for (Role role : userRoles) {
			roles.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
		}

//		List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>(roles);
		return roles;
	}

	private UserDetails buildUserForAuthentication(User user, List<GrantedAuthority> authorities) {

		return new org.springframework.security.core.userdetails.User(user.getEmailId(), user.getPassword(), true, true,
				true, true, authorities);
	}

	@Override
	public User getById(Long id) {
		return userRepository.getUserbyId(id);
	}

	@Override
	public User update(User user) {
		User existingUser = userService.getById(user.getId());
		existingUser.setFirstName(user.getFirstName());
		existingUser.setLastName(user.getLastName());
		existingUser.setCountry(user.getCountry());
		existingUser.setMobileNumber(user.getMobileNumber());
		existingUser.setEmailId(user.getEmailId());
		userRepository.save(existingUser);
		return existingUser;
	}

	@Override
	public User changePassword(User user) throws MessagingException {
		List<EmailMessage> emailmsg = new ArrayList<>();
		User alreadyexist = userRepository.getUserbyId(user.getId());
		alreadyexist.setPassword(CJUtil.encodePassword(user.getPassword()));

		// message delivery saved to send later
		EmailMessage msg = new EmailMessage();
		String approvalmsg = "";
		String approvalsbj = "";
		// boolean passwordstatus = alreadyexist.isForceToChangePassword();
		/*
		 * if (passwordstatus) { approvalmsg = CJMessageConstant.RESET_PASSWORD;
		 * approvalsbj = CJMessageConstant.RESET_PASSWORD_SUBJECT; } else {
		 */
		approvalmsg = CJMessageConstant.UPDATE_PASSWORD;
		approvalsbj = CJMessageConstant.UPDATE_PASSWORD_SUBJECT;
		/* } */

		msg.setEmailsendStatus(EmailSendStatus.PENDING);
		// alreadyexist.setForceToChangePassword(false);
		String emailms = CJUtil.changePwd(alreadyexist, approvalmsg);
		// msg.setMessage(emailms);
		// msg.setSubject(approvalsbj);
		// msg.setRecieverEmail(alreadyexist.getEmailId());
		// msg.setEmaileventType(EmailEventType.getByName("changepass"));
		// emailmsg.add(msg);
		// alreadyexist.setEmailMessege(emailmsg);

		try {
			i2iCommonUtil.sendMail(alreadyexist.getEmailId(), emailms, approvalsbj);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			throw e;
		}
		userRepository.save(alreadyexist);
		alreadyexist.setPassword(null);
		return alreadyexist;

	}

	@Override
	public User forgetPwd(User user) {
		List<EmailMessage> emailmsg = new ArrayList<>();
		int count = 6;
		String randomPassword = CJUtil.randomAlphaNumeric(count);
		User alreadyexist = userRepository.findByEmail(user.getEmailId());
		alreadyexist.setPassword(randomPassword);
		// alreadyexist.setForceToChangePassword(true);
		EmailMessage msg = new EmailMessage();
		msg.setRecieverEmail(alreadyexist.getEmailId());
		String forgetmessag = CJMessageConstant.FORGET_PASSWORD;
		String domain = CJCommonConstants.I2I_DOMAIN_URL;
		msg.setEmailsendStatus(EmailSendStatus.PENDING);
		String forgetsubject = CJMessageConstant.FORGET_PASSWORD_SUBJECT;
		String forgetmessage = CJUtil.forgetUpdateMessege(randomPassword, forgetmessag, domain);
		msg.setSubject(forgetsubject);
		msg.setMessage(forgetmessage);
		msg.setRecieverEmail(alreadyexist.getEmailId());
		msg.setEmaileventType(EmailEventType.getByName("forget"));
		emailmsg.add(msg);
		// alreadyexist.setEmailMessege(emailmsg);
		userRepository.save(alreadyexist);

		return alreadyexist;
	}

	@Override
	public String UpdateStatus(User user) {
		int count = 6;
		List<EmailMessage> emailmsg = new ArrayList<>();
		String randomPassword = CJUtil.randomAlphaNumeric(count);
		User alreadyexist = userRepository.findByEmail(user.getEmailId());
		alreadyexist.setPassword(randomPassword);
		userRepository.save(alreadyexist);
		// Message to send later
		EmailMessage msg = new EmailMessage();
		msg.setRecieverEmail(alreadyexist.getEmailId());
		String approvalmsg = CJMessageConstant.USER_APPROVED;
		String domain = CJCommonConstants.I2I_DOMAIN_URL;
		String forgetsubject = CJMessageConstant.USER_APPROVED_SUBJECT;
		msg.setEmailsendStatus(EmailSendStatus.PENDING);
		String messge = CJUtil.forgetUpdateMessege(randomPassword, approvalmsg, domain);
		msg.setSubject(forgetsubject);
		msg.setMessage(messge);
		msg.setRecieverEmail(alreadyexist.getEmailId());
		msg.setEmaileventType(EmailEventType.getByName("updateuser"));
		emailmsg.add(msg);
		// alreadyexist.setEmailMessege(emailmsg);
		userRepository.save(alreadyexist);
		// End of message

		return "{\"emailSent\": \" true\"}";

	}

	@Override
	@Transactional
	public String sendInvite(String emailIds, String roletype, String userId) throws Exception {
		Long userid = Long.parseLong(userId);
		List<EmailMessage> emailmsg = new ArrayList<>();

		// User dbuser=userService.findByEmail(emailIds);
		User existingUser = userService.getById(userid);
		String[] sendToIds = emailIds.split(",");
		boolean flag = true;
		if (sendToIds != null && sendToIds.length >= 0) {
			for (int i = 0; i < sendToIds.length; i++) {
				UserDto dbuser = userService.findByInviteEmail(sendToIds[i]);
				if (dbuser.getEmailId() != null) {
					String message = "The email %s has already been invited.";
					return "{\"error\": \" " + String.format(message, new String[] { sendToIds[i] }) + "\"}";
					// throw new UserAlreadyExistsException();
				}
				// Invitaition invitaion= new Invitaition();
				String invitationCode = String.valueOf(CJUtil.generateInvitationCode(6));
				String approvalmsg = CJMessageConstant.INVITED_BY_ADMIN_EMAIL_MESSAGE;
				approvalmsg = approvalmsg + "<BR><B>Email:</B>" + sendToIds[i] + "</BR>";
				approvalmsg = approvalmsg + "\n <BR><B>Invitation Code:</B>" + invitationCode + "<BR>";
				approvalmsg = approvalmsg + " \n or You can join by Click To Register link. &nbsp;&nbsp;&nbsp;";
				String register = domainUrl + "/register" + "?referedBy=" + userid + "&referedemail=" + sendToIds[i]
						+ "&inviateCode=" + invitationCode + "\n";
				/*
				 * String register = domainUrl + "/register" + "?referedBy=" + userid +
				 * "&referedemail=" + sendToIds[i] + "\n";
				 */
				String signature = CJCommonConstants.emailSignature;
				String invitemessge = approvalmsg + "\n</BR><a href=" + register.trim() + "&inviateCode="
						+ invitationCode + "><B>Click To Register</B></a></BR>" + signature;
				String approvalsbj = CJMessageConstant.SUBJECT;

				try {
					User user = new User();
					user.setEmailId(sendToIds[i]);
					user.setInviteCode(invitationCode);
					user.setReferedBy(userid);
					Mail mail = new Mail();
					mail.setFrom(CJMessageConstant.CJ_EMAIL);
					mail.setTo(emailIds);
					mail.setSubject(CJMessageConstant.SUBJECT);

					// user.setEmailId(sendToIds[i]);
					// user.setInviteCode(invitationCode);
					/*
					 * Role role= new Role(); role.setName("USER");
					 * user.setRole(Arrays.asList(role));
					 */
					Map<String, Object> model = new HashMap<String, Object>();
					// model.put("token", token);
					model.put("emailId", user.getEmailId());
					// model.put("signature", "https://giveCharity.com");
					// model.put("resetUrl", forgetPassword.getReturnUrl() + "?token=" +
					// token.getToken()+"&reqType=token");
					model.put("invitationCode", user.getInviteCode());
					model.put("domainUrl", register);
				
					mail.setModel(model);

					user.setStatus(Status.INACTIVE);
					user.setCreatedDate(LocalDateTime.now());
					user.setReferedBy(userid);
					userRepository.save(user);
					emailUtil.sendEmail(mail, "invite_request.html", null, null, flag);
				} catch (Exception ex) {
					logger.error("Error occured in while sending invite:::>>>" + ex);
					throw ex;
				}
			}
		}

		return "{\"emailSent\": \" " + flag + "\"}";
	}

	@Override
	public UserDto findByEmail(String emailid) {
		User dbUser = userRepository.findByEmail(emailid);
		UserDto usr = new UserDto();
		if (dbUser != null) {
			dbUser.setPassword(null);
			BeanUtils.copyProperties(dbUser, usr);
		}
		return usr;

	}

	@Override
	public User updateUserStatus(String email, Status status) {

		String userStatus = status.name();
		User user = userRepository.updateUserStatus(email, userStatus);

		/**
		 * Code is commented for the admin approval and messege would release in later
		 * phase int count = 6; List<EmailMessage> emailmsg = new ArrayList<>(); String
		 * randomPassword = I2iUtil.randomAlphaNumeric(count); EmailMessage msg = new
		 * EmailMessage(); String approvalmsg =
		 * I2IMessageConstant.SIGN_UP_MESSAGE_WITHOUT_APPROVAL; String messge =
		 * I2iUtil.signupMessege(randomPassword, user, approvalmsg);
		 * msg.setMessage(messge); msg.setSubject(I2IMessageConstant.SIGN_UP_SUBJECT);
		 * msg.setRecieverEmail(user.getEmailId());
		 * msg.setEmaileventType(EmailEventType.getByName("signup"));
		 * msg.setEmailsendStatus(EmailSendStatus.PENDING); emailmsg.add(msg);
		 * user.setEmailMessege(emailmsg);
		 */
		userRepository.save(user);
		return user;
	}

	@Override
	public Prospect registerProspect(Prospect prospect) throws UserAlreadyExistsException {
		Prospect prspect = new Prospect();
		prspect.setEmailId(prospect.getEmailId());
		return prospectRepository.save(prspect);
	}

	@Override
	public UserDto findByInviteEmail(String emailid) {
		User dbUser = userRepository.findByInviteEmail(emailid);
		UserDto usr = new UserDto();
		if (dbUser != null) {
			dbUser.setPassword(null);
			BeanUtils.copyProperties(dbUser, usr);
		}
		return usr;

	}

	@Override
	public String forgotpassword(String emailId) throws Exception {
		UserDto dbuser = userService.findByEmail(emailId);
		boolean flag = true;
		if (dbuser == null) {
			String message = "The User does not exist.";
			return "{\"error\": \" " + String.format(message, new String[] { dbuser.getEmailId() }) + "\"}";
		}

		PasswordToken token = new PasswordToken();
		token.setToken(UUID.randomUUID().toString());
		token.setEmail(dbuser.getEmailId());
		token.setExpiryDate(30);
		tokenRepository.save(token);

		String approvalmsg = null;
		approvalmsg = "<BR><B>Hi  </B>" + dbuser.getFirstName() + "<BR>";
		approvalmsg = approvalmsg + "You can reset your password by clicking to the link provided below. <BR>";
		String url = "https://14.99.182.162/itoi/reset" + "?token=" + token.getToken() + "&reqType=token";
		String signature = CJCommonConstants.emailSignature;
		String invitemessge = approvalmsg + "</BR>" + "<a href=" + url + "> <B>Reset Password</B> </a><BR>"
		// + "Reset Key :" + token.getToken() + "<BR>"
				+ signature;
		String approvalsbj = CJMessageConstant.PASSWORDSUBJECT;

		try {

			Mail mail = new Mail();
			mail.setFrom(CJMessageConstant.CJ_EMAIL);
			mail.setTo(dbuser.getEmailId());
			mail.setSubject(CJMessageConstant.PASSWORDSUBJECT);
			Map<String, Object> model = new HashMap<String, Object>();
			// model.put("token", token);
			model.put("userName", dbuser.getFirstName());
			model.put("url", url);
			mail.setModel(model);
			emailUtil.sendEmail(mail, "password_change.html", null, null, flag);
		} catch (Exception ex) {
			logger.error("Error occured in while sending Reset Email:::>>>" + ex);
			throw ex;
		}
		return "{\"emailSent\": \" " + flag + "\"}";
	}

	@Override
	public String resetPassword(ResetPassword resetPassword) throws Exception {
		PasswordToken resetToken = tokenRepository.findByToken(resetPassword.getToken());
		String resultmessage = "Your password has been successfully changed";
		if (resetToken == null) {
			String message = "Invalid token";
			return "{\"error\": \" " + String.format(message, new String[] { resetPassword.getToken() }) + "\"}";
		} else if (resetToken.isExpired()) {
			String message = "Token has expired,please use valid token to reset password.";
			return "{\"error\": \" " + String.format(message, new String[] { resetPassword.getToken() }) + "\"}";
		}
		String email = resetToken.getEmail();
		User user = userRepository.findByEmail(email);
		String updatedPassword = CJUtil.encodePassword(resetPassword.getPassword());
		user.setPassword(updatedPassword);
		Mail mail = new Mail();
		mail.setFrom(CJMessageConstant.CJ_EMAIL);
		mail.setTo(user.getEmailId());
		mail.setSubject(CJMessageConstant.PASSWORD_CHANGE);

		Map<String, Object> model = new HashMap<String, Object>();
		// model.put("token", token);
		model.put("userName", user.getFirstName());
		model.put("domainUrl", domainUrl);
		mail.setModel(model);
		userRepository.save(user);
		emailUtil.sendEmail(mail, "password_change_confirmation.html", null, null, false);
		tokenRepository.delete(resetToken);
		return "{\"success\": \" " + resultmessage + "\"}";
	}

	@Override
	public void changePassword(String changedPassword, String userEmail, String currentPassword) throws Exception {
		if (userEmail == null || !CJUtil.isValid(userEmail)) {
			throw new Exception("Invalid user email.");
		}
		if (null == changedPassword || StringUtils.isEmpty(changedPassword)) {
			throw new Exception("User password can not empty/null.");
		}
		User user = userRepository.findByEmail(userEmail.trim());
		if (user == null) {
			throw new Exception("User not found.");
		} else {
			if (CJUtil.matchPassword(currentPassword, user.getPassword())) {
				user.setPassword(CJUtil.encodePassword(changedPassword));
				user.setIsResetPassword(Boolean.TRUE);
				user.setModifiedDate(LocalDateTime.now());
				Mail mail = new Mail();
				mail.setFrom(CJMessageConstant.CJ_EMAIL);
				mail.setTo(user.getEmailId());
				mail.setSubject(CJMessageConstant.PASSWORD_CHANGE);

				Map<String, Object> model = new HashMap<String, Object>();
				// model.put("token", token);
				model.put("userName", user.getFirstName());
				model.put("domainUrl", domainUrl);
				mail.setModel(model);
				userRepository.save(user);
				emailUtil.sendEmail(mail, "password_change_confirmation.html", null, null, false);
			} else {
				throw new Exception("Invalid current password.");
			}
		}
	}

	public List<SentInvitesList> sentInvitesList(String emailId) {
		List<SentInvitesList> inviteList = new ArrayList<SentInvitesList>();
		List<User> userList = userRepository.sentInvitesList(emailId);
		// if (userList != null) {
		for (User u : userList) {
			SentInvitesList list = new SentInvitesList();
			list.setUserId(u.getId());
			list.setEmail(u.getEmailId());
			list.setFirstName(u.getFirstName());
			list.setLastName(u.getLastName());
			list.setMobileNumber(u.getMobileNumber());
			list.setInviteBy(u.getInviteBy());
			list.setInviteCode(u.getInviteCode());
			list.setReferedBy(u.getReferedBy());
			list.setSource(u.getSource());
			// list.setConnectionDate(u.getConnectionDate());
			list.setProfileImageUrl(u.getProfileImageUrl());
			list.setCreatedDate(u.getCreatedDate());
			list.setRegistrationDate(u.getRegistrationDate());
			list.setStatus(u.getStatus());
			if (u.getOccupation() != null) {
				List<Occupation> olist = u.getOccupation().stream().filter(f -> f.getEndDate() == null)
						.collect(Collectors.toList());
				if (!olist.isEmpty()) {
					list.setDesignation(olist.get(0).getDesignation());
					list.setStartdate(olist.get(0).getStartdate());
					list.setEmployerName(olist.get(0).getEmployerName());
				} else {
					list.setDesignation(u.getOccupation().get(0).getDesignation());
					list.setStartdate(u.getOccupation().get(0).getStartdate());
					list.setEmployerName(u.getOccupation().get(0).getEmployerName());
				}
			}
			inviteList.add(list);
		}
		// }
		return inviteList;
	}

	public List<SentInvitesList> receivedInvitesList(String emailId) {
		List<SentInvitesList> inviteList = new ArrayList<SentInvitesList>();
		List<User> userList = userRepository.receivedInvitesList(emailId);
		// if (userList != null) {
		for (User u : userList) {
			SentInvitesList list = new SentInvitesList();
			list.setUserId(u.getId());
			list.setEmail(u.getEmailId());
			list.setFirstName(u.getFirstName());
			list.setLastName(u.getLastName());
			list.setMobileNumber(u.getMobileNumber());
			list.setInviteBy(u.getInviteBy());
			list.setInviteCode(u.getInviteCode());
			list.setReferedBy(u.getReferedBy());
			list.setSource(u.getSource());
			// list.setConnectionDate(u.getConnectionDate());

			list.setProfileImageUrl(u.getProfileImageUrl());
			list.setCreatedDate(u.getCreatedDate());
			list.setRegistrationDate(u.getRegistrationDate());
			list.setStatus(u.getStatus());
			if (u.getOccupation() != null) {
				List<Occupation> olist = u.getOccupation().stream().filter(f -> f.getEndDate() == null)
						.collect(Collectors.toList());
				if (!olist.isEmpty()) {
					list.setDesignation(olist.get(0).getDesignation());
					list.setStartdate(olist.get(0).getStartdate());
					list.setEmployerName(olist.get(0).getEmployerName());
				} else {
					list.setDesignation(u.getOccupation().get(0).getDesignation());
					list.setStartdate(u.getOccupation().get(0).getStartdate());
					list.setEmployerName(u.getOccupation().get(0).getEmployerName());
				}
			}
			inviteList.add(list);
		}
		// }
		return inviteList;
	}

	@Override
	public String declineRequest(String emailId, String declinedEmail) throws Exception {
		userRepository.declineRequest(emailId, declinedEmail);
		UserDto currentUser = userService.findByEmail(emailId);
		UserDto connectedUser = userService.findByEmail(declinedEmail);
		boolean flag = true;
		if (connectedUser == null || currentUser == null) {
			String message = "The User does not exist.";
			return "{\"error\": \" "
					+ String.format(message, new String[] { connectedUser.getEmailId(), currentUser.getEmailId() })
					+ "\"}";
		}

		String declinedMessage = null;
		declinedMessage = "<BR><B>Hi  </B>" + currentUser.getFirstName() + "<BR>";
		declinedMessage = declinedMessage + " You have declined your connection request. <BR>";
		String declinedSubject = CJMessageConstant.DECLINEREQUESTSUBJECT;

		try {

			Mail mail = new Mail();
			mail.setFrom(CJMessageConstant.CJ_EMAIL);
			mail.setTo(currentUser.getEmailId());
			mail.setSubject(CJMessageConstant.DECLINEREQUESTSUBJECT);
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("connectedUser", connectedUser.getFirstName());
			model.put("currentUser", currentUser.getFirstName());
			mail.setModel(model);
			emailUtil.sendEmail(mail, "request_decline.html", null, null, flag);
		} catch (Exception ex) {
			logger.error("Error occured in while sending response mail:::>>>" + ex);
			throw ex;
		}

		return "{\"emailSent\": \" " + flag + "\"}";
	}

	@Override
	public String ignoreRequest(String emailId, String ignoredEmail) throws Exception {
		userRepository.ignoreRequest(emailId, ignoredEmail);
		UserDto currentUser = userService.findByEmail(emailId);
		UserDto connectedUser = userService.findByEmail(ignoredEmail);
		boolean flag = true;
		if (connectedUser == null || currentUser == null) {
			String message = "The User does not exist.";
			return "{\"error\": \" "
					+ String.format(message, new String[] { connectedUser.getEmailId(), currentUser.getEmailId() })
					+ "\"}";
		}

		String ignoredMessage = null;
		ignoredMessage = "<BR><B>Hi  </B>" + connectedUser.getFirstName() + "<BR>";
		ignoredMessage = ignoredMessage + currentUser.getFirstName() + " has ignored your connection request. <BR>";
		String ignoredSubject = CJMessageConstant.IGNOREREQUESTSUBJECT;

		try {
			Mail mail = new Mail();
			mail.setFrom(CJMessageConstant.CJ_EMAIL);
			mail.setTo(connectedUser.getEmailId());
			mail.setSubject(CJMessageConstant.IGNOREREQUESTSUBJECT);
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("connectedUser", connectedUser.getFirstName());
			model.put("currentUser", currentUser.getFirstName());
			mail.setModel(model);
			emailUtil.sendEmail(mail, "connection_request_ignored.html", null, null, flag);
			/*
			 * i2iCommonUtil.sendMail(new String[] { ignoredEmail }, ignoredMessage,
			 * ignoredSubject);
			 */
		} catch (Exception ex) {
			logger.error("Error occured in while sending response mail:::>>>" + ex);
			throw ex;
		}

		return "{\"emailSent\": \" " + flag + "\"}";
	}

	@Override
	public String confirmRequest(String emailId, String confirmedEmail) throws Exception {
		userRepository.confirmRequest(emailId, confirmedEmail);
		UserDto currentUser = userService.findByEmail(emailId);
		UserDto connectedUser = userService.findByEmail(confirmedEmail);
		boolean flag = true;
		if (connectedUser == null || currentUser == null) {
			String message = "The User does not exist.";
			return "{\"error\": \" "
					+ String.format(message, new String[] { connectedUser.getEmailId(), currentUser.getEmailId() })
					+ "\"}";
		}

		String confirmedMessage = null;
		confirmedMessage = "<BR><B>Hi  </B>" + connectedUser.getFirstName() + "<BR>";
		confirmedMessage = confirmedMessage + currentUser.getFirstName()
				+ " has confirmed your connection request. <BR>";
		String confirmedSubject = CJMessageConstant.CONFIRMREQUESTSUBJECT;

		try {

			Mail mail = new Mail();
			mail.setFrom(CJMessageConstant.CJ_EMAIL);
			mail.setTo(connectedUser.getEmailId());
			mail.setSubject(CJMessageConstant.CONFIRMREQUESTSUBJECT);
			Map<String, Object> model = new HashMap<String, Object>();
			// model.put("token", token);
			model.put("userName", connectedUser.getFirstName());
			model.put("domainUrl", domainUrl);
			mail.setModel(model);
			emailUtil.sendEmail(mail, "received_invite_confirmation.html", null, null, flag);
		} catch (Exception ex) {
			logger.error("Error occured in while sending response mail:::>>>" + ex);
			throw ex;
		}

		return "{\"emailSent\": \" " + flag + "\"}";
	}

	public List<UserConnection> getConnections(String emailid) {
		List<UserConnection> userConnectionList = new ArrayList<>();
		//List<User> userList = userRepository.getconnections(emailid);
		
		List<User> userList= connService.getConnections(emailid);
		
		//STR_CONNECTIONS_CQL

		for (User obj : userList) {
			UserConnection userconnection = new UserConnection();
			Occupation currentOccupation= null ;
			if (obj.getOccupation() != null && !obj.getOccupation().isEmpty()) {
				currentOccupation = obj.getOccupation().get(obj.getOccupation().size() - 1);
				if(currentOccupation==null)
					currentOccupation=new Occupation();
			}

			userconnection.setUserId(obj.getId());
			if (obj.getConnectionDate() != null) {
				userconnection.setConnectionDate(
						LocalDateTime.parse(obj.getConnectionDate(), DateTimeFormatter.ISO_ZONED_DATE_TIME));
			}
			// userconnection.setConnectionDate(connectiondate.getConnectionDate());
			userconnection.setFirstName(obj.getFirstName());
			userconnection.setLastName(obj.getLastName());
			userconnection.setEmail(obj.getEmailId());
			userconnection.setProfileImageUrl(obj.getProfileImageUrl());
			userconnection.setDesignation(currentOccupation.getDesignation());
			userconnection.setStartDate(currentOccupation.getStartdate());
			userconnection.setTillDate(currentOccupation.isTillDate());
			userconnection.setEndDate(currentOccupation.getEndDate());
			userconnection.setEmployerName(currentOccupation.getEmployerName());
			userconnection.setQbid(obj.getQbid());
			userConnectionList.add(userconnection);
		}

		return userConnectionList;
	}

	@Override
	public List<SearchDto> getUserByFirstName(String loginuseremail, String searchstring) {
		List<User> dbsearch = userRepository.getByFirstname(loginuseremail, "(?i)" + searchstring + ".*");
		List<SearchDto> search = new ArrayList<SearchDto>();
		dbsearch.forEach(ds -> {
			SearchDto searchdto = new SearchDto();
			searchdto.setUserId(ds.getId());
			searchdto.setFirstName(ds.getFirstName());
			searchdto.setLastName(ds.getLastName());
			searchdto.setProfileImageUrl(ds.getProfileImageUrl());
			searchdto.setEmail(ds.getEmailId());
			// searchdto.setQuickboxuserId(ds.getQuickboxuserId());
			search.add(searchdto);
		});
		return search;

	}

	@Override
	public List<UserConnection> getAllRecommendations(String emailid) {
		List<User> recomendList = userRepository.getRecommendationList(emailid);
		List<User> duplicatelist = userRepository.getduplicateRecmondationList(emailid);
		/*
		 * recomendList.forEach(rlist->{ duplicatelist.forEach(dlist->{
		 * if(dlist.getEmailId().equalsIgnoreCase(rlist.getEmailId())) {
		 * recomendList.remove(rlist); }
		 * 
		 * }); });
		 */
		
		recomendList.removeIf(h->h.getEmailId().equals(emailid));
		List<UserConnection> finalUserList = new ArrayList<UserConnection>();
		List<User> finallist = recomendList.stream().filter(x -> !duplicatelist.contains(x))
				.collect(Collectors.toList());
		finallist.forEach(flist -> {
			UserConnection uconnection = new UserConnection();
			uconnection.setFirstName(flist.getFirstName());
			uconnection.setLastName(flist.getLastName());
			uconnection.setEmail(flist.getEmailId());
			uconnection.setUserId(flist.getId());
			uconnection.setProfileImageUrl(flist.getProfileImageUrl());
			if (flist.getOccupation() != null) {
				List<Occupation> olist = flist.getOccupation().stream().filter(f -> f.getEndDate() == null)
						.collect(Collectors.toList());
				if (!olist.isEmpty()) {
					uconnection.setDesignation(olist.get(0).getDesignation());
					uconnection.setStartDate(olist.get(0).getStartdate());
					uconnection.setEmployerName(olist.get(0).getEmployerName());
				} else {
					uconnection.setDesignation(flist.getOccupation().get(0).getDesignation());
					uconnection.setStartDate(flist.getOccupation().get(0).getStartdate());
					uconnection.setEmployerName(flist.getOccupation().get(0).getEmployerName());
				}
			}
			finalUserList.add(uconnection);
		});
		return finalUserList;
	}

	@Override
	public User connectUser(String loginuseremailid, String connecteduseremailid) {
		User connectrequest = userRepository.getconnectUser(loginuseremailid, connecteduseremailid);

		// List<EmailMessage> emailmsg = new ArrayList<>();

		UserDto dbuser = userService.findByEmail(connecteduseremailid);
		UserDto dbuser1 = userService.findByEmail(loginuseremailid);
		boolean flag = true;
		String approvalmsg = " ";
		/*
		 * if(dbuser.getFirstName()!=null) { approvalmsg = + dbuser.getFirstName() + ";
		 * }
		 */
		approvalmsg = "Hey " + dbuser.getFirstName() + ", You got request to connect from " + dbuser1.getFirstName();
		String signature = CJCommonConstants.emailSignature;
		String invitemessge = approvalmsg + signature;
		String approvalsbj = CJMessageConstant.CONNECT_Notification_SUBJECT;

		try {
			Mail mail = new Mail();
			mail.setFrom(CJMessageConstant.CJ_EMAIL);
			mail.setTo(dbuser.getEmailId());
			mail.setSubject(CJMessageConstant.CONNECT_Notification_SUBJECT);
			Map<String, Object> model = new HashMap<String, Object>();
			// model.put("token", token);
			model.put("receiverName", dbuser.getFirstName());
			model.put("senderName", dbuser1.getFirstName());
			model.put("domainUrl", domainUrl);
			mail.setModel(model);

			emailUtil.sendEmail(mail, "existing_user_connection_request.html", null, null, flag);
		} catch (Exception ex) {
			logger.error("Error occured in while sending ConnectionNotification Email:::>>>" + ex);
			throw ex;
		}

		return connectrequest;
	}

	@Override
	public String sendMessage(EmailMessageDto messageDto) {

		List<EmailMessage> emailmsg = new ArrayList<>();

		UserDto receiverUser = userService.findByEmail(messageDto.getTo());
		UserDto senderUser = userService.findByEmail(messageDto.getFrom());

		if (receiverUser.getEmailId() == null || senderUser.getEmailId() == null) {
			return "{\"error\": \" Invalid From or To user's email. \"}";
		}
		boolean flag = true;

		String approvalmsg;
		approvalmsg = "<B>Hey! </B> " + receiverUser.getFirstName() + ",  You got new Message From  "
				+ senderUser.getFirstName() + "</BR>" + "<B> MESSAGE </B> : ";
		approvalmsg = approvalmsg + messageDto.getStrMessage();
		// String register = domainUrl + "/reset" + "&referedemail=" +
		// dbuser.getEmailId() + "\n";
		String signature = CJCommonConstants.emailSignature;
		String messageBody = approvalmsg + signature;
		String approvalsbj = CJMessageConstant.MESSAGE_Notification_SUBJECT;

		try {

			Mail mail = new Mail();
			mail.setFrom(CJMessageConstant.CJ_EMAIL);
			mail.setTo(receiverUser.getEmailId());
			mail.setSubject(CJMessageConstant.MESSAGE_Notification_SUBJECT);

			Map<String, Object> model = new HashMap<String, Object>();
			// model.put("token", token);
			model.put("receiverName", receiverUser.getFirstName());
			// model.put("signature", "https://giveCharity.com");
			// model.put("resetUrl", forgetPassword.getReturnUrl() + "?token=" +
			// token.getToken()+"&reqType=token");
			model.put("senderName", senderUser.getFirstName());
			model.put("message", messageDto.getStrMessage());
			mail.setModel(model);

			userRepository.saveMessage(senderUser.getEmailId(), receiverUser.getEmailId(), messageDto.getStrMessage());

			emailUtil.sendEmail(mail, "message_received.html", null, approvalsbj, flag);
		} catch (Exception ex) {
			logger.error("Error occured in while sending ConnectionNotification Email:::>>>" + ex);
			// throw ex;
			return "{\"error\": \" " + ex.getMessage() + "\"}";
		}

		return "{\"emailSent\": \" " + flag + "\"}";
	}

	@Override
	public User deleteConnection(String loginuseremailId, String deletedemailId) {

		User deleteconnection = userRepository.deleteConnection(loginuseremailId, deletedemailId);

		List<EmailMessage> emailmsg = new ArrayList<>();

		UserDto dbuser = userService.findByEmail(deletedemailId);
		UserDto dbuser1 = userService.findByEmail(loginuseremailId);
		boolean flag = true;

		/*
		 * if (dbuser == null) { String message = "The User does not exist."; return
		 * "error";
		 */

		String approvalmsg = null;
		approvalmsg = dbuser.getFirstName();

		String loginusername = dbuser1.getFirstName();
		// approvalmsg=approvalmsg+"\n <BR><B>Invitation
		// Code:</B>"+invitationCode+"<BR>";
		approvalmsg = "Hey " + approvalmsg + ",  You are removed from Connection by " + loginusername;
		String register = domainUrl + "/reset" + "&referedemail=" + dbuser.getEmailId() + "\n";
		String signature = CJCommonConstants.emailSignature;
		String invitemessge = approvalmsg + signature;
		String approvalsbj = CJMessageConstant.DELETE_CONNECTION_Notification_SUBJECT;

		try {

			i2iCommonUtil.sendMail(new String[] { dbuser.getEmailId() }, invitemessge, approvalsbj);
		} catch (Exception ex) {
			logger.error("Error occured in while sending ConnectionNotification Email:::>>>" + ex);
			throw ex;
		}

		return deleteconnection;
	}

	@Override
	public String updateQuickBoxId(String emailId, String qbid) throws Exception {
		Optional<User> dbUser = userRepository.findByuserEmail(emailId);
		if (dbUser.isPresent() && org.springframework.util.StringUtils.hasText(qbid)) {
			dbUser.get().setQbid(qbid);
			userRepository.save(dbUser.get());
			return "{\"success\": \" " + Boolean.TRUE + "\"}";
		} else {
			return "{\"error\": \" Invalid user email or QuickBoxId to update\"}";
		}
	}

	@Override
	public String createGroup(GroupDto groupDto) throws Exception {
		boolean flag = true;
		if (groupDto.getGroupName() == null) {
			String message = "Please give a meaningful name to this group!";
			return "{\"error\": \" " + String.format(message, new String[] { groupDto.getGroupName() }) + "\"}";
		}
		if (groupDto.getDescription() == null) {
			String message = "Please provide some information about this group!";
			return "{\"error\": \" " + String.format(message, new String[] { groupDto.getDescription() }) + "\"}";
		}
		Group group = new Group();
		BeanUtils.copyProperties(groupDto, group);
		if (groupDto.getGroupId() != null) {
			group.setId(groupDto.getGroupId());
		}

		group.setUserIds(null);
		group.setCreatedDate(LocalDateTime.now());
		group = groupRepo.save(group);

		List<Long> userIds = groupDto.getUserIds();
		if (userIds == null) {
			userIds = new ArrayList<Long>();
		}
		userIds.add(groupDto.getCreatedBy());
		final Long groupId = group.getId();
		userIds.forEach(a -> {
			groupRepo.tagUsers(a.longValue(), groupId);
		});
		// groupRepo.tagUsers(userIds, group.getId());

		if (groupDto.getGroupImage() != null) {
			MultipartFile file = groupDto.getGroupImage();
			String fileName = file.getOriginalFilename();
			Path filelocation = Paths.get(this.fileStorageLocation + "/groups/" + String.valueOf(group.getId()) + "/");
			try {
				Files.createDirectories(filelocation);
				Files.copy(file.getInputStream(), filelocation.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				return "{\"error\": \" Unable to upload group image::" + e.getMessage() + " \"}";
			}
			String imageUrl = CJCommonConstants.GROUP_IMAGE_URL + String.valueOf(group.getId()) + "/" + fileName;
			group.setGroupImageUrl(imageUrl);
		} else {
			return "{\"error\": \" Invalid user email or QuickBoxId to update\"}";
		}
		group = groupRepo.save(group);

		return "{\"groupCreated\": \" true\"}";

	}

	@Override
	public List<UserConnection> connectedUserList(String email) {
		// TODO Auto-generated method stub
		List<User> dbUserList = userRepository.getconnectedUserList(email);
		List<UserConnection> userConnectionlist = new ArrayList<UserConnection>();
		dbUserList.forEach(usr -> {

			UserConnection duconn = new UserConnection();
			duconn.setFirstName(usr.getFirstName());
			duconn.setLastName(usr.getLastName());
			duconn.setProfileImageUrl(usr.getProfileImageUrl());
			duconn.setQbid(usr.getQbid());
			duconn.setUserId(usr.getId());
			duconn.setEmail(usr.getEmailId());
			if (usr.getConnectionDate() != null) {
				duconn.setConnectionDate(
						LocalDateTime.parse(usr.getConnectionDate(), DateTimeFormatter.ISO_ZONED_DATE_TIME));
			}
			userConnectionlist.add(duconn);

		});
		return userConnectionlist;
	}

	@Override
	public Group getGroupDetails(Long groupId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Group> getGrouplist(Long createdBy) {
		List<Group> dbgrouplist = groupRepo.getGroup(createdBy);

		dbgrouplist.forEach(g -> {
			List<User> memberList = groupRepo.memberUserlist(g.getId());
			Long createdUser = g.getCreatedBy();
			memberList.forEach(mem -> {
				if (createdUser.equals(createdBy)) {
					g.setCreatedByName("YOU");
				} else {
					if (mem.getId().equals(createdUser)) {

						g.setCreatedByName(mem.getFirstName() + " " + mem.getLastName());
					}
				}
			});
			g.setMemberList(memberList);

		});
		return dbgrouplist;
	}

	@Override
	public String deleteGroup(Long groupId) {
		groupRepo.deleteGroup(groupId);
		return "{\"groupDeleted\": \" true\"}";
	}

	@Override
	public String sendMessageInGroup(GroupMessageDto groupMessage) {
		UserDto senderUser = userService.findByEmail(groupMessage.getFrom());
		List<User> memberList = groupRepo.memberUserlist(groupMessage.getGroupId());
		boolean flag = true;
		List<String> receiverUsers = new ArrayList<String>();
		for (User u : memberList) {
			System.err.println(u.getFirstName() + u.getId());
			if (groupMessage.getFrom() == u.getId().toString()) {
				System.err.println("same user");
			} else {
				receiverUsers.add(u.getEmailId());
			}
		}

		String approvalmsg;
		approvalmsg = "<B>Hey! </B> <BR>  You got new Message From  " + senderUser.getFirstName() + "<BR>"
				+ "<B> MESSAGE </B> : ";
		approvalmsg = approvalmsg + groupMessage.getStrMessage();
		String signature = CJCommonConstants.groupEmailSignature;
		String messageBody = approvalmsg + signature;
		String approvalsbj = CJMessageConstant.MESSAGE_Notification_SUBJECT;
		String receiverUsersList[] = new String[receiverUsers.size()];
		for (int j = 0; j < receiverUsers.size(); j++) {
			receiverUsersList[j] = receiverUsers.get(j);
		}
		try {
			groupRepo.sendMessageToGroup(groupMessage.getFrom(), groupMessage.getGroupId(),
					groupMessage.getStrMessage());
			i2iCommonUtil.sendMail(receiverUsersList, messageBody, approvalsbj);
		} catch (Exception ex) {
			logger.error("Error occured in while sending ConnectionNotification Email:::>>>" + ex);
			return "{\"error\": \" " + ex.getMessage() + "\"}";
		}
		return "{\"emailSent\": \" " + flag + "\"}";
	}

	public String editGroupMember(GroupDto groupDto) throws Exception {
		if (groupDto.getGroupId() == null) {
			return "{\"error\": \" GroupId cannot null\"}";
		}
		List<Long> userIds = groupDto.getUserIds();
		final Long groupId = groupDto.getGroupId();
		userIds.forEach(a -> {
			groupRepo.tagUsers(a.longValue(), groupId);
		});
		return "{\"groupMemberUpdated\": \" true\"}";

	}

	@Override
	public String removeGroupMember(GroupDto groupDto) throws Exception {
		if (groupDto.getGroupId() == null) {
			return "{\"error\": \" GroupId cannot null\"}";
		}
		List<Long> userIds = groupDto.getUserIds();
		final Long groupId = groupDto.getGroupId();
		userIds.forEach(a -> {
			groupRepo.UntagUsers(a.longValue(), groupId);
		});
		return "{\"groupMemberDeleted\": \" true\"}";
	}

	@Override
	public String post(PostUrlDto post) {
		MultipartFile[] uploadedFiles = post.getFiles();
		Post postData = new Post();
		List<PostUrl> urlList = new ArrayList<PostUrl>();
/*<<<<<<< HEAD
		if(uploadedFiles != null) {
		for (MultipartFile file : uploadedFiles) {
			PostUrl postUrl = new PostUrl();
			String fileName = file.getOriginalFilename();
			if(post.getPrimaryFiles()!=null) {
				post.getPrimaryFiles().forEach(f->{
					if(f.equalsIgnoreCase(fileName)) {
						postUrl.setPrimary(Boolean.TRUE);
					}
				});
			}
			String extension = i2iCommonUtil.getFileExtension(fileName);
			//System.err.println("extension---" + extension);
			Path filelocation = Paths.get(this.fileStorageLocation + "/groups/post/" + String.valueOf(post.getGroupId()) + "/");
			try {
				Files.createDirectories(filelocation);
				Files.copy(file.getInputStream(), filelocation.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				return "{\"error\": \" Unable to upload file::" + e.getMessage() + " \"}";
			}
			if (CJCommonConstants.imageExtension.toUpperCase().contains(extension.toUpperCase())) {
				String imageUrl = CJCommonConstants.GROUP_ATTACHMENT_IMAGE_URL + String.valueOf(post.getGroupId()) + "/"
						+ fileName;
				postUrl.setType("image");
				postUrl.setUrl(imageUrl);
			} else if (CJCommonConstants.vedioExtension.toUpperCase().contains(extension.toUpperCase())) {
				String videoUrl = CJCommonConstants.GROUP_ATTACHMENT_VIDEO_URL + String.valueOf(post.getGroupId()) + "/"
						+ fileName;
				postUrl.setType("video");
				postUrl.setUrl(videoUrl);
			} else if (CJCommonConstants.docExtension.toUpperCase().contains(extension.toUpperCase())) {
				String docUrl = CJCommonConstants.GROUP_ATTACHMENT_DOC_URL + String.valueOf(post.getGroupId()) + "/"
						+ fileName;
				postUrl.setType("document");
				postUrl.setUrl(docUrl);
			}
			if(postUrl.getUrl()==null) {
				return "{\"error\": \" Invalid file type to upload ::" +extension + " \"}";
			}
			urlList.add(postUrl);
		}
	}
		postData.setUrlList(urlList);
=======*/
		postData.setContent(post.getContent());
		postData.setCreatedDate(LocalDateTime.now());
		postData.setIsPrivate(post.getIsPrivate());

		if (post.getId() != null) {
			postData.setId(post.getId());
			postData = postUrlRepo.save(postData);

		} else {
			postData = postUrlRepo.save(postData);
			postUrlRepo.createSenderRelationship(postData.getId(), post.getCreatedBy());
		}
		if (uploadedFiles != null) {
			int imagecount=1;
			int vediocount=1;
			int documentcount=1;
			for (MultipartFile file : uploadedFiles) {
				PostUrl postUrl = new PostUrl();
				String fileName = file.getOriginalFilename();
				if(imagecount>3||vediocount>1||documentcount>1) {
					return "{\"error\": \"limit exceeded  to upload file \"}";
				}
				
				String extension = i2iCommonUtil.getFileExtension(fileName);
				Path filelocation = Paths
						.get(this.fileStorageLocation + "/post/" + String.valueOf(postData.getId()) + "/");
				try {
					Files.createDirectories(filelocation);
					Files.copy(file.getInputStream(), filelocation.resolve(fileName),
							StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					return "{\"error\": \" Unable to upload file::" + e.getMessage() + " \"}";
				}
				if (CJCommonConstants.imageExtension.toUpperCase().contains(extension.toUpperCase())) {
					String imageUrl = CJCommonConstants.GROUP_ATTACHMENT_IMAGE_URL+"post/" + String.valueOf(postData.getId())
							+ "/" + fileName;
					postUrl.setType("image");
					postUrl.setUrl(imageUrl);
					urlList.add(postUrl);
					imagecount=imagecount+1;
				} else if (CJCommonConstants.vedioExtension.toUpperCase().contains(extension.toUpperCase())) {
					String videoUrl = CJCommonConstants.GROUP_ATTACHMENT_VIDEO_URL + "post/"+String.valueOf(postData.getId())
							+ "/" + fileName;
					postUrl.setType("video");
					postUrl.setUrl(videoUrl);
					urlList.add(postUrl);
					vediocount=vediocount+1;
				} else if (CJCommonConstants.docExtension.toUpperCase().contains(extension.toUpperCase())) {
					String docUrl = CJCommonConstants.GROUP_ATTACHMENT_DOC_URL + "post/"+String.valueOf(postData.getId()) + "/"
							+ fileName;
					postUrl.setType("document");
					postUrl.setUrl(docUrl);
					urlList.add(postUrl);
					documentcount=documentcount+1;
				}
			}
			
			postData.setUrlList(urlList);
			postData=postUrlRepo.save(postData);
		}

		Long postId = postData.getId();
		
		if(postData.getUrlList()!=null) {
			Long[] postUrlIds = new Long[20];
			List<PostUrl> urlListData = postData.getUrlList();
			
			int count = 0;
			for (PostUrl url : urlListData) {
				postUrlIds[count] = url.getId();
				count++;
			}
			postUrlRepo.createHasAssociatedUrlRelationship(postId, postUrlIds);
		}
		if (post.getGroupId() != null) {
			Long[] groupIds = new Long[post.getGroupId().size()];
			List<Long> groupIdList = post.getGroupId();
			int flag = 0;
			for (Long id : groupIdList) {
				System.err.println("groupId----" + id);
				groupIds[flag] = id;
				flag++;
			}
			
			if (groupIds.length == 0)
				System.err.println("Its not a group post");
			else
				post.getGroupId().forEach(g -> {
					postUrlRepo.createReceiverRelationship(postId, g);
				});
		}
/*<<<<<<< HEAD
		postUrlRepo.createHasAssociatedUrlRelationship(postId, postUrlIds);
//		if(groupIds.length == 0)
//			System.err.println("Its not a group post");
//		else
//			post.getGroupId().forEach(g->{
//				postUrlRepo.createReceiverRelationship(postId, g);
//			});
//		postUrlRepo.createSenderRelationship(postId, userId);
		
		return "{\"posted successfully\": \" true\"}";
=======*/
		if (post.getTagUsers() != null) {
			List<Long> tagUsers = post.getTagUsers();
			if (tagUsers.size() == 0) {
				System.err.println("no user is being tagged");
			}else {
				tagUsers.forEach(usr->{
					postUrlRepo.createTaggedUserRelationship(postId, usr);
				});
			}
			
		}
		return "{\"success\": \" true\"}";
	}
	
	@Override
	public String deletePost(Long postId) {
		Post postDb = new Post();
		postDb.setId(postId);
		Optional<Post> dbPost = postUrlRepo.findById(postId);
		if (dbPost.isPresent()) {
			postDb.setIsDeleted(Boolean.TRUE);
			postUrlRepo.save(postDb);
		} else {
			return "{\"error\": \" Invalid post to delete.\"}";
		}
		return "{\"success\": \" true\"}";
	}
	
}
