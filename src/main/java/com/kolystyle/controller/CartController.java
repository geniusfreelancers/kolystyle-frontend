package com.kolystyle.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kolystyle.domain.CartItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.PromoCodes;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.domain.SiteSetting;
import com.kolystyle.domain.User;
import com.kolystyle.repository.ShoppingCartRepository;
import com.kolystyle.service.CartItemService;
import com.kolystyle.service.ProductService;
import com.kolystyle.service.PromoCodesService;
import com.kolystyle.service.ShoppingCartService;
import com.kolystyle.service.SiteSettingService;
import com.kolystyle.service.UserService;

@Controller
@RequestMapping("/shoppingCart")
public class CartController {
	
	private static final Logger LOG = LoggerFactory.getLogger(CartController.class);
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CartItemService cartItemService;

	@Autowired
	private ShoppingCartService shoppingCartService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private ShoppingCartRepository shoppingCartRepository;
	
	@Autowired
	private PromoCodesService promoCodesService;
	

	
	//Apply Promo  Codes 	
	@RequestMapping(value="/applyPromoCode", method=RequestMethod.POST)
	public @ResponseBody 
	ShoppingCart applyPromoCode(@ModelAttribute("id") String id,
			@ModelAttribute("promocode") String couponCode, 
			Model model,Principal principal, HttpServletRequest request){
		HttpSession session = request.getSession();
		if(couponCode.isEmpty()){
			model.addAttribute("emptyPromoError",true);
			return null;
		}
		PromoCodes promoCodes = promoCodesService.findByPromoCode(couponCode);
		User user = null;
		ShoppingCart shoppingCart;
		if(principal != null){
			user= userService.findByUsername(principal.getName());
			shoppingCart = user.getShoppingCart();
			LOG.info("User {} is a member with shopping cart id of {} and bag ID of {}", user.getUsername(), shoppingCart.getId(),shoppingCart.getBagId());
		}else{	
			//Get cart from cookie
			Cookie[] cookies = request.getCookies();
			boolean foundCookie = false;
			 String cartBagId = null;
			 if (cookies != null){
			int cookieLength = cookies.length;
			
	   	 //Check cookie value
			if (cookieLength >0) {
	        for(int i = 0; i < cookieLength; i++) { 
	            Cookie cartID = cookies[i];
	            if (cartID.getName().equalsIgnoreCase("BagId")) {
	            	LOG.info("User with Bag Id {} adding product to cart", cartID.getValue());
	                System.out.println("BagId = " + cartID.getValue());
	                foundCookie = true;
	               cartBagId = cartID.getValue();
	            }
	        }
	       
		}
			 }
			 shoppingCart = shoppingCartService.findCartByBagId(cartBagId);
	/*		// Get Cart from Session.
			shoppingCart = (ShoppingCart) session.getAttribute("ShoppingCart");
			LOG.info("User is a GUEST with shopping cart id of {} and bag ID of {}", shoppingCart.getId(),shoppingCart.getBagId());
		*/
			 }
		if(promoCodes==null){
			LOG.info("User entered invalid promo code: {}", couponCode);
			return shoppingCart;
		}else{
			LOG.info("User entered valid promo code: {} value of {} {}", couponCode.toUpperCase(),promoCodes.getPromoValue(),promoCodes.getPercentOrDollar());
		}
		

			BigDecimal gTotal = shoppingCart.getGrandTotal();
			BigDecimal promoVal = promoCodes.getPromoValue();
			BigDecimal discountedAmount = new BigDecimal(0);
			LOG.info("User's Shopping Cart Grand Total is: {}", gTotal);
			if(promoCodes.getPercentOrDollar().equalsIgnoreCase("dollar")) {
				discountedAmount = promoVal;
						//gTotal- promoCodes.getPromoValue();
				LOG.info("User's applied Coupon Code with dollar value of: {}", promoVal);
				LOG.info("User's New Shopping Cart Grand Total is: {} after {} dollars discount", discountedAmount,promoVal);
			}else {
				discountedAmount = promoVal.divide(new BigDecimal(100),2);
				LOG.info("User's applied Coupon Code with percentage value of: {}%", promoCodes.getPromoValue());
				discountedAmount = discountedAmount.multiply(gTotal);
				LOG.info("User's applied Coupon Code with percentage value of: {}% and gets $ {} discount", promoVal,discountedAmount);
				//discountedAmount = gTotal.subtract(discountedAmount);
				LOG.info("User's New Shopping Cart Grand Total is: {} after {} percentage discount", discountedAmount,promoVal);
			}
			
			
			shoppingCart.setPromoCode(couponCode);
			LOG.info("Promo Code {} is stored in Shopping Cart with Bag ID {}",couponCode, shoppingCart.getBagId());
			shoppingCart.setDiscountedAmount(discountedAmount);
			LOG.info("Stored Discounted Amount {} Shopping Cart with Bag ID {} where Grand Total was {}",discountedAmount,couponCode, shoppingCart.getBagId(),shoppingCart.getGrandTotal());
			//check for shipping cost
			
			shoppingCart.setOrderTotal(gTotal.add(shoppingCart.getShippingCost()).subtract(discountedAmount));
			shoppingCartRepository.save(shoppingCart);
			LOG.info("Shopping Cart is saved and returning promoCodes as JSON");

		return shoppingCart;
	}
	
	@RequestMapping(value="/removePromoCode/{cartId}/{bagId}", method=RequestMethod.POST)
	public @ResponseBody 
	ShoppingCart removePromoCode(@PathVariable(value = "cartId") Long id,@PathVariable(value = "bagId") String bagId) {
		ShoppingCart shoppingCart = shoppingCartRepository.findOne(id);
		if(shoppingCart.getBagId().equalsIgnoreCase(bagId)) {
			//Remove Promocode from shopping cart. Reset discount value order total. Shipping Cost
			BigDecimal gTotal = shoppingCart.getGrandTotal();
			BigDecimal discountedAmount = new BigDecimal(0);
			shoppingCart.setPromoCode(null);
			shoppingCart.setDiscountedAmount(discountedAmount);
			shoppingCart.setOrderTotal(gTotal.add(shoppingCart.getShippingCost()).subtract(discountedAmount));
			shoppingCartRepository.save(shoppingCart);
			
			return shoppingCart;
		}else {
			//Shopping Cart ID and Bag ID mismatched Do something to 
			return null;
		}
		
	}
	
	@RequestMapping("/cart")
	public String shoppingCart(Model model,Principal principal,HttpServletRequest request){
		
		User user = null;
		ShoppingCart shoppingCart;
		HttpSession session = request.getSession();
		
		//User need to log in If wanted to implement Guest Check out need to work on this
		if(principal != null){
		
		user= userService.findByUsername(principal.getName());
		shoppingCart = user.getShoppingCart();
		}else{
			Cookie[] cookies = request.getCookies();
			boolean foundCookie = false;
			 String cartBagId = null;
			 if (cookies != null){
			int cookieLength = cookies.length;
			
	   	 //Check cookie value
			if (cookieLength >0) {
	        for(int i = 0; i < cookieLength; i++) { 
	            Cookie cartID = cookies[i];
	            if (cartID.getName().equalsIgnoreCase("BagId")) {
	            	LOG.info("User with Bag Id {} adding product to cart", cartID.getValue());
	                System.out.println("BagId = " + cartID.getValue());
	                foundCookie = true;
	               cartBagId = cartID.getValue();
	            }
	        }
	       
		}
			 }
			// Get Cart from Session.	shoppingCart = (ShoppingCart) session.getAttribute("ShoppingCart");
			
			 shoppingCart = shoppingCartService.findCartByBagId(cartBagId);
       	// If null, create it.
       	if (shoppingCart == null) {
       		model.addAttribute("emptyCart",true);
       		return "shoppingCart";
/*//       		shoppingCart = new ShoppingCart();
//       		String sessionID = session.getId();
//       		shoppingCart.setSessionId(sessionID);
//   			
//   			
//			//To generate random number 99 is max and 10 is min
//			Random rand = new Random();
//			int  newrandom = rand.nextInt(99) + 10;
//			
//		//	Time Stamp and Random Number for Bag Id so we can always
//			//  have unique bag id within Guest Cart
//			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//			
//			String bagId = newrandom+"KS"+timestamp.getTime();
//			shoppingCart.setBagId(bagId);
//			shoppingCartRepository.save(shoppingCart);
*/           
       		// And store to Session.
   //    		request.getSession().setAttribute("ShoppingCart",shoppingCart);
       	}
       	
		}
		List<CartItem> cartItemList = cartItemService.findByShoppingCart(shoppingCart);
		if(cartItemList.size()< 1) {
			model.addAttribute("emptyCart",true);
		}else {
			shoppingCartService.updateShoppingCart(shoppingCart);
			model.addAttribute("emptyCart",false);	
		}
       	
       	
		model.addAttribute("cartItemList",cartItemList);
		model.addAttribute("shoppingCart",shoppingCart);
		model.addAttribute("noCartExist",true);
		
		return "shoppingCart";
	}
	
/*	@RequestMapping("/addItem")
	public String addItem(@ModelAttribute("product") Product product,
			@ModelAttribute("qty") String qty,
			@ModelAttribute("size") String size,
			HttpServletRequest request, HttpServletResponse response, 
			Model model, 
			Principal principal){
		User user = null;
		ShoppingCart shoppingCart;
		//Get Browser cookie and Session
		HttpSession session = request.getSession();
		LOG.info("User with session Id {} adding product to cart", request.getSession().getId());
		Cookie[] cookies = request.getCookies();
		boolean foundCookie = false;
		int cookieLength = cookies.length;
   	 //Check cookie value
		if (cookieLength >0) {
        for(int i = 0; i < cookieLength; i++) { 
            Cookie cartID = cookies[i];
            if (cartID.getName().equals("BagId")) {
            	LOG.info("User with Bag Id {} adding product to cart", cartID.getValue());
                System.out.println("BagId = " + cartID.getValue());
                foundCookie = true;
            }
        }
	}
		//Check this for id being null
		product = productService.findOne(product.getId());
		LOG.info("User adding product with ID  {} to cart", product.getId());
		//Check if product qty is available
		if(Integer.parseInt(qty) > product.getInStockNumber()){
			model.addAttribute("notEnoughStock",true);
			LOG.info("User is looking to add {} product with ID to cart in following qty", Integer.parseInt(qty));
			return "forward:/productDetail?id="+product.getId();
		}
		
		//Modify this line if you want Guest to add items to cart
        if(principal != null){
        	user =userService.findByUsername(principal.getName());
        	LOG.info("User {} is adding product to cart", user.getUsername());
        	shoppingCart = user.getShoppingCart();
        }else{  
        	
        	//Get Cart By Bag Id
        	
        	// Get Cart from Session.
        	 shoppingCart = (ShoppingCart) session.getAttribute("ShoppingCart");
        	 LOG.info("Returning Guest User is adding product to cart");
        	 
        	// If null, create it.
        	if (shoppingCart == null) {
        		shoppingCart = new ShoppingCart();
        		String sessionID = session.getId();
        		shoppingCart.setSessionId(sessionID);   			
       			
    			//To generate random number 99 is max and 10 is min
    			Random rand = new Random();
    			int  newrandom = rand.nextInt(99) + 10;
    			
    			Time Stamp and Random Number for Bag Id so we can always
    			  have unique bag id within Guest Cart
    			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    			
    			String bagId = newrandom+"KS"+timestamp.getTime();
    			shoppingCart.setCartType("guest");
    			shoppingCart.setBagId(bagId);
    			shoppingCartRepository.save(shoppingCart);
    			LOG.info("Guest User with Bag ID {} is adding product to cart", shoppingCart.getBagId());
        		// And store to Session.
        		request.getSession().setAttribute("ShoppingCart",shoppingCart);
        		// And CartId to cookie.
       		 if (!foundCookie) {
       	            Cookie cookie1 = new Cookie("BagId",shoppingCart.getBagId());
       	            cookie1.setPath("/");
       	            cookie1.setMaxAge(30*24*60*60);
       	            response.addCookie(cookie1); 
       	        }

        	}
        }
        
     	cartItemService.addProductToCartItem(product,shoppingCart,Integer.parseInt(qty), size);
        
		model.addAttribute("addProductSuccess",true);
		
		return "forward:/productDetail?id="+product.getId();
		
	}*/
	 @RequestMapping("/addItem")
	    public @ResponseBody
	    ShoppingCart addItem(@ModelAttribute("product") Product product,
			@ModelAttribute("qty") String qty,
			@ModelAttribute("size") String size,
			HttpServletRequest request, HttpServletResponse response, 
			Model model, 
			Principal principal){
		User user = null;
		ShoppingCart shoppingCart;
		//Get Browser cookie and Session
		HttpSession session = request.getSession();
		LOG.info("User with session Id {} adding product to cart", request.getSession().getId());
		
		//Check this for id being null
		try {
			product = productService.findOne(product.getId());
		}catch (NullPointerException  e){
			model.addAttribute("doesNotExist",true);
			LOG.info("User is looking to add non existing product with ID {} to cart in {} qty",product.getId(), Integer.parseInt(qty));
			return null ;
		}
		
		LOG.info("User adding product with ID  {} to cart", product.getId());
		//Check if product qty is available
		if(Integer.parseInt(qty) > product.getInStockNumber()){
			model.addAttribute("notEnoughStock",true);
			LOG.info("User is looking to add {} product with ID to cart in following qty", Integer.parseInt(qty));
			return null ;
		}
		
		//Modify this line if you want Guest to add items to cart
        if(principal != null){
        	user =userService.findByUsername(principal.getName());
        	LOG.info("User {} is adding product to cart", user.getUsername());
        	shoppingCart = user.getShoppingCart();
        }else{  
        	Cookie[] cookies = request.getCookies();
			boolean foundCookie = false;
			 String cartBagId = null;
			 if (cookies != null){
			int cookieLength = cookies.length;
			
	   	 //Check cookie value
			if (cookieLength >0) {
	        for(int i = 0; i < cookieLength; i++) { 
	            Cookie cartID = cookies[i];
	            if (cartID.getName().equalsIgnoreCase("BagId")) {
	            	LOG.info("User with Bag Id {} adding product to cart", cartID.getValue());
	                System.out.println("BagId = " + cartID.getValue());
	                foundCookie = true;
	               cartBagId = cartID.getValue();
	            }
	        }
	       
		}
			 }
        	
        	//Get Cart By Bag Id
        	 shoppingCart = shoppingCartService.findCartByBagId(cartBagId);
        	// Get Cart from Session.  shoppingCart = (ShoppingCart) session.getAttribute("ShoppingCart");
        	 LOG.info("Returning Guest User is adding product to cart");
        	 
        	// If null, create it.
        	if (shoppingCart == null) {
        		shoppingCart = new ShoppingCart();
        		String sessionID = session.getId();
        		shoppingCart.setSessionId(sessionID);   			
       			
    			//To generate random number 99 is max and 10 is min
    			Random rand = new Random();
    			int  newrandom = rand.nextInt(99) + 10;
    			
    			/*Time Stamp and Random Number for Bag Id so we can always
    			  have unique bag id within Guest Cart*/
    			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    			
    			String bagId = newrandom+"KS"+timestamp.getTime();
    			shoppingCart.setCartType("guest");
    			shoppingCart.setBagId(bagId);
    			shoppingCartRepository.save(shoppingCart);
    			LOG.info("Guest User with Bag ID {} is adding product to cart", shoppingCart.getBagId());
        		// And store to Session.
        		request.getSession().setAttribute("ShoppingCart",shoppingCart);
        		// And CartId to cookie.
       		 if (!foundCookie) {
       	            Cookie cookie1 = new Cookie("BagId",shoppingCart.getBagId());
       	            cookie1.setPath("/");
       	            cookie1.setMaxAge(30*24*60*60);
       	            response.addCookie(cookie1); 
       	        }

        	}
        }
        
     	cartItemService.addProductToCartItem(product,shoppingCart,Integer.parseInt(qty), size);
     	
		System.out.println(shoppingCart.getCartItemList());
		return shoppingCart;
		
	}
	
	@RequestMapping("/updateCartItem")
	public String updateShoppingCart(@ModelAttribute("id") Long cartItemId, @ModelAttribute("qty") int qty,Model model){
		CartItem cartItem = cartItemService.findById(cartItemId);
		
		if(qty < 1 ){
			//model.addAttribute("notEnoughStock",true);
			cartItemService.removeCartItem(cartItem);
			return "forward:/shoppingCart/cart";
		}
		
		if(qty > cartItem.getProduct().getInStockNumber()){
			model.addAttribute("notEnoughStock",true);
			return "forward:/shoppingCart/cart";
			}
		
		
		cartItem.setQty(qty);
		cartItemService.updateCartItem(cartItem);
		
		return "forward:/shoppingCart/cart";
		
	}
	
	@RequestMapping("/removeItem")
	public String removeItem(@RequestParam("id") Long id){
		cartItemService.removeCartItem(cartItemService.findById(id));
		
		return "forward:/shoppingCart/cart";
	}
	/******
	 * Making JSON RESPONSE FOR SHOPPING CART TO DISPLAY MINI CART
	 */
	/*@RequestMapping(value="/minicart", method=RequestMethod.GET)
	public @ResponseBody 
	List<CartItem> miniCart(Model model,Principal principal,HttpServletRequest request){
		
		User user = null;
		
		HttpSession session = request.getSession();
		
		List<CartItem> cartItemList;
		//User need to log in If wanted to implement Guest Check out need to work on this
		if(principal != null){
		ShoppingCart shoppingCart;
		user= userService.findByUsername(principal.getName());
		shoppingCart = user.getShoppingCart();
		
		cartItemList = cartItemService.findByShoppingCart(shoppingCart);
		shoppingCartService.updateShoppingCart(shoppingCart);
		model.addAttribute("cartItemList",cartItemList);
		model.addAttribute("shoppingCart",shoppingCart);
		model.addAttribute("userShoppingCart",true);

		}else{
			GuestShoppingCart guestShoppingCart;
			// Get Cart from Session.
       	 guestShoppingCart = (GuestShoppingCart) session.getAttribute("guestShoppingCart");
       	 
       	// If null, create it.
       	if (guestShoppingCart == null) {
       		guestShoppingCart = new GuestShoppingCart();
       		String sessionID = session.getId();
   			guestShoppingCart.setGuestSession(sessionID);
   			
   			
			//To generate random number 99 is max and 10 is min
			Random rand = new Random();
			int  newrandom = rand.nextInt(99) + 10;
			
			Time Stamp and Random Number for Bag Id so we can always
			  have unique bag id within Guest Cart
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			
			String bagId = newrandom+"KS"+timestamp.getTime();
   			guestShoppingCart.setBagId(bagId);
   			guestShoppingCartRepository.save(guestShoppingCart);
           
       		// And store to Session.
       		request.getSession().setAttribute("guestShoppingCart",guestShoppingCart);
       	}
       	cartItemList = cartItemService.findByGuestShoppingCart(guestShoppingCart);
       	shoppingCartService.updateGuestShoppingCart(guestShoppingCart);
       	model.addAttribute("cartItemList",cartItemList);
		model.addAttribute("guestShoppingCart",guestShoppingCart);
		model.addAttribute("guestShoppingCartId",guestShoppingCart.getId());
		model.addAttribute("guestBagId",guestShoppingCart.getBagId());
		model.addAttribute("guestShoppingCartGrandTotal",guestShoppingCart.getGrandTotal());
		model.addAttribute("guestShoppingCart",true);
		}
		
		return cartItemList;
	}*/
}
