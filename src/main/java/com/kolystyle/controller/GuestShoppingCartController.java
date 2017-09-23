package com.kolystyle.controller;

import java.security.Principal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.kolystyle.domain.CartItem;
import com.kolystyle.domain.GuestShoppingCart;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.domain.User;
import com.kolystyle.repository.GuestShoppingCartRepository;
import com.kolystyle.service.CartItemService;
import com.kolystyle.service.ProductService;
import com.kolystyle.service.PromoCodesService;
import com.kolystyle.service.ShoppingCartService;
import com.kolystyle.service.UserService;

@Controller
@RequestMapping("/shoppingCart")
public class GuestShoppingCartController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CartItemService cartItemService;

	@Autowired
	private ShoppingCartService shoppingCartService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private GuestShoppingCartRepository guestShoppingCartRepository;
	
	@Autowired
	private PromoCodesService promoCodesService;
	
	//Apply Promo  Codes 	
	@RequestMapping(value="/applyPromoCode", method=RequestMethod.POST)
	public String applyPromoCode(@ModelAttribute("id") String id,@ModelAttribute("promocode") String promocode, Model model){
		model.addAttribute("invalidPromoError",true);
/*		if(promocode.isEmpty()){
			System.out.println("Please enter promo code");
		}
		PromoCodes promoCodes = promoCodesService.findByPromoCode(promocode);
		if(promoCodes==null){
			System.out.println("Coupon value is: Fuck YOU Get Off from my site");
			model.addAttribute("invalidPromoError",true);
		}else{
			System.out.println("Coupon value is: "+promoCodes.getPromoValue());
		}
		
		
		
		int ShoppingCartId = id.length();
		if(ShoppingCartId < 11){
			System.out.println("Member : "+id);
		}else{
			System.out.println("Guest : "+id);
		}
		return "shoppingCart";*/
		
		return "shoppingCart";
	}
	
	@RequestMapping("/cart")
	public String shoppingCart(Model model,Principal principal,HttpServletRequest request){
		
		User user = null;
		
		HttpSession session = request.getSession();
		
		
		//User need to log in If wanted to implement Guest Check out need to work on this
		if(principal != null){
		ShoppingCart shoppingCart;
		user= userService.findByUsername(principal.getName());
		shoppingCart = user.getShoppingCart();
		
		List<CartItem> cartItemList = cartItemService.findByShoppingCart(shoppingCart);
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
			
			/*Time Stamp and Random Number for Bag Id so we can always
			  have unique bag id within Guest Cart*/
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			
			String bagId = newrandom+"KS"+timestamp.getTime();
   			guestShoppingCart.setBagId(bagId);
   			guestShoppingCartRepository.save(guestShoppingCart);
           
       		// And store to Session.
       		request.getSession().setAttribute("guestShoppingCart",guestShoppingCart);
       	}
       	List<CartItem> cartItemList = cartItemService.findByGuestShoppingCart(guestShoppingCart);
       	shoppingCartService.updateGuestShoppingCart(guestShoppingCart);
       	model.addAttribute("cartItemList",cartItemList);
		model.addAttribute("guestShoppingCart",guestShoppingCart);
		model.addAttribute("guestShoppingCartId",guestShoppingCart.getId());
		model.addAttribute("guestBagId",guestShoppingCart.getBagId());
		model.addAttribute("guestShoppingCartGrandTotal",guestShoppingCart.getGrandTotal());
		model.addAttribute("guestShoppingCart",true);
		}
		
		return "shoppingCart";
	}
	
	@RequestMapping("/addItem")
	public String addItem(@ModelAttribute("product") Product product,@ModelAttribute("qty") String qty,HttpServletRequest request, Model model, Principal principal){
	
		User user = null;
		ShoppingCart shoppingCart;
		HttpSession session = request.getSession();
		GuestShoppingCart guestShoppingCart;
		//Check this for id being null
		product = productService.findOne(product.getId());
		//Check if product qty is available
		if(Integer.parseInt(qty) > product.getInStockNumber()){
			model.addAttribute("notEnoughStock",true);
			return "forward:/productDetail?id="+product.getId();
		}
		
		//Modify this line if you want Guest to add items to cart
        if(principal != null){
        	user =userService.findByUsername(principal.getName());
        	shoppingCart = user.getShoppingCart();
        	CartItem cartItem = cartItemService.addProductToCartItem(product,user,Integer.parseInt(qty));
        }else{  
        	
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
    			
    			/*Time Stamp and Random Number for Bag Id so we can always
    			  have unique bag id within Guest Cart*/
    			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    			
    			String bagId = newrandom+"KS"+timestamp.getTime();
       			guestShoppingCart.setBagId(bagId);
    			guestShoppingCartRepository.save(guestShoppingCart);
            
        		// And store to Session.
        		request.getSession().setAttribute("guestShoppingCart",guestShoppingCart);
        	}
        	CartItem cartItem = cartItemService.addProductToGuestCartItem(product,guestShoppingCart,Integer.parseInt(qty));
        }
 
   
	
		
	
		//May be useful for coupons options with different mapping Else you can delete it
//		List<CartItem> cartItemList = cartItemService.findByShoppingCart(user.getShoppingCart());
//		for(CartItem cartItem : cartItemList){
//			if(product.getId() == cartItem.getProduct().getId()){
//				if(product.getInStockNumber() < (cartItem.getQty()+Integer.parseInt(qty))){
//					model.addAttribute("notEnoughStock",true);
//					return "forward:/productDetail?id="+product.getId();
//				}	}	}		

		
		
		
	
		
		
		model.addAttribute("addProductSuccess",true);
		
		return "forward:/productDetail?id="+product.getId();
		
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
	
}
