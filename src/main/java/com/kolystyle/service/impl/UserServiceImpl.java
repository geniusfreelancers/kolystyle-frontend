package com.kolystyle.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.domain.User;
import com.kolystyle.domain.UserBilling;
import com.kolystyle.domain.UserPayment;
import com.kolystyle.domain.UserShipping;
import com.kolystyle.domain.WishList;
import com.kolystyle.domain.security.PasswordResetToken;
import com.kolystyle.domain.security.UserRole;
import com.kolystyle.repository.PasswordResetTokenRepository;
import com.kolystyle.repository.RoleRepository;
import com.kolystyle.repository.UserPaymentRepository;
import com.kolystyle.repository.UserRepository;
import com.kolystyle.repository.UserShippingRepository;
import com.kolystyle.service.UserService;

@Service
public class UserServiceImpl implements UserService {
	
	private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	private PasswordResetTokenRepository passwordResetTokenRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private UserPaymentRepository userPaymentRepository;
	
	@Autowired
	private UserShippingRepository userShippingRepository;

	@Override
	public PasswordResetToken getPasswordResetToken(final String token){
		return passwordResetTokenRepository.findByToken(token);
	}
	
	@Override
	public void createPasswordResetTokenForUser(final User user, final String token){
		final PasswordResetToken myToken = new PasswordResetToken(token, user);
		passwordResetTokenRepository.save(myToken);
	}
	
	@Override
	public User findByUsername(String username){
		return userRepository.findByUsername(username);
	}
	
	@Override
	public User findById(Long id){
		return userRepository.findOne(id);
	}
	
	@Override
	public User findByEmail(String email){
		return userRepository.findByEmail(email);
	}
	
	@Override
	@Transactional
	public User createUser(User user, Set<UserRole> userRoles){
		User localUser = userRepository.findByUsername(user.getUsername());
		
		if(localUser != null){
			LOG.info("user {} already exists. Nothing will be done", user.getUsername());
		}else{
			for(UserRole ur : userRoles){
				roleRepository.save(ur.getRole());
			}
			
			user.getUserRoles().addAll(userRoles);
			ShoppingCart shoppingCart = new ShoppingCart();
			shoppingCart.setUser(user);
			//Set Bag Id
			//To generate random number 99 is max and 10 is min
			Random rand = new Random();
			int  newrandom = rand.nextInt(99) + 10;
			
			/*Time Stamp and Random Number for Bag Id so we can always
			  have unique bag id within Guest Cart*/
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			
			String bagId = newrandom+"KF"+timestamp.getTime();
			shoppingCart.setBagId(bagId);
			shoppingCart.setCartType("customer");
			user.setShoppingCart(shoppingCart);
			
			//Wish List
			WishList wishList = new WishList();
			wishList.setUser(user);
			user.setWishList(wishList);
			
			user.setUserShippingList(new ArrayList<UserShipping>());
			user.setUserPaymentList(new ArrayList<UserPayment>());
			
			
			localUser = userRepository.save(user);
		}
		return localUser;
	}
	
	@Override
	public User save(User user){
		return userRepository.save(user);
	}
	

	@Override
	public void updateUserBilling(UserBilling userBilling, UserPayment userPayment, User user){
		userPayment.setUser(user);
		userPayment.setUserBilling(userBilling);
		userPayment.setCardLastNumber(userPayment.getCardNumber().substring(12));
		userPayment.setDefaultPayment(true);
		userBilling.setUserPayment(userPayment);
		user.getUserPaymentList().add(userPayment);
		save(user);
		
	}
	
	@Override
	public void updateUserShipping(UserShipping userShipping, User user){
		userShipping.setUser(user);
		userShipping.setUserShippingDefault(true);
		user.getUserShippingList().add(userShipping);
		save(user);
		
	}
	
	@Override
	public void setUserDefaultPayment(Long userPaymentId, User user){
		List<UserPayment> userPaymentList = (List<UserPayment>) userPaymentRepository.findAll();
		
		for(UserPayment userPayment : userPaymentList){
			if(userPayment.getId() == userPaymentId){
				userPayment.setDefaultPayment(true);
				userPaymentRepository.save(userPayment);
			}else{
				userPayment.setDefaultPayment(false);
				userPaymentRepository.save(userPayment);
			}
		}
	}

	@Override
	public void setUserDefaultShipping(Long userShippingId, User user){
		List<UserShipping> userShippingList = (List<UserShipping>) userShippingRepository.findByUserId(user.getId());
		
		for(UserShipping userShipping : userShippingList){
			if(userShipping.getId() == userShippingId){
				userShipping.setUserShippingDefault(true);
				userShippingRepository.save(userShipping);
			}else{
				userShipping.setUserShippingDefault(false);
				userShippingRepository.save(userShipping);
			}
		}
	}
	
	
	
	
}
