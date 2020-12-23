package com.cts.cj.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.cts.cj.constants.CJCommonConstants;
import com.cts.cj.domain.Education;
import com.cts.cj.domain.FileUpload;
import com.cts.cj.domain.Occupation;
import com.cts.cj.domain.Skill;
import com.cts.cj.domain.User;
import com.cts.cj.dto.UserDto;
import com.cts.cj.repository.FileUploaderRepository;
import com.cts.cj.repository.UserProfileRepository;
import com.cts.cj.repository.UserRepository;
import com.cts.cj.util.FileStorageProperties;


@Service
public class UserProfileServiceImpl implements UserProfileService {
	public static final Logger logger = LoggerFactory.getLogger(UserProfileServiceImpl.class);
	@Autowired
	UserProfileRepository userProfileRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	FileUploaderRepository fileUploaderRepository;
	
	 private final Path fileStorageLocation;
	 
	   @Value("${i2i.secure.context_path}")
		private String secureContextPath;
		
		@Value("${i2i.non_secure.context_path}")
		private String nonsecureContextPath;
	 

	@Autowired
	public UserProfileServiceImpl(FileStorageProperties fileStorageProperties) {
		this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
		try {
			Files.createDirectories(this.fileStorageLocation);
		} catch (Exception ex) {
			throw new com.cts.cj.exception.FileStorageException(
					"Could not create the directory where the uploaded files will be stored.", ex);
		}
	}
	
	    
	@Override
	public UserDto getProfile(Long id) {
		User dbUser=userRepository.findUserProfileDetailById(id);
		UserDto usr= new UserDto();
		if(dbUser!=null) {
			dbUser.setPassword(null);
			BeanUtils.copyProperties(dbUser, usr);
		}
		return usr;		
	}

	@Override
	public String saveUserProfile(UserDto user) {
		User dbuser = userRepository.findByEmail(user.getEmailId());
		if (dbuser != null && user.getIsBasicDetail()) {
			dbuser.setFirstName(user.getFirstName());
			dbuser.setLastName(user.getLastName());
			dbuser.setGender(user.getGender());
			dbuser.setNationality(user.getNationality());
			dbuser.setMaritalStatus(user.getMaritalStatus());
			if(user.getDateOfBirth()!=null){
				try {
			LocalDate dob=LocalDate.parse(user.getDateOfBirth(), DateTimeFormatter.ofPattern("MM-dd-yyyy"));
			if(dob.isAfter(LocalDate.now())) {
				return "{\"error\": \" Invalid Date of Birth, Date of Birth can not be future date \"}";
			}
			dbuser.setDateOfBirth(dob.format(DateTimeFormatter.ofPattern("MM-dd-yyyy")));
				}catch(DateTimeException ex) {
					
					return "{\"error\": \" Invalid Date of Birth, Date of Birth should be in MM/DD/YYYY format \"}";
				}
			}
		}
		if (dbuser != null && user.getIsContactDetail()) {
			dbuser.setCity(user.getCity());
			dbuser.setState(user.getState());
			dbuser.setAddressLine1(user.getAddressLine1());
			dbuser.setAddressLine2(user.getAddressLine2());
			dbuser.setMobileNumber(user.getMobileNumber());
			dbuser.setAlternateMobileNumber(user.getAlternateMobileNumber());
			dbuser.setAlternateEmail(user.getAlternateEmail());
			dbuser.setZipCode(user.getZipCode());
			dbuser.setCountry(user.getCountry());
		}
		if(dbuser!=null ) {
			if(!user.getIsDeleteProfileImage() && user.getProfileImage()!=null) {
			MultipartFile file=user.getProfileImage();
			String fileName = file.getOriginalFilename();
			Path filelocation=Paths.get(this.fileStorageLocation+"/"+String.valueOf(dbuser.getId())+"/");
			try {
				Files.createDirectories(filelocation);
				Files.copy(file.getInputStream(), filelocation.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				return "{\"error\": \" Unable to upload profile image::"+e.getMessage()+" \"}";
			}
			String imageUrl=CJCommonConstants.PROFILE_IMAGE_URL+String.valueOf(dbuser.getId())+"/"+fileName;
			dbuser.setProfileImageUrl(imageUrl);
			}
			if(user.getIsDeleteProfileImage()) {
				dbuser.setProfileImageUrl(null);
			}
		}
		if (dbuser != null && user.getIsEducationDetail()) {
			List<Education> educationListUpdated = new ArrayList<>();
			List<Education> education = user.getEducation();
			for (int i = 0; i < education.size(); i++) {
				Education educationUpdated = new Education();
				educationUpdated.setEducationId(education.get(i).getEducationId());
				educationUpdated.setQualificationType(education.get(i).getQualificationType());
				educationUpdated.setInstitution(education.get(i).getInstitution());
				educationUpdated.setStartDate(education.get(i).getStartDate());
				educationUpdated.setEndDate(education.get(i).getEndDate());
				educationUpdated.setUserId(dbuser.getId());
				educationUpdated.setCreatedDate(LocalDateTime.now());
				if(educationUpdated.isTillDate()) {
					educationUpdated.setEndDate(null);
				}
				educationListUpdated.add(educationUpdated);
				dbuser.setEducation(educationListUpdated);
			}
		}
		if (dbuser != null && user.getIsExperienceDetail()) {
			List<Occupation> updatedOccupationList = new ArrayList<>();
			List<Occupation> occupation = user.getOccupation();
			for (int i = 0; i < occupation.size(); i++) {
				Occupation dbOccuption= new Occupation();
				dbOccuption.setOccupationId(occupation.get(i).getOccupationId());
				dbOccuption.setOccupationName(occupation.get(i).getOccupationName());
				dbOccuption.setDesignation(occupation.get(i).getDesignation());
				dbOccuption.setEmployerName(occupation.get(i).getEmployerName());
				dbOccuption.setStartdate(occupation.get(i).getStartdate());
				dbOccuption.setEndDate(occupation.get(i).getEndDate());
				dbOccuption.setOccupationStatus(occupation.get(i).getOccupationStatus());
				dbOccuption.setUserId(dbuser.getId());
				dbOccuption.setTillDate(occupation.get(i).isTillDate());
				dbOccuption.setCreatedDate(LocalDateTime.now());
				if(dbOccuption.isTillDate()) {
					dbOccuption.setEndDate(null);
				}
				updatedOccupationList.add(dbOccuption);
				dbuser.setOccupation(updatedOccupationList);
			}
			
		}
		if (dbuser != null && user.getIsSkillDetail()) {
			List<Skill> updatedskillList = new ArrayList<>();
			List<Skill> skill = user.getSkill();
			/*
			 * for (int i = 0; i < skill.size(); i++) { Skill usrSkill= new Skill();
			 * usrSkill.setName(skill.get(i).getName());
			 * usrSkill.setId(skill.get(i).getId()); updatedskillList.add(usrSkill);
			 * dbuser.setSkill(updatedskillList); }
			 */
		}
		if(dbuser != null) {
			dbuser = userRepository.save(dbuser);
		}
		Long userId=dbuser.getId();
		if(dbuser != null && user.getIsEducationDetail()) {
			//userRepository.deleteHasEducationRelationship(userId);
			userRepository.createEducationRelationship(userId);
		}
		if(dbuser != null && user.getIsExperienceDetail()) {
			//userRepository.deleteHasOccupationRelationship(userId);
			userRepository.createOccupationRelationship(userId);
		}
		
		if (dbuser != null && user.getIsSkillDetail()) {
			userRepository.deleteHasSKILLRelationship(userId);
			List<Skill> skill = user.getSkill();
			skill.forEach(s->{
				userRepository.createHasSKILLRelationship(userId,s.getName());
			});
		}
		BeanUtils.copyProperties(dbuser, user);
		user.setPassword(null);

		/*
		 * if(userprofileid!=null) { alreadyexist =
		 * userRepository.getUserbyId(userprofileid); }
		 * alreadyexist.setFirstName(userProfile.getFirstName());
		 * alreadyexist.setLastName(userProfile.getLastName());
		 * alreadyexist.setMobileNumber(userProfile.getMobileNumber());
		 * alreadyexist.setAlternateMobileNumber(userProfile.getAlternateMobileNumber())
		 * ; alreadyexist.setGender(userProfile.getGender());
		 * alreadyexist.setNationality(userProfile.getNationality());
		 * alreadyexist.setMaritalStatus(userProfile.getMaritalStatus());
		 * alreadyexist.setDateOfBirth(userProfile.getDateOfBirth());
		 * alreadyexist.setCountry(userProfile.getCountryCode());
		 * userRepository.save(alreadyexist); UserProfile userprofil =
		 * userProfileRepository.getUserProfile(userprofileid);
		 * userprofil.setFirstName(userProfile.getFirstName());
		 * userprofil.setLastName(userProfile.getLastName());
		 * userprofil.setDateOfBirth(userProfile.getDateOfBirth());
		 * userprofil.setGender(userProfile.getGender());
		 * userprofil.setNationality(userProfile.getNationality());
		 * userprofil.setAlternateEmail(userProfile.getAlternateEmail());
		 * userprofil.setAlternateMobileNumber(userProfile.getAlternateMobileNumber());
		 * userprofil.setCountryCode(userProfile.getCountryCode());
		 * userprofil.setMobileNumber(userProfile.getMobileNumber());
		 * userprofil.setEmailId(userProfile.getEmailId())
		 */;
		/*
		 * List<Education> educationListUpdated = new ArrayList<>(); List<Education>
		 * education = userProfile.getEducation();
		 * 
		 * for (int i = 0; i < education.size(); i++) { Education educationUpdated = new
		 * Education();
		 * educationUpdated.setQualificationType(education.get(i).getQualificationType()
		 * ); educationUpdated.setInstitution(education.get(i).getInstitution());
		 * educationUpdated.setStartDate(education.get(i).getStartDate());
		 * educationUpdated.setEndDate(education.get(i).getStartDate());
		 * educationUpdated.setUserId(userprofileid);
		 * educationListUpdated.add(educationUpdated); }
		 * userprofil.setEducation(educationListUpdated);
		 * 
		 * Address address = userProfile.getAddress(); Address addressUpdated = new
		 * Address(); addressUpdated.setAddress1(address.getAddress1());
		 * addressUpdated.setAddress2(address.getAddress2());
		 * addressUpdated.setAlternateEmail(address.getAlternateEmail());
		 * addressUpdated.setCity(address.getCity());
		 * addressUpdated.setMobileNumber(address.getMobileNumber());
		 * addressUpdated.setState(address.getState());
		 * addressUpdated.setPinCode(address.getPinCode());
		 * addressUpdated.setUserId(userprofileid);
		 * userprofil.setAddress(addressUpdated);
		 * 
		 * 
		 * 
		 * IdentityDetails identityDetails = userProfile.getIdentityDetails();
		 * IdentityDetails identityDetail = new IdentityDetails();
		 * identityDetail.setDriverLicence(identityDetails.getDriverLicence());
		 * identityDetail.setAadharNumber(identityDetails.getAadharNumber());
		 * identityDetail.setPan(identityDetails.getPan());
		 * identityDetail.setsSN(identityDetails.getsSN());
		 * identityDetail.setPassport(identityDetails.getPassport());
		 * identityDetail.setUserId(userprofileid);
		 * 
		 * userprofil.setIdentityDetails(identityDetail);
		 * 
		 * List<Occupation> occupationUpdated = new ArrayList<>(); List<Occupation>
		 * occupation = userProfile.getOccupations();
		 * 
		 * for (int i = 0; i < occupation.size(); i++) { Occupation occupationUser = new
		 * Occupation();
		 * occupationUser.setOccupationId(occupation.get(i).getOccupationId());
		 * occupationUser.setDesignation(occupation.get(i).getDesignation());
		 * occupationUser.setEmployerName(occupation.get(i).getEmployerName());
		 * occupationUser.setStartdate(occupation.get(i).getStartdate());
		 * occupationUser.setTillDate(occupation.get(i).isTillDate()); if
		 * (occupationUser.isTillDate()) { LocalDate today = LocalDate.now(); String
		 * endDate = today.toString(); occupationUser.setEndDate(endDate); }
		 * 
		 * else { occupationUser.setEndDate(occupation.get(i).getEndDate()); }
		 * 
		 * occupationUser.setOccupationStatusId(occupation.get(i).getOccupationStatusId(
		 * )); occupationUser.setUserId(userprofileid);
		 * occupationUpdated.add(occupationUser); }
		 * 
		 * userprofil.setOccupations(occupationUpdated);
		 * userProfileRepository.save(userprofil);
		 */
		return "{\"profileUpdate\": \" true\"}";
	}

	@Override
	public void saveDoc(MultipartFile file, Long userId, String uploadType) throws IOException {

		String fileName = file.getOriginalFilename();
		Path targetLocation = this.fileStorageLocation.resolve(fileName);
		Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
	    String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path(secureContextPath+"/ua/").path(fileName).toUriString();
		FileUpload filetoUpdated = getUpload(userId, uploadType);
		FileUpload filetoUpload = new FileUpload();
		if (filetoUpdated != null) {
			filetoUpload.setFileId(filetoUpdated.getFileId());
			filetoUpload.setFileName(filetoUpdated.getFileName());
			filetoUpload.setFileId(filetoUpdated.getFileId());
			filetoUpload.setUploadedUrl(filetoUpdated.getUploadedUrl());
			filetoUpload.setUploadType(filetoUpdated.getUploadType());
			filetoUpload.setUploadedTime(LocalDateTime.now());
			filetoUpload.setUserId(filetoUpdated.getUserId());
		} else {
			filetoUpload.setFileName(file.getOriginalFilename());
			filetoUpload.setUploadedTime(LocalDateTime.now());
		    filetoUpload.setUploadedUrl(fileDownloadUri);
			filetoUpload.setUploadType(uploadType);
			filetoUpload.setUserId(userId);
		}
		
		fileUploaderRepository.save(filetoUpload);
	}

	@Override
	public List<FileUpload> getDocList(Long imageId) {
		return fileUploaderRepository.getDocList(imageId);

	}

	@Override
	public FileUpload getUpload(Long userId, String UploadType) {
		return fileUploaderRepository.getUpload(userId, UploadType);
	}
	
	public Resource loadFileAsResource(String fileName) {
		 try {
	            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
	            Resource resource = new UrlResource(filePath.toUri());
	            if(resource.exists()) {
	                return resource;
	            } else {
	                throw new com.cts.cj.exception.MyFileNotFoundException("File not found " + fileName);
	            }
	        } catch (MalformedURLException ex) {
	            throw new com.cts.cj.exception.MyFileNotFoundException("File not found " + fileName, ex);
	        }
	    }
	
	
	public Resource loadFileAsResource(String fileName, Long userId) {
		 try {
	            Path filePath = Paths.get(this.fileStorageLocation+"/"+String.valueOf(userId)+"/").resolve(fileName).normalize();
	            Resource resource = new UrlResource(filePath.toUri());
	            if(resource.exists()) {
	                return resource;
	            } else {
	                throw new com.cts.cj.exception.MyFileNotFoundException("File not found " + fileName);
	            }
	        } catch (MalformedURLException ex) {
	            throw new com.cts.cj.exception.MyFileNotFoundException("File not found " + fileName, ex);
	        }
	    }

	public Resource loadgroupFileAsResource(String fileName, Long groupId) {
		 try {
	            Path filePath = Paths.get(this.fileStorageLocation+"/groups/"+String.valueOf(groupId)+"/").resolve(fileName).normalize();
	            Resource resource = new UrlResource(filePath.toUri());
	            if(resource.exists()) {
	                return resource;
	            } else {
	                throw new com.cts.cj.exception.MyFileNotFoundException("File not found " + fileName);
	            }
	        } catch (MalformedURLException ex) {
	            throw new com.cts.cj.exception.MyFileNotFoundException("File not found " + fileName, ex);
	        }
	    }

	@Override
	public String connectUser(Long id, Long userId) {
		try {
			userRepository.connectUser(id, userId);
			return "{\"connected\": \" true \"}";
		}catch(Exception ex) {
			logger.error("Error in connect User"+ex.getMessage());
			return "{\"error\": \" Can not connect user \"}";
		}
		
	}
}
