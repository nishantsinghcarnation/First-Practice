package com.cts.cj.service;

import java.util.List;

import com.cts.cj.domain.PostPrevious;
import com.cts.cj.dto.SharePostResponse;
import com.cts.cj.dto.UserResponse;

public interface DasBoardService {

	public List<UserResponse> findMyMentorMenties(Long userId,String  roleType);
	
	public PostPrevious createPost(PostPrevious post);
	
	public List<SharePostResponse> getAllPost(Long id);
	
}
