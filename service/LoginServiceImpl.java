package com.cts.cj.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cts.cj.domain.User;
import com.cts.cj.repository.UserRepository;

@Service
public class LoginServiceImpl implements LoginService {
	
	@Autowired
	private UserRepository userRepository;
	
	  private static BCryptPasswordEncoder bCryptPasswordEncoder=new BCryptPasswordEncoder();

	 
	
	@Override public User isUserExist(String email,String password) {
		Optional<User> user=userRepository.findByuserEmail(email); 
		if(bCryptPasswordEncoder.matches(password, user.get().getPassword())) {
			user.get().setPassword(null);
			return user.get(); 
		}else {
			return null;
		}
		
		}

	@Override
	public User getExistingUser(User user) {
		userRepository.getExistingUser(user.getEmailId(), user.getPassword(),user.getFirstName());
		return null;
	}
}
