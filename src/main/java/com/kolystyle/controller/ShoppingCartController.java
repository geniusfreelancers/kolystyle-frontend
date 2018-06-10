package com.kolystyle.controller;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.kolystyle.domain.CartItem;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.repository.CartItemRepository;
import com.kolystyle.repository.ShoppingCartRepository;
import com.kolystyle.service.CartItemService;
import com.kolystyle.service.ProductService;
import com.kolystyle.service.PromoCodesService;
import com.kolystyle.service.ShoppingCartService;
import com.kolystyle.service.SiteSettingService;
import com.kolystyle.service.UserService;

@Controller
@RequestMapping("/rest/cart")
public class ShoppingCartController {
	
	private static final Logger LOG = LoggerFactory.getLogger(ShoppingCartController.class);
	
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
	private ShoppingCartRepository shoppingCartRepository;
	
	@Autowired
	private PromoCodesService promoCodesService;
	
	@Autowired
	private SiteSettingService siteSettingService;
	
	
	
	@RequestMapping("/{cartId}")
    public @ResponseBody
    ShoppingCart getCartById(@PathVariable(value = "cartId") Long cartId,HttpServletRequest request){
		ShoppingCart shoppingCart = shoppingCartService.findCartByCookie(request);
		System.out.println("SUCCESSFUL WITH COOKIE LOGIC");
		if(shoppingCart.getId() == cartId) {
			 return shoppingCart;
		}
        return null;
    }
	
	@RequestMapping("/mycart")
    public @ResponseBody
    ShoppingCart getMyCart(HttpServletRequest request){
		ShoppingCart shoppingCart = shoppingCartService.findCartByCookie(request);
		System.out.println("SUCCESSFUL WITH COOKIE LOGIC");
		
			 return shoppingCart;
		
    }
	/*@RequestMapping("/cart")
	public String shoppingCart(Model model,Principal principal,HttpServletRequest request){
		
		User user = null;
		ShoppingCart shoppingCart;
		HttpSession session = request.getSession();
		
		//User need to log in If wanted to implement Guest Check out need to work on this
		if(principal != null){
		
		user= userService.findByUsername(principal.getName());
		shoppingCart = user.getShoppingCart();
		}else{
			
			// Get Cart from Session.
			shoppingCart = (ShoppingCart) session.getAttribute("ShoppingCart");
       	 
       	// If null, create it.
       	if (shoppingCart == null) {
       		shoppingCart = new ShoppingCart();
       		String sessionID = session.getId();
       		shoppingCart.setSessionId(sessionID);
   			
   			
			//To generate random number 99 is max and 10 is min
			Random rand = new Random();
			int  newrandom = rand.nextInt(99) + 10;
			
		//	Time Stamp and Random Number for Bag Id so we can always
			//  have unique bag id within Guest Cart
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			
			String bagId = newrandom+"KS"+timestamp.getTime();
			shoppingCart.setBagId(bagId);
			shoppingCartRepository.save(shoppingCart);
           
       		// And store to Session.
       		request.getSession().setAttribute("ShoppingCart",shoppingCart);
       	}
       	
		}
		List<CartItem> cartItemList = cartItemService.findByShoppingCart(shoppingCart);
		if(cartItemList.size()< 1) {
			model.addAttribute("emptyCart",true);
		}else {
			model.addAttribute("emptyCart",false);	
		}
       	shoppingCartService.updateShoppingCart(shoppingCart);
       	SiteSetting siteSetting= siteSettingService.findOne((long) 1);
       	
       	if(shoppingCart.getGrandTotal().doubleValue()>=siteSetting.getFreeShippingMin().doubleValue()) {
       		if(shoppingCart.getDiscountedAmount() != null) {
       			if(shoppingCart.getDiscountedAmount().doubleValue()>=siteSetting.getFreeShippingMin().doubleValue()) {
       				model.addAttribute("freeShip",true);
       			}else {
       				model.addAttribute("freeShip",false);
       			}
       		}else {
       			model.addAttribute("freeShip",true);
       		}
       		
       	}else {
       		model.addAttribute("freeShip",false);
       		model.addAttribute("shippingCost",10.00);
       	}
		model.addAttribute("cartItemList",cartItemList);
		model.addAttribute("shoppingCart",shoppingCart);
		
		return "shoppingCart";
	}*/
	
	
	/*@RequestMapping(value = "/addItem", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void addItem(@ModelAttribute("product") Product product,
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
   	 //Check cookie value
        for(int i = 0; i < cookies.length; i++) { 
            Cookie cartID = cookies[i];
            if (cartID.getName().equals("BagId")) {
            	LOG.info("User with Bag Id {} adding product to cart", cartID.getValue());
                System.out.println("BagId = " + cartID.getValue());
                foundCookie = true;
            }
        }
		//Check this for id being null
		product = productService.findOne(product.getId());
		LOG.info("User adding product with ID  {} to cart", product.getId());
		//Check if product qty is available
		if(Integer.parseInt(qty) > product.getInStockNumber()){
			model.addAttribute("notEnoughStock",true);
			LOG.info("User is looking to add {} product with ID to cart in following qty", Integer.parseInt(qty));
			return;
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
		
	}*/
	

	@RequestMapping(value= "/removeItem", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void removeItem(@RequestParam("id") Long id){
		cartItemService.removeCartItem(cartItemService.findById(id));
	}
	
	/*@RequestMapping(value = "/remove/{productId}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void removeCartItem(@PathVariable(value = "productId") Long id){
		cartItemService.removeCartItem(cartItemService.findById(id));
    }*/

	/*@RequestMapping(value = "/remove/{cartItemId}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void removeCartItems(@PathVariable(value = "cartItemId") Long id){
		cartItemService.removeCartItem(cartItemRepository.findOne(id));
		//findById(id)
    }*/
	@RequestMapping(value = "/remove/{cartItemId}", method = RequestMethod.POST)
    public @ResponseBody ShoppingCart removeCartItems(@PathVariable(value = "cartItemId") Long id){
		CartItem cartItem = cartItemRepository.findOne(id);
		cartItemRepository.delete(cartItem);
	//	cartItemService.removeOne(id);
	//	cartItemService.removeCartItem(cartItemRepository.findOne(id));
		
		ShoppingCart shoppingCart = cartItem.getShoppingCart();
		shoppingCart = shoppingCartService.updateShoppingCart(shoppingCart);
		return shoppingCart;
		//findById(id)
    }
	
	@ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Illegal request, please verify your payload.")
    public void handleClientErrors(Exception e){}

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Internal Server Error.")
    public void handleServerErrors(Exception e){}
}
