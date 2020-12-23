package com.cts.cj.service;
import java.util.List;
import javax.mail.MessagingException;
import com.cts.cj.constants.Status;
import com.cts.cj.domain.Group;
import com.cts.cj.domain.Post;
import com.cts.cj.domain.Prospect;
import com.cts.cj.domain.ResetPassword;
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

public interface UserService {

	List<User> getAll();

	void deleteAll(String emailid, String mobileNumber, String user);

	public String register(UserDto user) throws UserAlreadyExistsException, MessagingException;

	public User getById(Long id);

	public User update(User user);

	public User changePassword(User user) throws MessagingException;

	public User forgetPwd(User user);

	public String UpdateStatus(User user);

	public String sendInvite(String emailIds, String roletype, String userId)
			throws UserAlreadyExistsException, Exception;

	public UserDto findByEmail(String emailid);

	public User updateUserStatus(String email, Status status);

	public Prospect registerProspect(Prospect Prospect) throws UserAlreadyExistsException;

	public UserDto findByInviteEmail(String emailid);

	List<UserConnection> getConnections(String emailid);

	List<SearchDto> getUserByFirstName(String loginuseremail, String searchstring);

	public String forgotpassword(String emailId) throws Exception;

	public String resetPassword(ResetPassword resetPassword) throws Exception;

	List<UserConnection> getAllRecommendations(String emailId);

	User connectUser(String loginuseremailid, String connecteduseremailid);

	public String sendMessage(EmailMessageDto messageDto);

	User deleteConnection(String loginuseremailId, String deletedemailId);

	public void changePassword(String changedPassword, String userEmail, String currentPassword) throws Exception;

	List<SentInvitesList> sentInvitesList(String emailId);

	List<SentInvitesList> receivedInvitesList(String emailId);

	public String declineRequest(String emailId, String declinedEmail) throws Exception;

	public String ignoreRequest(String emailId, String ignoredEmail) throws Exception;

	public String confirmRequest(String emailId, String confirmedEmail) throws Exception;

	public String updateQuickBoxId(String emailId, String quickboxId) throws Exception;

	public String createGroup(GroupDto groupDto) throws Exception;

	List<UserConnection> connectedUserList(String email);

	public Group getGroupDetails(Long groupId);

	public List<Group> getGrouplist(Long createdBy);

	public String deleteGroup(Long groupId);

	String sendMessageInGroup(GroupMessageDto groupMessage);
   
	public String  editGroupMember(GroupDto groupDto)throws Exception;
   
   public String  removeGroupMember(GroupDto groupDto)throws Exception;

   String post(PostUrlDto post);
   
   public String deletePost(Long postId);
   
}
