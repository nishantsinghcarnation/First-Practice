package com.cts.cj.service;

import com.cts.cj.domain.User;

public interface LoginService {
	
	
	User isUserExist(String email,String pass);
	
	User getExistingUser(User user);

}
