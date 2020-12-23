package com.cts.cj.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.cts.cj.exception.MeetingNotFoundException;
import com.cts.cj.exception.UserAlreadyExistsException;
import com.cts.cj.exception.UserNotFoundException;

@ControllerAdvice
@RestController
public class CJExceptionController extends ResponseEntityExceptionHandler{
	private static final Logger logger = LoggerFactory.getLogger(CJExceptionController.class);
		@ExceptionHandler(value = UserAlreadyExistsException.class)
		public ResponseEntity<?> exception(UserAlreadyExistsException exception) {
			logger.error("Error>>>"+exception.getMessage());
			exception.printStackTrace();
			return new ResponseEntity<>(exception.getMessage(),HttpStatus.NOT_FOUND);
		}

	@ExceptionHandler(value = UserNotFoundException.class)
	public ResponseEntity<?> exception(UserNotFoundException exception) {

		logger.error("Error>>>"+exception.getMessage());
		exception.printStackTrace();
		return new ResponseEntity<>(exception.getMessage(),HttpStatus.NOT_FOUND);

	}
	
	@ExceptionHandler(value = MeetingNotFoundException.class)
	public ResponseEntity<Object> exception(MeetingNotFoundException exception) {

		return new ResponseEntity<>("Meeting for this meeting ID does not exist.Please try with different meeting ID",
				HttpStatus.NOT_FOUND);

	}

}