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
import org.springframework.beans.factory.annotation.Value;
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
import com.kolystyle.service.impl.AmazonClient;

@Controller
@RequestMapping("/shoppingCart")
public class CartController {
	
	private static final Logger LOG = LoggerFactory.getLogger(CartController.class);
	 private AmazonClient amazonClient;
		

	    @Value("${amazonProperties.endpointUrl}")
	    private String endpointUrl;
	    @Value("${amazonProperties.bucketName}")
	    private String bucketName;
	    @Value("${amazonProperties.accessKey}")
	    private String accessKey;
	    @Value("${amazonProperties.secretKey}")
	    private String secretKey;
	    @Autowired
	    CartController(AmazonClient amazonClient) {
	        this.amazonClient = amazonClient;
	    }
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
			//Validate PromoCodes		
			shoppingCart = promoCodesService.validatePromoCode(promoCodes,shoppingCart,couponCode);
			return	shoppingCartService.updateShoppingCart(shoppingCart);
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
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
		User user = null;
		ShoppingCart shoppingCart;
		List<CartItem> cartItemList = null;

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
       		PromoCodes promoCodes = promoCodesService.findByPromoCode(shoppingCart.getPromoCode());
       		//Promocode Validate
       		shoppingCart = promoCodesService.validatePromoCode(promoCodes,shoppingCart,shoppingCart.getPromoCode());
       		cartItemList = cartItemService.findByShoppingCart(shoppingCart);
			shoppingCartService.updateShoppingCart(shoppingCart);
			model.addAttribute("emptyCart",false);	
		}
       	
       	
		model.addAttribute("cartItemList",cartItemList);
		model.addAttribute("shoppingCart",shoppingCart);
		model.addAttribute("noCartExist",true);
		model.addAttribute("noMiniCartExist",true);
		
		return "shoppingCart";
	}
	
	@RequestMapping("/addItem")
	    public @ResponseBody
	    ShoppingCart addItem(@ModelAttribute("product") Product product,
			@ModelAttribute("qty") int qty,
			@ModelAttribute("size") String size,
			@ModelAttribute("option") String option,
			HttpServletRequest request, HttpServletResponse response, 
			Model model, Principal principal){
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
			LOG.info("User is looking to add non existing product with ID {} to cart in {} qty",product.getId(), qty);
			return null ;
		}
		
		LOG.info("User adding product with ID  {} to cart", product.getId());
		//Check if product qty is available
		if(qty > product.getInStockNumber()){
			model.addAttribute("notEnoughStock",true);
			LOG.info("User is looking to add {} product with ID to cart in following qty", qty);
			return null ;
		}
		
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
    			
    			//Time Stamp and Random Number for Bag Id so we can always
    			  //have unique bag id within Guest Cart
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
        if(product.getInStockNumber() < (cartItemService.findProductQtyInCart(shoppingCart,product)+qty) && !cartItemService.ifProductSizeExist(shoppingCart,product,size)) {
			 model.addAttribute("notEnoughStock",true);	
			 return null;
		}
        Long shoppingCartId = shoppingCart.getId();
        CartItem cartItem = cartItemService.addProductToCartItem(product,shoppingCart,qty, size,option);
        cartItemRepository.save(cartItem);
        if(cartItem == null) {
     		model.addAttribute("notEnoughStock",true);
     		return null;
     	}
      //  cartItemRepository.save(cartItem);

		Date addedDate = Calendar.getInstance().getTime();
		shoppingCart.setUpdatedDate(addedDate);
		
		shoppingCartRepository.save(shoppingCart);
		shoppingCartService.updateShoppingCart(shoppingCart);
		shoppingCartRepository.save(shoppingCart);
		//System.out.println("CartList="+shoppingCart.getCartItemList());
		return shoppingCartRepository.findOne(shoppingCartId);	
	}
	
/*	@RequestMapping("/addItem")
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
    CartItem cartItem = cartItemService.addProductToCartItem(product,shoppingCart,Integer.parseInt(qty), size);
 	if(cartItem == null) {
 		model.addAttribute("notEnoughStock",true);
 		return null;
 	}
    cartItemRepository.save(cartItem);

	Date addedDate = Calendar.getInstance().getTime();
	shoppingCart.setUpdatedDate(addedDate);
	
	shoppingCartRepository.save(shoppingCart);
	shoppingCartService.updateShoppingCart(shoppingCart);
	shoppingCartRepository.save(shoppingCart);
	System.out.println("CartList="+shoppingCart.getCartItemList());
	return shoppingCart;
	
}*/
	@RequestMapping(value="/updateCartItem/{cartItemId}/{qty}/{promoCode}", method = RequestMethod.PUT)
	public  @ResponseBody
    ShoppingCart updateShoppingCart(@PathVariable(value = "cartItemId") Long cartItemId, 
			@PathVariable(value = "qty") int qty,@PathVariable(value = "promoCode") String promoCode,Model model){
		CartItem cartItem = cartItemService.findById(cartItemId);
		 if(cartItem == null) {
	     		model.addAttribute("notEnoughStock",true);
	     		return null;
	     	}
		 ShoppingCart shoppingCart = cartItem.getShoppingCart();
		 Product product = cartItem.getProduct();
		 if(product.getInStockNumber() < cartItemService.findProductQtyInCart(shoppingCart,product)+qty-cartItem.getQty()) {
			 model.addAttribute("notEnoughStock",true);	
			 return null;
			}

		Long shoppingCartId = shoppingCart.getId();
		//need to change logic to return error message from db
		if(qty < 1 ){
			//model.addAttribute("notEnoughStock",true);
			cartItemService.removeCartItem(cartItem);
		//	shoppingCart.setCartItemQty(shoppingCart.getCartItemQty()-cartItem.getQty());
		}else if(qty > cartItem.getProduct().getInStockNumber()) {
			model.addAttribute("notEnoughStock",true);
		}else {
			cartItem.setQty(qty);
			//check to see if subtotal promo shipping and total is updated as well NEED TO UPDATE
			PromoCodes promoCodes = promoCodesService.findByPromoCode(promoCode);
			cartItemService.updateCartItem(cartItem);
			shoppingCart = promoCodesService.validatePromoCode(promoCodes,shoppingCart,promoCode);
			shoppingCart = shoppingCartService.updateShoppingCart(shoppingCart);
		}
		
		shoppingCartRepository.save(shoppingCart);
		shoppingCartService.updateShoppingCart(shoppingCart);
		return shoppingCartRepository.findOne(shoppingCartId);
		
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
