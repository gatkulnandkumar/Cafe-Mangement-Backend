	package com.inn.cafe.service;
	
	import java.util.List;
	import java.util.Map;
	
	import org.springframework.http.ResponseEntity;
	
	import com.inn.cafe.wrapper.UserWrapper;
	
	public interface UserService {
	
		ResponseEntity<String> signUp(Map<String, String> requestMap);
		
		ResponseEntity<String> login(Map<String, String> requestMap);
		
		ResponseEntity<List<UserWrapper>> getAllUsers();
		
		ResponseEntity<String> update(Map<String, String> requestMap);
		
		ResponseEntity<String> checkToken();
		
		ResponseEntity<String> changePassword(Map<String, String> requestMap);
		
		ResponseEntity<String> forgotPassword(Map<String, String> requestMap);
		
		boolean sendOtpByEmail(String email);
		
		ResponseEntity<String> resetPassword(Map<String, String> requestMap);
		
	
		
		
	}