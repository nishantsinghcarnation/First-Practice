package com.cts.cj.service;

import java.util.List;

import com.cts.cj.domain.User;

public interface ConnectionService {
	
	List<User> getConnections(String email);

}
