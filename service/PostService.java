package com.cts.cj.service;

import java.util.List;

import org.springframework.core.io.Resource;

import com.cts.cj.domain.Post;
import com.cts.cj.dto.CommentDto;
import com.cts.cj.dto.PostDto;

public interface PostService {

	List<PostDto> getPost(Long createdBy);
	Post viewPost(Long postId);
	Resource loadpostFileAsResource(String fileName, Long postId);
	String postComment(CommentDto commentDto);
	void tagUsers(Long postId, Long[] users);
	public String deleteComment(Long commentId);
}
