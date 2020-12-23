package com.cts.cj.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class UserChatServiceImpl implements UserChatService {

	@Autowired
	private RestTemplate restTemplate; 
	
	@Value("${cj.mesibo.url}")
	private String mesiboApiUrl;
	@Override
	public String getMesiboUserDetails(String Operation, String token, String appId, String emailAddress) {
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		// build the request
		HttpEntity<?> request = new HttpEntity<>(headers);
		// make an HTTP GET request with headers
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(mesiboApiUrl).queryParam("op", "useradd")
				.queryParam("token", token).queryParam("appid", appId).queryParam("addr", emailAddress);
		ResponseEntity<String> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, request,
				String.class);
		  if (response.getStatusCode() == HttpStatus.OK) {
			 // System.out.println("mesiBodata>>>"+response.getBody());
			  return response.getBody();
		  }
		 
		return "{\"error\": \" could not make  api call \"}";
	}

}
