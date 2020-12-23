package com.cts.cj.service;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.cts.cj.domain.FileUpload;
import com.cts.cj.domain.User;
import com.cts.cj.domain.UserProfile;
import com.cts.cj.dto.UserDto;

public interface UserProfileService {
	
	public UserDto getProfile(Long id);
	
	public String saveUserProfile(UserDto user);
	
	public void saveDoc(MultipartFile file,Long userId,String uploadType) throws IOException;
	
	public List<FileUpload> getDocList(Long userId);
	
	public FileUpload getUpload(Long userId,String UploadType);

	public Resource loadFileAsResource(String fileName);
	public Resource loadFileAsResource(String fileName,Long userId);
	
	public Resource loadgroupFileAsResource(String fileName, Long groupId);
	public String connectUser(Long id,Long userId);
	

}
