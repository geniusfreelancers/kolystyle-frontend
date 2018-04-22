package com.kolystyle.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.kolystyle.domain.CartItem;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.PromoCodes;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.domain.SiteSetting;
import com.kolystyle.domain.User;
import com.kolystyle.repository.CartItemRepository;
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
	private CartItemRepository cartItemRepository;

	@Autowired
	private ShoppingCartService shoppingCartService;
	
	@Autowired
	private ProductService productService;
	@Autowired
	private SiteSettingService siteSettingService;
	@Autowired
	private ShoppingCartRepository shoppingCartRepository;
	
	@Autowired
	private PromoCodesService promoCodesService;
	

	
	//Apply Promo  Codes 	
	@RequestMapping(value="/applyPromoCode/{id}/{promocode}",  method = RequestMethod.PUT)
	public @ResponseBody 
	ShoppingCart applyPromoCode(@PathVariable(value = "id") String id, @PathVariable(value = "promocode") String couponCode,
			Model model,Principal principal, HttpServletRequest request){
	//	HttpSession session = request.getSession();
		if(couponCode.isEmpty()){
			model.addAttribute("emptyPromoError",true);
			return null;
		}else {
			couponCode = couponCode.trim();
		}
		PromoCodes promoCodes = promoCodesService.findByPromoCode(couponCode);
		User user = null;
		ShoppingCart shoppingCart;
		if(principal != null){
			user= userService.findByUsername(principal.getName());
			shoppingCart = user.getShoppingCart();
			LOG.info("User {} is a member with shopping cart id of {} and bag ID of {}", user.getUsername(), shoppingCart.getId(),shoppingCart.getBagId());
		}else{	
			
			shoppingCart = shoppingCartService.findCartByCookie(request);
			System.out.println("SUCCESSFUL WITH COOKIE LOGIC");
			 }
		String errors = null; 
		if(promoCodes==null){
			LOG.info("User entered invalid promo code: {}", couponCode.toUpperCase());
			errors = "The promo code "+couponCode.toUpperCase()+" you entered is invalid.";
		}else{
			LOG.info("User entered valid promo code: {} value of {} {}", couponCode.toUpperCase(),promoCodes.getPromoValue(),promoCodes.getPercentOrDollar());
			BigDecimal gTotal = shoppingCart.getGrandTotal();
			BigDecimal promoVal = promoCodes.getPromoValue();
			BigDecimal discountedAmount = new BigDecimal(0);
			LOG.info("User's Shopping Cart Grand Total is: {}", gTotal);
			
			Date start = promoCodes.getStartDate();
	        Date expiry = promoCodes.getExpiryDate();
	        Date today = Calendar.getInstance().getTime();
			//check for cart minimum and expiry and start date
	        if(!promoCodes.isPromoStatus()){
				errors = "Promo code "+couponCode.toUpperCase()+" not active";
			}else if(gTotal.compareTo(promoCodes.getCartTotal()) < 0) {
				LOG.info("User entered valid promo code: {} . But Shopping Cart Total is {} which is less than required Pormo Cart minimum of {} ", couponCode.toUpperCase(),gTotal,promoCodes.getCartTotal());
				errors = "Subtotal must be $"+promoCodes.getCartTotal()+" or above to apply promo code "+couponCode.toUpperCase();
			}else if(promoCodes.getPromoUsedCount() >= promoCodes.getPromoUseCount()){
				errors = "Promo code "+couponCode.toUpperCase()+" is not available anymore";
			}else if(start.after(today)) {
				errors = "Promo code "+couponCode.toUpperCase()+" can be used on or after "+new SimpleDateFormat("MM-dd-yyyy").format(promoCodes.getStartDate());
			}else if(expiry.before(today)) {
				errors = "Promo code "+couponCode.toUpperCase()+" expired on "+new SimpleDateFormat("MM-dd-yyyy").format(promoCodes.getExpiryDate());
			}else if(promoCodes.getCartItemQty() > shoppingCartService.cartItemCount(shoppingCart)) {
				errors = "Minimum "+promoCodes.getCartItemQty()+" items required to use "+couponCode.toUpperCase();
			}else {
				LOG.info("We can proceed with applying promo code {}. It passes all validation",couponCode.toUpperCase());			
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

			LOG.info("Shopping Cart is saved and returning ShoppingCart as JSON");
			}
		}
			shoppingCart.setErrors(errors);
			LOG.info("Promo code not applied because it didn't pass requirenment, Shopping Cart is saving NOW and returned as JSON");
			shoppingCartRepository.save(shoppingCart);
			return	shoppingCartService.updateShoppingCart(shoppingCart);
			//ShoppingCart newshoppingCart = shoppingCartService.findCartByCookie(request);
		//return newshoppingCart;
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
			shoppingCart.setErrors(null);
			shoppingCart.setDiscountedAmount(discountedAmount);
			shoppingCart.setOrderTotal(gTotal.add(shoppingCart.getShippingCost()).subtract(discountedAmount));
			
			shoppingCartRepository.save(shoppingCart);
			return shoppingCartService.updateShoppingCart(shoppingCart);
			//ShoppingCart newshoppingCart = shoppingCartRepository.findOne(id);
			//return newshoppingCart;
		}else {
			//Shopping Cart ID and Bag ID mismatched Do something to 
			LOG.info("Unusual promo code removal is triggred for Cart with ID : {}",shoppingCart.getId());
			return null;
		}
		
	}
	
	@RequestMapping("/cart")
	public String shoppingCart(Model model,Principal principal,HttpServletRequest request){
		SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
		User user = null;
		ShoppingCart shoppingCart;
		List<CartItem> cartItemList = null;
		//User need to log in If wanted to implement Guest Check out need to work on this
		if(principal != null){
		
		user= userService.findByUsername(principal.getName());
		shoppingCart = user.getShoppingCart();
		}else{

			shoppingCart = shoppingCartService.findCartByCookie(request);
			System.out.println("SUCCESSFUL WITH COOKIE LOGIC");
		}
       	if (shoppingCart == null || shoppingCart.getCartItemList().size() < 1) {
       		model.addAttribute("emptyCart",true);
       		

       	}else {
       		cartItemList = cartItemService.findByShoppingCart(shoppingCart);
       	
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
		 System.out.println("Size is : "+size);
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
        	boolean foundCookie = false;

        	 shoppingCart = shoppingCartService.findCartByCookie(request);
        	 System.out.println("SUCCESSFUL WITH COOKIE LOGIC");
        	 LOG.info("Returning Guest User is adding product to cart");
        	 if (shoppingCart != null) {
        		 foundCookie = true;
        	 }
        	 
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
        CartItem cartItem = cartItemService.addProductToCartItem(product,shoppingCart,Integer.parseInt(qty), size);
     	cartItemRepository.save(cartItem);
     	// may not be needed
     //	shoppingCartRepository.save(shoppingCart);
     	//need to fix some issue for null pointer here
     	/*shoppingCart.setGrandTotal(shoppingCartService.calculateCartSubTotal(shoppingCart).setScale(2, BigDecimal.ROUND_HALF_UP));
		shoppingCart.setDiscountedAmount(shoppingCartService.calculateDiscountAmount(shoppingCart,promoCodesService.findByPromoCode(shoppingCart.getPromoCode())).setScale(2, BigDecimal.ROUND_HALF_UP));
		shoppingCart.setShippingCost(shoppingCartService.calculateShippingCost(shoppingCart).setScale(2, BigDecimal.ROUND_HALF_UP));
		shoppingCart.setOrderTotal(shoppingCartService.calculateCartOrderTotal(shoppingCart).setScale(2, BigDecimal.ROUND_HALF_UP));*/

		Date addedDate = Calendar.getInstance().getTime();
		shoppingCart.setUpdatedDate(addedDate);
		
		shoppingCartRepository.save(shoppingCart);
		shoppingCartService.updateShoppingCart(shoppingCart);
		shoppingCartRepository.save(shoppingCart);
		System.out.println("CartList="+shoppingCart.getCartItemList());
		return shoppingCart;
		
	}
	
	@RequestMapping(value="/updateCartItem/{cartItemId}/{qty}", method = RequestMethod.PUT)
	public  @ResponseBody
    ShoppingCart updateShoppingCart(@PathVariable(value = "cartItemId") Long cartItemId, 
			@PathVariable(value = "qty") int qty,Model model){
		CartItem cartItem = cartItemService.findById(cartItemId);
		ShoppingCart shoppingCart = cartItem.getShoppingCart();
		Long shoppingCartId = shoppingCart.getId();
		//need to change logic to return error message from db
		if(qty < 1 ){
			//model.addAttribute("notEnoughStock",true);
			cartItemService.removeCartItem(cartItem);
		}else if(qty > cartItem.getProduct().getInStockNumber()) {
			model.addAttribute("notEnoughStock",true);
		}else {
			cartItem.setQty(qty);
			//check to see if subtotal promo shipping and total is updated as well NEED TO UPDATE
			cartItemService.updateCartItem(cartItem);
			shoppingCartService.updateShoppingCart(shoppingCart);
		}
		shoppingCartRepository.save(shoppingCart);
		return shoppingCartRepository.findOne(shoppingCartId);
		//return shoppingCart;
		
	}
	

	
	/*@RequestMapping("/removeItem")
	public String removeItem(@RequestParam("id") Long id){
		cartItemService.removeCartItem(cartItemService.findById(id));
		
		return "forward:/shoppingCart/cart";
	}*/
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
