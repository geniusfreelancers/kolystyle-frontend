package com.kolystyle.service;

import java.util.Set;

import com.kolystyle.domain.User;
import com.kolystyle.domain.UserBilling;
import com.kolystyle.domain.UserPayment;
import com.kolystyle.domain.UserShipping;
import com.kolystyle.domain.security.PasswordResetToken;
import com.kolystyle.domain.security.UserRole;

public interface UserService {
	PasswordResetToken getPasswordResetToken(final String token);
	
	void createPasswordResetTokenForUser(final User user, final String token);
	
	User findByUsername(String username);
	
	User findByEmail(String email);
	
	User findById(Long id);
	
	User createUser(User user, Set<UserRole> userRoles);
	
	User save(User user);
	
	void updateUserBilling(UserBilling userBilling, UserPayment userPayment, User user);
	
	void updateUserShipping(UserShipping userShipping, User user);
	
	void setUserDefaultPayment(Long userPaymentId, User user);
	
	void setUserDefaultShipping(Long userShippingId, User user);
	
	
	
}
