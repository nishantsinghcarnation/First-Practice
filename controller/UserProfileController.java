package com.cts.cj.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cts.cj.domain.Country;
import com.cts.cj.domain.FileUpload;
import com.cts.cj.domain.OccupationStaus;
import com.cts.cj.domain.ProfessionType;
import com.cts.cj.domain.UserProfile;
import com.cts.cj.dto.SkillDto;
import com.cts.cj.dto.UserDto;
import com.cts.cj.service.CountryService;
import com.cts.cj.service.OccupStatusService;
import com.cts.cj.service.PostService;
import com.cts.cj.service.ProfessionService;
import com.cts.cj.service.SkillService;
import com.cts.cj.service.UserProfileService;
import com.cts.cj.service.UserServiceImpl;


@RestController
public class UserProfileController {
	
	public static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	
	@Value("${i2i.secure.context_path}")
	private String secureContextPath;
	
	
	@Value("${i2i.non_secure.context_path}")
	private String nonsecureContextPath;
		
	
	@Autowired
	UserProfileService userProfileService;
	
	@Autowired
	CountryService countryService;
	
	@Autowired 
	ProfessionService professionService;
	
	@Autowired
	private OccupStatusService occupStatusService;
	
	@Autowired
	private SkillService skillservice;
	@Autowired
	private PostService pstService;
	
	/**
	 * @param userId
	 * @return
	 */
	@CrossOrigin("*")
	@GetMapping(path ="${i2i.secure.context_path}"+"/ua/getprofile/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<UserDto> getProfileById(@PathVariable Long userId) {
		return new ResponseEntity<UserDto>(userProfileService.getProfile(userId), HttpStatus.OK);
	}
	/**
	 * @param userId
	 * @return
	 */
	@CrossOrigin("*")
	@PostMapping(path ="${i2i.secure.context_path}"+"/ua/saveprofile")
	public ResponseEntity<String> saveUserProfile(@ModelAttribute UserDto user) {
		return new ResponseEntity<String>(userProfileService.saveUserProfile(user),HttpStatus.OK);
	}
	
	/**
	 * @return
	 */
	@CrossOrigin("*")
	@GetMapping(path ="${i2i.secure.context_path}"+"/ua/getAllCountries", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<Country>> getAllCountries() {
		return new ResponseEntity<List<Country>>(countryService.getCountryList(),HttpStatus.OK);
	}
	
	/**
	 * @return
	 */
	@CrossOrigin("*")
	@GetMapping(path ="${i2i.secure.context_path}"+"/ua/getProfessions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<ProfessionType>> getProfessions() {
		return new ResponseEntity<List<ProfessionType>>(professionService.getProfessionList(),HttpStatus.OK);
	}
	/**
	 * @return
	 */
	@CrossOrigin("*")
	@GetMapping(path ="${i2i.secure.context_path}"+"/ua/getOccupationStatus", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<List<OccupationStaus>> getOccupationStatus() {
		return new ResponseEntity<List<OccupationStaus>>(occupStatusService.getOccupStatusList(),HttpStatus.OK);
	}
	/**
	 * @param MultipartFile file
	 */
	@CrossOrigin("*")
	@PostMapping(path = "${i2i.secure.context_path}"+ "/ua/uploadImg/{userId}/{uploadType}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Object> uploadImg(@RequestParam("file") MultipartFile[] file,@PathVariable Long userId,@PathVariable String uploadType)
			throws IOException {
		for (int i = 0; i < file.length; i++)  {
		   userProfileService.saveDoc(file[i], userId,uploadType);
		}
		return new ResponseEntity<Object>("file Uplaoded succesfully", HttpStatus.OK);
	}
	/**
	 * @param MultipartFile file
	 */
	@CrossOrigin("*")
	@GetMapping(path = "${i2i.secure.context_path}"+ "/ua/getDocList/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<List<FileUpload>> getDocList(@PathVariable Long userId)throws IOException {
		return new ResponseEntity<List<FileUpload>>(userProfileService.getDocList(userId), HttpStatus.OK);
	}
	/**
	 * @param fileName
	 * @param request
	 * @return
	 */
	@CrossOrigin("*")
	@GetMapping("${i2i.secure.context_path}" + "/ua/downloadFile/{fileName:.+}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
		Resource resource = userProfileService.loadFileAsResource(fileName);
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			logger.info("Could not determine file type.");
		}
		if (contentType == null) {
			contentType = "application/octet-stream";
		}
		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

	
@CrossOrigin("*")
@GetMapping("${i2i.non_secure.context_path}" + "/ua/profileImage/{userId}/{fileName:.+}")
public ResponseEntity<Resource> downloadFile(@PathVariable Long userId,@PathVariable String fileName, HttpServletRequest request) {
	Resource resource = userProfileService.loadFileAsResource(fileName,userId);
	String contentType = null;
	try {
		contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
	} catch (IOException ex) {
		logger.info("Could not determine file type.");
	}
	if (contentType == null) {
		contentType = "application/octet-stream";
	}
	return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
			.body(resource);
}

@CrossOrigin("*")
@PostMapping(path ="${i2i.secure.context_path}"+"/ua/addskill")
public ResponseEntity<String> saveskill(@RequestBody List<SkillDto> skill) {
	return new ResponseEntity<String>(skillservice.addSkill(skill),HttpStatus.OK);
}
@CrossOrigin("*")
@PostMapping(path ="${i2i.secure.context_path}"+"/ua/updateskill")
public ResponseEntity<String> updateSkill(@RequestBody SkillDto skill) {
	return new ResponseEntity<String>(skillservice.updateSkill(skill),HttpStatus.OK);
}
@CrossOrigin("*")
@PostMapping(path ="${i2i.secure.context_path}"+"/ua/deleteskill")
public ResponseEntity<String> deleteSkill(@RequestBody SkillDto skill) {
	return new ResponseEntity<String>(skillservice.deleteSkill(skill),HttpStatus.OK);
}

@CrossOrigin("*")
@GetMapping(path ="${i2i.secure.context_path}"+"/ua/getAllSkill")
public ResponseEntity<List<SkillDto>> getAllSkill() {
	return new ResponseEntity<List<SkillDto>>(skillservice.getAllSkill(),HttpStatus.OK);
}
@CrossOrigin("*")
@GetMapping(path ="${i2i.non_secure.context_path}"+"/ua/searchSkill")
public ResponseEntity<List<SkillDto>> searchSkill(@RequestParam String name) {
	return new ResponseEntity<List<SkillDto>>(skillservice.serachSkillList(name),HttpStatus.OK);
}
@CrossOrigin("*")
@GetMapping(path ="${i2i.secure.context_path}"+"/ua/connect")
public ResponseEntity<String> connectUser(@RequestParam Long id, Long userId) {
	return new ResponseEntity<String>(userProfileService.connectUser(id, userId),HttpStatus.OK);
}

@CrossOrigin("*")
@GetMapping("${i2i.non_secure.context_path}" + "/ua/groupImage/{groupId}/{fileName:.+}")
public ResponseEntity<Resource> downloadGroupFile(@PathVariable Long groupId,@PathVariable String fileName, HttpServletRequest request) {
	Resource resource = userProfileService.loadgroupFileAsResource(fileName, groupId);
	String contentType = null;
	try {
		contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
	} catch (IOException ex) {
		logger.info("Could not determine file type.");
	}
	if (contentType == null) {
		contentType = "application/octet-stream";
	}
	return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
			.body(resource);
}

@CrossOrigin("*")
@GetMapping("${i2i.non_secure.context_path}" + "/ua/image/post/{postId}/{fileName:.+}")
public ResponseEntity<Resource> downloadPostFiles(@PathVariable Long postId,@PathVariable String fileName, HttpServletRequest request) {
	Resource resource = pstService.loadpostFileAsResource(fileName, postId);
	String contentType = null;
	try {
		contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
	} catch (IOException ex) {
		logger.info("Could not determine file type.");
	}
	if (contentType == null) {
		contentType = "application/octet-stream";
	}
	return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
			.body(resource);
}

@CrossOrigin("*")
@GetMapping("${i2i.non_secure.context_path}" + "/ua/video/post/{postId}/{fileName:.+}")
public ResponseEntity<Resource> downloadPostvedioFiles(@PathVariable Long postId,@PathVariable String fileName, HttpServletRequest request) {
	Resource resource = pstService.loadpostFileAsResource(fileName, postId);
	String contentType = null;
	try {
		contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
	} catch (IOException ex) {
		logger.info("Could not determine file type.");
	}
	if (contentType == null) {
		contentType = "application/octet-stream";
	}
	return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
			.body(resource);
}

@CrossOrigin("*")
@GetMapping("${i2i.non_secure.context_path}" + "/ua/document/post/{postId}/{fileName:.+}")
public ResponseEntity<Resource> downloadPostdocumentFiles(@PathVariable Long postId,@PathVariable String fileName, HttpServletRequest request) {
	Resource resource = pstService.loadpostFileAsResource(fileName, postId);
	String contentType = null;
	try {
		contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
	} catch (IOException ex) {
		logger.info("Could not determine file type.");
	}
	if (contentType == null) {
		contentType = "application/octet-stream";
	}
	return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
			.body(resource);
}

}
