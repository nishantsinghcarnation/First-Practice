package com.cts.cj.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cts.cj.configuration.JwtTokenUtil;
import com.cts.cj.domain.ApiResponse;
import com.cts.cj.domain.AuthToken;
import com.cts.cj.domain.LoginUser;
import com.cts.cj.domain.User;
import com.cts.cj.dto.UserDto;
import com.cts.cj.service.LoginService;
import com.cts.cj.service.UserService;


@RestController
public class LoginController {

	public static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	LoginService loginservice;
	
	@Autowired
	UserService UserService;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
	@Value("${i2i.secure.context_path}")
	private String secureContextPath;
	
	
	@Value("${i2i.non_secure.context_path}")
	private String nonsecureContextPath;

	/**
	 * @param email
	 * @param password
	 * @return
	 */
	@PostMapping(path = "login/{email}/{password}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<User> findAll(@PathVariable String email, @PathVariable String password) {
		logger.debug("Validating the user from email and password");
		return new ResponseEntity<User>(loginservice.isUserExist(email, password), HttpStatus.OK);
	}

	@CrossOrigin("*")
	@PostMapping(path = "${i2i.secure.context_path}/users/validateUser/{email}")
	public ResponseEntity<UserDto> validateUser(@PathVariable String email) {
		
		return new ResponseEntity<UserDto>(UserService.findByEmail(email), HttpStatus.OK);
	}

	/**
	 * @param email
	 * @return
	 */
	@CrossOrigin("*")
	@PostMapping(path ="${i2i.secure.context_path}/admin/validateUser/{email}")
	public ResponseEntity<UserDto> validateAdminUser(@PathVariable String email) {
		return new ResponseEntity<UserDto>(UserService.findByEmail(email), HttpStatus.OK);
	}
	
	@CrossOrigin("*")
	@RequestMapping(value = "${i2i.non_secure.context_path}/cj/token", method = RequestMethod.POST)
	public ApiResponse<AuthToken> register(@RequestBody LoginUser loginUser) throws AuthenticationException {
		
		try {
		final Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		final String token = jwtTokenUtil.generateToken(authentication);
		if(token==null) {
			return new ApiResponse<AuthToken>(HttpStatus.BAD_REQUEST ,new AuthToken(null,loginUser.getUsername(),"Invalid Username/Password"));
		}
		UserDto user=UserService.findByEmail(loginUser.getUsername());
		
			/*
			 * if (user!=null) { user.setId(null); }
			 */
		return new ApiResponse<AuthToken>(HttpStatus.OK,"success",new AuthToken(token,loginUser.getUsername(),user));
		}catch(AuthenticationException ex) {
			return new ApiResponse<AuthToken>(HttpStatus.OK,new AuthToken(null, null,"Invalid Username/Password"));
		}
	}

	
}
