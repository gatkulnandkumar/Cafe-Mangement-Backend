package com.inn.cafe.serviceImpl;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.inn.cafe.JWT.CustomerUsersDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.JWT.JwtUtil;
import com.inn.cafe.POJO.OTP;
import com.inn.cafe.POJO.User;
import com.inn.cafe.constants.CafeConstants;
import com.inn.cafe.dao.OTPDao;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.service.UserService;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.utils.EmailUtils;
import com.inn.cafe.wrapper.UserWrapper;
import org.apache.commons.lang3.StringUtils;

@Service
public class UserServiceImpl implements UserService{

	@Autowired
	UserDao userDao;
	@Autowired
	OTPDao otpDao;
	@Autowired
	AuthenticationManager authenticationManager;
	@Autowired
	CustomerUsersDetailsService customerUsersDetailsService;
	@Autowired
	JwtUtil jwtUtil;
	@Autowired
	JwtFilter jwtFilter;
	@Autowired
	EmailUtils emailUtils;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Value("${otp.expiration-time-ms}")
	private long otpExpirationTimeMs;
	@Value("${otp.cleanup.interval}")
	private long otpCleanupInterval;

	@Override
	public ResponseEntity<String> signUp(Map<String, String> requestMap) {
	try {
		if(validateSignUpMap(requestMap)) {
			User user = userDao.findByEmailId(requestMap.get("email"));
			if(Objects.isNull(user)) {
				User newUser = getUserFromMap(requestMap);
				newUser.setPassword(passwordEncoder.encode(newUser.getPassword())); // Hash the password
				userDao.save(newUser);
//				userDao.save(getUserFromMap(requestMap));
				return CafeUtils.getResponseEntity("Successfully Registered.", HttpStatus.OK);
			}
			else {
				return CafeUtils.getResponseEntity("Email already exists.", HttpStatus.BAD_REQUEST);
			}
		}
		else {
			return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
		}
	}catch(Exception e)
	{
		e.printStackTrace();
	}
	return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

//	private boolean validateSignUpMap(Map<String, String> requestMap) {
//		if(requestMap.containsKey("name") && requestMap.containsKey("contactNumber") && requestMap.containsKey("email")
//				&& requestMap.containsKey("password")) {
//			return true;
//		}
//		return false;
//	}
	private boolean validateSignUpMap(Map<String, String> requestMap) {
	    if (requestMap.containsKey("name") && requestMap.containsKey("contactNumber") &&
	        requestMap.containsKey("email") && requestMap.containsKey("password")) {
	        
	        String password = requestMap.get("password");
	        
	        // Add password validation logic here
	        boolean isValidPassword = validatePasswordPattern(password);
	        
	        return isValidPassword;
	    }
	    return false;
	}
	private boolean validatePasswordPattern(String password) {
	    // Password should be at least 8 characters long and have a combination of alphabets, special characters, and numbers
	    String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";
	    return password.matches(passwordPattern);
	}


	private User getUserFromMap(Map<String, String> requestMap) {
		User user = new User();
		user.setName(requestMap.get("name"));
		user.setContactNumber(requestMap.get("contactNumber"));
		user.setEmail(requestMap.get("email"));
		user.setPassword(requestMap.get("password"));
		user.setStatus("false");
		user.setRole("user");
		return user;
	}

	@Override
	public ResponseEntity<String> login(Map<String, String> requestMap) {
	 
		try {
			Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(requestMap.get("email"),
					requestMap.get("password")));
			if(auth.isAuthenticated()) {
				if(customerUsersDetailsService.getUserDetail().getStatus().equalsIgnoreCase("true")) {
					return new ResponseEntity<String>("{\"token\":\""+
							jwtUtil.generateToken(customerUsersDetailsService.getUserDetail().getEmail(), 
									customerUsersDetailsService.getUserDetail().getRole())+"\"}",HttpStatus.OK);
				}
				else {
					return new ResponseEntity<String>("{\"message\":\""+"Wait for admin approval."+"\"}",HttpStatus.BAD_REQUEST);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<String>("{\"message\":\""+"Bad Credentials."+"\"}",HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<List<UserWrapper>> getAllUsers() {
		try {
			if(jwtFilter.isAdmin()) {
				return new ResponseEntity<>(userDao.getAllUsers(), HttpStatus.OK);
			}else {
				return new ResponseEntity<>(new ArrayList<>(),HttpStatus.UNAUTHORIZED);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<String> update(Map<String, String> requestMap) {
		try {
			if(jwtFilter.isAdmin()) {
				Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));
				if(optional.isPresent()) {
					userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
					sendMailToAllAdmin(requestMap.get("status"), optional.get().getEmail(),userDao.getAllAdmin());
					return CafeUtils.getResponseEntity("User status updated Succesfully.",HttpStatus.OK);
				} else {
					return CafeUtils.getResponseEntity("User id does not exist",HttpStatus.OK);
				}
			} else {
				return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
		allAdmin.remove(jwtFilter.getCurrentUser());
		if(status !=null && status.equalsIgnoreCase("true")) {
			emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(),"Account Approved","USER:- "+user+"\n is approved by \nADMIN:-" +jwtFilter.getCurrentUser(),allAdmin);
		}else {
			emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(),"Account disabled","USER:- "+user+"\n is disabled by \nADMIN:- " +jwtFilter.getCurrentUser(),allAdmin);
		}
	}

	@Override
	public ResponseEntity<String> checkToken() {
	   
		return CafeUtils.getResponseEntity("true", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
		try {
			User userObj = userDao.findByEmail(jwtFilter.getCurrentUser());
			if(!userObj.equals(null)) {
				if(userObj.getPassword().equals(requestMap.get("oldPassword"))) {
					userObj.setPassword(requestMap.get("newPassword"));
					userDao.save(userObj);
					return CafeUtils.getResponseEntity("Password updated Successfully.", HttpStatus.OK);
				}
				return CafeUtils.getResponseEntity("Incorrect old Password",HttpStatus.BAD_REQUEST);
			}
			return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

//	@Override
//	public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
//	    try {
//	        // Call the sendOtpByEmail method instead of this.forgotPassword
//	        boolean otpSent = sendOtpByEmail(requestMap.get("email"));
//	        
//	        if (otpSent) {
//	            return ResponseEntity.ok("{\"message\":\"OTP sent to email\"}");
//	        } else {
//	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"Invalid email\"}");
//	        }
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
//	    }
//	}

//	@Override
//	public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
//	    try {
//	        String email = requestMap.get("email");
//	        
//	        User user = userDao.findByEmail(email);
//	        if (user != null) {
//	            boolean otpSent = sendOtpByEmail(email);
//	            
//	            if (otpSent) {
//	                return ResponseEntity.ok("{\"message\":\"OTP sent to email\"}");
//	            } else {
//	                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"message\":\"Error sending OTP\"}");
//	            }
//	        } else {
//	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"Invalid user\"}");
//	        }
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
//	    }
//	}
	@Override
	public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
	    try {
	        String email = requestMap.get("email");
	        
	        User user = userDao.findByEmail(email);
	        if (user != null) {
	            boolean otpSent = sendOtpByEmail(email);
	            
	            if (otpSent) {
	                OTP otpEntry = otpDao.findByEmail(email);
	                
	                if (otpEntry != null) {
	                    // Calculate remaining time for OTP expiration
	                    Duration remainingTime = Duration.between(LocalDateTime.now(), otpEntry.getExpireIn());
	                    
	                    return ResponseEntity.ok("{\"message\":\"OTP sent to email\",\"otpExpireIn\":\"" + remainingTime.getSeconds() + "\"}");
	                } else {
	                    return ResponseEntity.ok("{\"message\":\"OTP sent to email\"}");
	                }
	            } else {
	                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"message\":\"Error sending OTP\"}");
	            }
	        } else {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"Invalid user\"}");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}


	  
	@Override
	public boolean sendOtpByEmail(String email) {
	    OTP otpEntry = otpDao.findByEmail(email);
	    
	    if (otpEntry == null) {
	        otpEntry = new OTP();
	        otpEntry.setEmail(email);
	    }

	    int otp = generateRandomOtp();
	    otpEntry.setOtp(otp);
	    otpEntry.setActive(true);
	    otpEntry.setExpireIn(LocalDateTime.now().plusMinutes(3)); // Expiry time 3 minutes from now
	    otpDao.save(otpEntry);

	    // Send the OTP via email
	    sendOtpViaEmail(email, otp);
	    return true;
	}

	@Async
	  private void sendOtpViaEmail(String email, int otp) {
		    try {
		        emailUtils.sendOtpMail(email, "OTP for Password Reset", String.valueOf(otp));
		    } catch (MessagingException e) {
		        e.printStackTrace();
		    }
		}


//	  @Override
//	  public ResponseEntity<String> resetPassword(Map<String, String> requestMap) {
//	      try {
//	          String email = requestMap.get("email");
//	          String enteredOtp = requestMap.get("otp");
//	          String newPassword = requestMap.get("newPassword");
//
//	          OTP otpEntry = otpDao.findByEmail(email);
//
//	          if (otpEntry != null && otpEntry.isActive()) {
//	              int storedOtp = otpEntry.getOtp();
//	              LocalDateTime otpExpireTime = otpEntry.getExpireIn();
//
//	              if (enteredOtp.equals(String.valueOf(storedOtp)) && LocalDateTime.now().isBefore(otpExpireTime)) {
//	                  User user = userDao.findByEmail(email);
//	                  if (user != null) {
//	                      // Encrypt the new password before saving
//	                      String encryptedPassword = passwordEncoder.encode(newPassword);
//	                      user.setPassword(encryptedPassword);
//	                      userDao.save(user);
//
//	                      otpEntry.setActive(false); // Expire the OTP after successful password reset
//	                      otpDao.save(otpEntry);
//
//	                      return ResponseEntity.ok("{\"message\":\"Password reset successful\"}");
//	                  }
//	              }
//	          }
//
//	          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"Invalid OTP or expired OTP\"}");
//	      } catch (Exception e) {
//	          e.printStackTrace();
//	          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//	      }
//	  }
	@Override
	public ResponseEntity<String> resetPassword(Map<String, String> requestMap) {
	    try {
	        String email = requestMap.get("email");
	        String enteredOtp = requestMap.get("otp");
	        String newPassword = requestMap.get("newPassword");

	        OTP otpEntry = otpDao.findByEmail(email);

	        if (otpEntry != null && otpEntry.isActive()) {
	            int storedOtp = otpEntry.getOtp();
	            LocalDateTime otpExpireTime = otpEntry.getExpireIn();

	            if (enteredOtp.equals(String.valueOf(storedOtp)) && LocalDateTime.now().isBefore(otpExpireTime)) {
	                User user = userDao.findByEmail(email);
	                if (user != null) {
	                    // Validate the new password before updating
	                    boolean isValidPassword = validatePasswordPattern(newPassword);

	                    if (isValidPassword) {
	                        // Encrypt the new password before saving
	                        String encryptedPassword = passwordEncoder.encode(newPassword);
	                        user.setPassword(encryptedPassword);
	                        userDao.save(user);

	                        otpEntry.setActive(false); // Expire the OTP after successful password reset
	                        otpDao.save(otpEntry);

	                        return ResponseEntity.ok("{\"message\":\"Password reset successful\"}");
	                    } else {
	                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"Invalid new password format\"}");
	                    }
	                }
	            }
	        }

	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"Invalid OTP or expired OTP\"}");
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}



	private int generateRandomOtp() {
	    int otpLength = 6;
	    int otpValue = 0;
	    Random random = new Random();

	    for (int i = 0; i < otpLength; i++) {
	        otpValue = otpValue * 10 + random.nextInt(10); 
	    }

	    return otpValue;
	}

}