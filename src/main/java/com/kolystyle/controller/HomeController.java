package com.kolystyle.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.kolystyle.domain.CartItem;
import com.kolystyle.domain.Category;
import com.kolystyle.domain.Order;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.domain.User;
import com.kolystyle.domain.UserBilling;
import com.kolystyle.domain.UserPayment;
import com.kolystyle.domain.UserShipping;
import com.kolystyle.domain.security.PasswordResetToken;
import com.kolystyle.domain.security.Role;
import com.kolystyle.domain.security.UserRole;
import com.kolystyle.repository.CartItemRepository;
import com.kolystyle.service.CartItemService;
import com.kolystyle.service.OrderService;
import com.kolystyle.service.ProductService;
import com.kolystyle.service.ShoppingCartService;
import com.kolystyle.service.UserPaymentService;
import com.kolystyle.service.UserService;
import com.kolystyle.service.UserShippingService;
import com.kolystyle.service.impl.UserSecurityService;
import com.kolystyle.utility.MailConstructor;
import com.kolystyle.utility.SecurityUtility;
import com.kolystyle.utility.USConstants;

@Controller
public class HomeController {

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private MailConstructor mailConstructor;

	@Autowired
	private UserService userService;

	@Autowired
	private UserSecurityService userSecurityService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private UserPaymentService userPaymentService;

	@Autowired
	private UserShippingService userShippingService;
	
	@Autowired
	private CartItemService cartItemService;
	
	@Autowired
	private ShoppingCartService shoppingCartService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private CartItemRepository cartItemRepository;
	
	@RequestMapping("/thankyou")
	public String thankyou() {
		return "thankyou";
	}
	
	@RequestMapping("/index")
	public String index() {
		return "index";
	}
	
	 @RequestMapping("/")
	    public String home(Model model){
	        List<Product> productList = productService.findAll();
	        model.addAttribute("productList",productList);
	      //  model.addAttribute("searchCondition", searchCondition);
	        
	        
	        return "home";
	    }

	@RequestMapping("/userlogin")
	public String login(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "logout",
            required = false) String logout,Model model,HttpServletRequest request) {
		HttpSession session = request.getSession();
        String sessionID = session.getId();
        model.addAttribute("sessionID",sessionID);
		if(error != null){
            model.addAttribute("invalid",true);
        }

        if(logout != null){
            model.addAttribute("logout",true);
        }
        if(request.getHeader("referer")!= null) {
        String referrer = request.getHeader("referer");
        String newref = referrer.substring(referrer.length()-17,referrer.length());
		if(newref.equalsIgnoreCase("shoppingCart/cart")){
        	model.addAttribute("guestOption",true); 
        	return "myAccount";
        }
        }
		return "myAccount";
	}
	
	
	@RequestMapping("/login")
	public String loginPost(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "logout",
            required = false) String logout,Model model, @ModelAttribute("username") String username) {
		if(error != null){
            model.addAttribute("invalid",true);
            return "myAccount";
        }

        if(logout != null){
            model.addAttribute("logout",true);
            return "myAccount";
        }
      
		UserDetails userDetails = userSecurityService.loadUserByUsername(username);
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
				userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		User user = userService.findByUsername(username);

		model.addAttribute("user", user);
		return "account";	
	}
	

	
	@RequestMapping("/register")
	public String register(HttpServletRequest request, Model model) {
		HttpSession session = request.getSession();
        String sessionID = session.getId();
        User user = new User();
        model.addAttribute("sessionID",sessionID);
        model.addAttribute("user",user);
		return "register";
	}
	

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public String registerPost(HttpServletRequest request,
			@ModelAttribute("user") User user,
			Model model, HttpServletResponse response) throws Exception {

		if (userService.findByUsername(user.getUsername()) != null) {
			model.addAttribute("usernameExists", true);
			return "register";
		}
		if (userService.findByEmail(user.getEmail()) != null) {
			model.addAttribute("emailExists", true);
			return "register";
		}

		//This can be used to randomly generate password
		//String password = SecurityUtility.randomPassword();
		String userPassword = user.getPassword();
		String encryptedPassword = SecurityUtility.passwordEncoder().encode(userPassword);
		user.setPassword(encryptedPassword);

		Role role = new Role();
		role.setRoleId(1);
		role.setName("ROLE_USER");
		Set<UserRole> userRoles = new HashSet<>();
		userRoles.add(new UserRole(user, role));
		userService.createUser(user, userRoles);

		//Getting User info
		Cookie[] cookies = request.getCookies();
		String cartId = "";
		boolean foundCookie = false;
   	 	//Check cookie value
        for(int i = 0; i < cookies.length; i++) { 
            Cookie cartID = cookies[i];
            if (cartID.getName().equals("BagId")) {
            	cartId = cartID.getValue();
                System.out.println("BagId = " + cartId);
                foundCookie = true;
                cartID.setPath("/");
                cartID.setMaxAge(0);
         		response.addCookie(cartID);
                ShoppingCart guestShoppingCart = shoppingCartService.findCartByBagId(cartId);
                List<CartItem> cartItemList = cartItemService.findByShoppingCart(guestShoppingCart);
                
                for(CartItem cartItem : cartItemList) {
                 	cartItem.setShoppingCart(user.getShoppingCart());
                	cartItemRepository.save(cartItem);
                }
                
                user.getShoppingCart().setGrandTotal(guestShoppingCart.getGrandTotal());
                user.getShoppingCart().setPromoCode(guestShoppingCart.getPromoCode());
                user.getShoppingCart().setDiscountedAmount(guestShoppingCart.getDiscountedAmount());
                shoppingCartService.remove(guestShoppingCart);
            }
        }
		String token = UUID.randomUUID().toString();
		userService.createPasswordResetTokenForUser(user, token);

		String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();

		SimpleMailMessage email = mailConstructor.constructResetTokenEmail(appUrl, request.getLocale(), token, user,
				userPassword);
	//Uncomment to enable to send email to user as welcome email
	//	mailSender.send(email);
		model.addAttribute("emailSent", "true");
	//	model.addAttribute("orderList", user.getOrderList());
		
		return "myAccount";
	}
	
	
	
	@RequestMapping("/verifyregister")
	public String verifyregister(Locale locale, @RequestParam("token") String token, Model model) {
		PasswordResetToken passToken = userService.getPasswordResetToken(token);
		if (passToken == null) {
			String message = "Invalid Token.";
			model.addAttribute("message", message);
			return "redirect:/badRequest";
		}

		User user = passToken.getUser();
		String username = user.getUsername();
		UserDetails userDetails = userSecurityService.loadUserByUsername(username);
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
				userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		model.addAttribute("user", user);
		model.addAttribute("classActiveEdit", true);
		
		return "myProfile";
	}
	
	
	
	@RequestMapping("/productshelf")
	public String productshelf(Model model, Principal principal, Pageable page) {
		if(principal != null){
			String username = principal.getName();
			User user = userService.findByUsername(username);
			model.addAttribute("user", user);
		}
		
		List<Product> productList = productService.findAll();
		model.addAttribute("productList",productList);
		model.addAttribute("activeAll",true);
		
		return "productshelf";
	}
	
	@RequestMapping("/productDetail")
	public String productDetail(@PathParam("id") Long id, Model model, Principal principal) {
		if(principal != null){
			String username = principal.getName();
			User user = userService.findByUsername(username);
			model.addAttribute("user", user);
		}
		
		Product product = productService.findOne(id);
		String availableSize = product.getSize();
		List<String> sizeList = Arrays.asList(availableSize.split("\\s*,\\s*"));
		
		model.addAttribute("product", product);
		
		//List<Integer> qtyList = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
		int qtly = product.getInStockNumber();
		List<Integer> qtyList = new ArrayList<Integer>();
		for (int i=1; i<=qtly;i++) {
			qtyList.add(i);
		}
		Category category = product.getCategory();
		List<Product> productList = productService.findTop12ByCategory(category);
		
		
		//List<Product> productList = productService.findByCategory(category);
		model.addAttribute("productList", productList);
		System.out.println(productList);
		model.addAttribute("sizeList", sizeList);
		model.addAttribute("qtyList", qtyList);
		model.addAttribute("qty", 1);
		
		
		return "productDetail";
	}
	
	@RequestMapping("/forgotpassword")
	public String forgotpassword() {
		return "forgotpassword";
	}
	
	@RequestMapping("/forgetPassword")
	public String forgetPassword(HttpServletRequest request, @ModelAttribute("email") String email, Model model) {
		model.addAttribute("classActiveForgetPassword", true);
		User user = userService.findByEmail(email);
		if (user == null) {
			model.addAttribute("emailNotExist", true);
			return "forgotpassword";
		}
		String password = SecurityUtility.randomPassword();

		String encryptedPassword = SecurityUtility.passwordEncoder().encode(password);
		user.setPassword(encryptedPassword);
		userService.save(user);

		String token = UUID.randomUUID().toString();
		userService.createPasswordResetTokenForUser(user, token);

		String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();

		SimpleMailMessage newEmail = mailConstructor.constructResetTokenEmail(appUrl, request.getLocale(), token, user,
				password);
	//Uncomment to send email to user for password reset
		//	mailSender.send(newEmail);
		model.addAttribute("forgetPasswordEmailSent", "true");
		return "forgotpassword";
	}
	
/*	@RequestMapping("/myProfile")
	public String myProfile(Model model, Principal principal){
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user", user);
		model.addAttribute("userPaymentList", user.getUserPaymentList());
		model.addAttribute("userShippingList", user.getUserShippingList());
		model.addAttribute("orderList", user.getOrderList());
		
		UserShipping userShipping = new UserShipping();
		model.addAttribute("userShipping", userShipping);
		
		
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("listOfShippingAddresses", true);
		
		List<String> stateList = USConstants.listOfUSStatesCode;
		Collections.sort(stateList);
		model.addAttribute("stateList", stateList);
		model.addAttribute("classActiveEdit", true);
		
		return "myProfile";		
	}*/
	
	/*@RequestMapping(value = "/updateUserInfo", method= RequestMethod.POST)
	public String updateUserInfo(
			@ModelAttribute("user") User user,
			@ModelAttribute("newPassword") String newPassword,
			Model model
			) throws Exception{
		User currentUser = userService.findById(user.getId());
		
		if(currentUser == null){
			throw new Exception("User not found");
		}
		
		check email already exist
		if(userService.findByEmail(user.getEmail()) != null){
			if(userService.findByEmail(user.getEmail()).getId() != currentUser.getId()){
				model.addAttribute("emailExists", true);
				return "myProfile";
			}
		}
		
		check username already exist
		if(userService.findByUsername(user.getUsername()) != null){
			if(userService.findByUsername(user.getUsername()).getId() != currentUser.getId()){
				model.addAttribute("usernameExists", true);
				return "myProfile";
			}
		}
		
		//Update Password
		if(newPassword != null && !newPassword.isEmpty() && !newPassword.equals("")){
			BCryptPasswordEncoder passwordEncoder = SecurityUtility.passwordEncoder();
			String dbPassword = currentUser.getPassword();
			if(passwordEncoder.matches(user.getPassword(), dbPassword)){
				currentUser.setPassword(passwordEncoder.encode(newPassword));
			}else{
				model.addAttribute("incorrectPassword", true);
				return "myProfile";
			}
		}
		
		currentUser.setFirstName(user.getFirstName());
		currentUser.setLastName(user.getLastName());
		currentUser.setUsername(user.getUsername());
		currentUser.setEmail(user.getEmail());
		
		userService.save(currentUser);
		
		model.addAttribute("updateSuccess", true);
		model.addAttribute("user", currentUser);
		model.addAttribute("classActiveEdit", true);
		
		model.addAttribute("listOfShippingAddresses", true);
		model.addAttribute("listOfCreditCards", true);
			
		
		UserDetails userDetails = userSecurityService.loadUserByUsername(currentUser.getUsername());
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
				userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		return "myProfile";
	}*/
	
	@RequestMapping("/listOfCreditCards")
	public String listOfCreditCards(Model model, Principal principal, HttpServletRequest request){
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user",user);
		model.addAttribute("userPaymentList", user.getUserPaymentList());
		model.addAttribute("userShippingList", user.getUserShippingList());
		model.addAttribute("orderList", user.getOrderList());
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("classActiveBilling", true);
		model.addAttribute("listOfShippingAddresses", true);
		
		return "myProfile";
	}

	@RequestMapping("/listOfShippingAddresses")
	public String listOfShippingAddresses(Model model, Principal principal, HttpServletRequest request){
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user",user);
		model.addAttribute("userPaymentList", user.getUserPaymentList());
		model.addAttribute("userShippingList", user.getUserShippingList());
		model.addAttribute("orderList", user.getOrderList());
		
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("classActiveShipping", true);
		model.addAttribute("listOfShippingAddresses", true);
		
		return "myProfile";
	}
	
	@RequestMapping("/addNewCreditCard")
	public String addNewCreditCard(Model model, Principal principal, HttpServletRequest request){
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user",user);
		
		model.addAttribute("addNewCreditCard", true);
		model.addAttribute("classActiveBilling", true);
		model.addAttribute("listOfShippingAddresses", true);
		
		UserBilling userBilling = new UserBilling();
		UserPayment userPayment = new UserPayment();
		
		model.addAttribute("userBilling", userBilling);
		model.addAttribute("userPayment", userPayment);
		
		List<String> stateList = USConstants.listOfUSStatesCode;
		Collections.sort(stateList);
		model.addAttribute("stateList",stateList);
		model.addAttribute("userPaymentList", user.getUserPaymentList());
		model.addAttribute("userShippinglist", user.getUserShippingList());
		model.addAttribute("orderList", user.getOrderList());
		
		return "myProfile";
	}
	
	@RequestMapping(value="/addNewCreditCard", method=RequestMethod.POST)
	public String addNewCreditCard(
			@ModelAttribute("userPayment") UserPayment userPayment,
			@ModelAttribute("userBilling") UserBilling userBilling,
			Principal principal, Model model){
		User user = userService.findByUsername(principal.getName());
		userService.updateUserBilling(userBilling, userPayment, user);
		
		model.addAttribute("user", user);
		model.addAttribute("userPaymentList", user.getUserPaymentList());
		model.addAttribute("userShippingList", user.getUserShippingList());
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("classActiveBilling", true);
		model.addAttribute("listOfShippingAddresses", true);
		model.addAttribute("orderList", user.getOrderList());
		
		return "myProfile";
	}
	
	/*@RequestMapping("/addNewShippingAddress")
	public String addNewShippingAddress(Model model, Principal principal){
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user",user);
		
		model.addAttribute("addNewShippingAddress", true);
		model.addAttribute("classActiveShipping", true);
		model.addAttribute("listOfCreditCards", true);
		
		UserShipping userShipping = new UserShipping();
		
		model.addAttribute("userShipping", userShipping);
		
		List<String> stateList = USConstants.listOfUSStatesCode;
		Collections.sort(stateList);
		model.addAttribute("stateList",stateList);
		model.addAttribute("userPaymentList", user.getUserPaymentList());
		model.addAttribute("userShippinglist", user.getUserShippingList());
		model.addAttribute("orderList", user.getOrderList());
		
		return "myProfile";
	}
	
	@RequestMapping(value="/addNewShippingAddress", method=RequestMethod.POST)
	public String addNewShippingAddressPost(
			@ModelAttribute("userShipping") UserShipping userShipping,
			Principal principal, Model model
			){
		User user = userService.findByUsername(principal.getName());
		userService.updateUserShipping(userShipping, user);
		
		model.addAttribute("user", user);
		model.addAttribute("userPaymentList", user.getUserPaymentList());
		model.addAttribute("userShippingList", user.getUserShippingList());
		model.addAttribute("listOfShippingAddresses", true);
		model.addAttribute("classActiveShipping", true);
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("orderList", user.getOrderList());
		
		return "myProfile";
	}*/
	
	@RequestMapping("/updateCreditCard")
	public String updateCreditCard(@ModelAttribute("id") Long creditCardId, Principal principal, Model model){
		User user = userService.findByUsername(principal.getName());
		UserPayment userPayment = userPaymentService.findById(creditCardId);
		
		if(user.getId() != userPayment.getUser().getId()){
			return "badRequestPage";
		}else{
			model.addAttribute("user", user);
			UserBilling userBilling = userPayment.getUserBilling();
			model.addAttribute("userPayment",userPayment);
			model.addAttribute("userBilling",userBilling);
			
			List<String> stateList = USConstants.listOfUSStatesCode;
			Collections.sort(stateList);
			model.addAttribute("stateList",stateList);
			
			model.addAttribute("addNewCreditCard", true);
			model.addAttribute("classActiveBilling", true);
			model.addAttribute("listOfShippingAddresses", true);
			
			model.addAttribute("userPaymentList", user.getUserPaymentList());
			model.addAttribute("userShippinglist", user.getUserShippingList());
			model.addAttribute("orderList", user.getOrderList());
			
			return "myProfile";
		}
	}
	
/*	@RequestMapping("/updateUserShipping")
	public String updateUserShipping(@ModelAttribute("id") Long shippingAddressId, Principal principal, Model model){
		User user = userService.findByUsername(principal.getName());
		UserShipping userShipping = userShippingService.findById(shippingAddressId);
		
		if(user.getId() != userShipping.getUser().getId()){
			return "badRequestPage";
		}else{
			model.addAttribute("user", user);
			
			model.addAttribute("userShipping",userShipping);
			
			List<String> stateList = USConstants.listOfUSStatesCode;
			Collections.sort(stateList);
			model.addAttribute("stateList",stateList);
			
			model.addAttribute("addNewShippingAddress", true);
			model.addAttribute("classActiveShipping", true);
			model.addAttribute("listOfCreditCards", true);
			
			model.addAttribute("userPaymentList", user.getUserPaymentList());
			model.addAttribute("userShippinglist", user.getUserShippingList());
			model.addAttribute("orderList", user.getOrderList());
			
			return "myProfile";
		}
	}
*/
	
	@RequestMapping(value="/setDefaultPayment", method=RequestMethod.POST)
	public String setDefaultPayment(@ModelAttribute("defaultUserPaymentId") Long defaultPaymentId, Principal principal, Model model){
		User user = userService.findByUsername(principal.getName());
		userService.setUserDefaultPayment(defaultPaymentId, user);
		
		model.addAttribute("user", user);
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("classActiveBilling", true);
		model.addAttribute("listOfShippingAddresses", true);
		
		model.addAttribute("userPaymentList", user.getUserPaymentList());
		model.addAttribute("userShippinglist", user.getUserShippingList());
		model.addAttribute("orderList", user.getOrderList());
		
		return "myProfile";
	}
	
/*	@RequestMapping(value="/setDefaultShippingAddress", method=RequestMethod.POST)
	public String setDefaultShippingAddress(@ModelAttribute("defaultShippingAddressId") Long defaultShippingId, Principal principal, Model model){
		User user = userService.findByUsername(principal.getName());
		userService.setUserDefaultShipping(defaultShippingId, user);
		
		model.addAttribute("user", user);
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("classActiveShipping", true);
		model.addAttribute("listOfShippingAddresses", true);
		€
		model.addAttribute("userPaymentList", user.getUserPaymentList());
		model.addAttribute("userShippinglist", user.getUserShippingList());
		model.addAttribute("orderList", user.getOrderList());
		
		return "myProfile";
	}*/
	
	@RequestMapping("/removeCreditCard")
	public String removeCreditCard(@ModelAttribute("id") Long creditCardId, Principal principal, Model model){
		User user = userService.findByUsername(principal.getName());
		UserPayment userPayment = userPaymentService.findById(creditCardId);
		
		if(user.getId() != userPayment.getUser().getId()){
			return "badRequestPage";
		}else{
			model.addAttribute("user", user);
			userPaymentService.removeById(creditCardId);
			
			
			model.addAttribute("listOfCreditCards", true);
			model.addAttribute("classActiveBilling", true);
			model.addAttribute("listOfShippingAddresses", true);
			
			model.addAttribute("userPaymentList", user.getUserPaymentList());
			model.addAttribute("userShippinglist", user.getUserShippingList());
			model.addAttribute("orderList", user.getOrderList());
			
			return "myProfile";
		}
	}
	

	@RequestMapping("/removeUserShipping")
	public String removeUserShipping(@ModelAttribute("id") Long userShippingId, Principal principal, Model model){
		User user = userService.findByUsername(principal.getName());
		UserShipping userShipping = userShippingService.findById(userShippingId);
		
		if(user.getId() != userShipping.getUser().getId()){
			return "badRequestPage";
		}else{
			model.addAttribute("user", user);
			
			userShippingService.removeById(userShippingId);
			
			
			model.addAttribute("listOfShippingAddresses", true);
			model.addAttribute("classActiveShipping", true);
			
			model.addAttribute("userPaymentList", user.getUserPaymentList());
			model.addAttribute("userShippinglist", user.getUserShippingList());
			model.addAttribute("orderList", user.getOrderList());
			
			return "myProfile";
		}
	}
	
	@RequestMapping("/newUser")
	public String newUser(Locale locale, @RequestParam("token") String token, Model model) {
		PasswordResetToken passToken = userService.getPasswordResetToken(token);
		if (passToken == null) {
			String message = "Invalid Token.";
			model.addAttribute("message", message);
			return "redirect:/badRequest";
		}

		User user = passToken.getUser();
		String username = user.getUsername();
		UserDetails userDetails = userSecurityService.loadUserByUsername(username);
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
				userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		model.addAttribute("user", user);
		model.addAttribute("classActiveEdit", true);
		
		return "myProfile";
	}
	
	@RequestMapping(value = "/newUser", method = RequestMethod.POST)
	public String newUserPost(HttpServletRequest request, @ModelAttribute("email") String userEmail,
			@ModelAttribute("username") String username, Model model) throws Exception {
		model.addAttribute("classActiveNewAccount", true);
		model.addAttribute("email", userEmail);
		model.addAttribute("username", username);

		if (userService.findByUsername(username) != null) {
			model.addAttribute("usernameExists", true);
			return "myAccount";
		}
		if (userService.findByEmail(userEmail) != null) {
			model.addAttribute("emailExists", true);
			return "myAccount";
		}

		User user = new User();
		user.setUsername(username);
		user.setEmail(userEmail);

		String password = SecurityUtility.randomPassword();

		String encryptedPassword = SecurityUtility.passwordEncoder().encode(password);
		user.setPassword(encryptedPassword);

		Role role = new Role();
		role.setRoleId(1);
		role.setName("ROLE_USER");
		Set<UserRole> userRoles = new HashSet<>();
		userRoles.add(new UserRole(user, role));
		userService.createUser(user, userRoles);

		String token = UUID.randomUUID().toString();
		userService.createPasswordResetTokenForUser(user, token);

		String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();

		SimpleMailMessage email = mailConstructor.constructResetTokenEmail(appUrl, request.getLocale(), token, user,
				password);
		mailSender.send(email);
		model.addAttribute("emailSent", "true");
		model.addAttribute("orderList", user.getOrderList());
		
		return "myAccount";
	}


	
	@RequestMapping("/orderDetail")
	public String orderDetail(
			@RequestParam("id") Long orderId,
			Principal principal, Model model
			){
		User user = userService.findByUsername(principal.getName());
		Order order = orderService.findOne(orderId);
		
		if(order.getUser().getId() != user.getId()){
			return "badRequestPage";
		}else{
			List<CartItem> cartItemList = cartItemService.findByOrder(order);
			model.addAttribute("cartItemList",cartItemList);
			model.addAttribute("user", user);
			model.addAttribute("order",order);
			
			model.addAttribute("userPaymentList", user.getUserPaymentList());
			model.addAttribute("userShippinglist", user.getUserShippingList());
			model.addAttribute("orderList",user.getOrderList());
			
			UserShipping userShipping = new UserShipping();
			model.addAttribute("userShipping",userShipping);
			
			List<String> stateList = USConstants.listOfUSStatesCode;
			Collections.sort(stateList);
			model.addAttribute("stateList",stateList);
			
			model.addAttribute("listOfShippingAddresses", true);
			model.addAttribute("classActiveOrders", true);
			model.addAttribute("listOfCreditCards", true);
			model.addAttribute("displayOrderDetail", true);
			

			
			return "myProfile";
		}
		
	}
}
