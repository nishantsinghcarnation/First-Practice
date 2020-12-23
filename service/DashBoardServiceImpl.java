package com.cts.cj.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cts.cj.domain.PostPrevious;
import com.cts.cj.domain.User;
import com.cts.cj.dto.SharePostResponse;
import com.cts.cj.dto.UserResponse;
import com.cts.cj.repository.DashBoardRepository;
import com.cts.cj.repository.UserRepository;
import com.cts.cj.util.BeanMapperUtil;

@Service
public class DashBoardServiceImpl implements DasBoardService{
	
	@Autowired
	DashBoardRepository dashBoardRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	BeanMapperUtil beanMapperUtil;
	
	@Override
	public List<UserResponse> findMyMentorMenties(Long userId,String  roleType) {
		List<User> users = dashBoardRepository.findMyMentorMenties(userId,roleType);
		return beanMapperUtil.mapAsUserResponseList(users);
	}

	@Override
	public PostPrevious createPost(PostPrevious post){
		List<User> menties = dashBoardRepository.findMyMentorMenties(post.getUserId(), post.getWhomToShare());
		List<PostPrevious> postPublished = new ArrayList<>();
		User alreadyexist = userRepository.getUserbyId(post.getUserId());
		PostPrevious postgenerate= new PostPrevious();
	    postgenerate.setCreated(LocalDateTime.now());
	    postgenerate.setUpdated(LocalDateTime.now());
		postgenerate.setPostDescription(post.getPostDescription());
		postgenerate.setUserId(post.getUserId());
		postgenerate.setUsertoshare(menties);
		postPublished.add(postgenerate);
		//alreadyexist.setPostExists(postPublished);
		userRepository.save(alreadyexist);
		return null;
	}
	@Override
	public List<SharePostResponse> getAllPost(Long id) {
		List<User> userSharePost = new ArrayList<>();
		List<PostPrevious> userwithPost = new ArrayList<>();
		List<User> user = dashBoardRepository.getAllPost(id);
		for (User userShared : user) {
			User usercustom = new User();
			List<PostPrevious> usersharedPost = new ArrayList();//userShared.getPostExists();
			for (PostPrevious postRecived : usersharedPost) {	
				User sharedUsername = userRepository.getUserbyId(postRecived.getUserId());
				PostPrevious post = new PostPrevious();
				post.setFirstName(sharedUsername.getFirstName()+" " +sharedUsername.getLastName());	
				post.setMobileNumer(sharedUsername.getMobileNumber());
				post.setPostId(postRecived.getPostId());
				String postCreatedDate = postRecived.getCreated().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
				String postDescription = postRecived.getPostDescription();
				post.setPostCreatedDate(postCreatedDate);
				post.setPostDescription(postDescription);
				userwithPost.add(post);
			}
			//usercustom.setPostExists(userwithPost);
			userSharePost.add(usercustom);
		}

		return beanMapperUtil.mapAsShareRequestResponseList(userSharePost);
	}
}
