package com.kolystyle.controller;

import java.security.Principal;
import java.util.List;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kolystyle.domain.ListItem;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.SiteSetting;
import com.kolystyle.domain.User;
import com.kolystyle.domain.WishList;
import com.kolystyle.service.ListItemService;
import com.kolystyle.service.ProductService;
import com.kolystyle.service.SiteSettingService;
import com.kolystyle.service.UserService;
import com.kolystyle.service.WishListService;
import com.kolystyle.service.impl.UserSecurityService;

@Controller
@RequestMapping("/customer")
public class WishListController {
	@Autowired
	private UserService userService;
	@Autowired
	private UserSecurityService userSecurityService;
	@Autowired
	private ListItemService listItemService;
	@Autowired
	private WishListService wishListService;
	@Autowired
	private ProductService productService;
	@Autowired
	private SiteSettingService siteSettingService;
	
	@RequestMapping("/wishlist")
	public String wishList(Model model,Principal principal){
		SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
		User user= userService.findByUsername(principal.getName());
		//User need to log in If wanted to implement Guest Check out need to work on this
		if(user != null){
		WishList wishList = user.getWishList();
		List<ListItem> listItemList = listItemService.findByWishList(wishList);
		
		if(listItemList.size()< 1) {
			model.addAttribute("emptyList",true);
		}
		model.addAttribute("itemCount",listItemList.size());
		model.addAttribute("user",user);
		model.addAttribute("listItemList",listItemList);
		model.addAttribute("wishList",wishList);
		}
		
		return "wishlist";
	}
	
	/*@RequestMapping("/addItem")
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
    			
    			Time Stamp and Random Number for Bag Id so we can always
    			  have unique bag id within Guest Cart
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
		
	}*/
	@RequestMapping("/addtolist")
	public String productDetail(@PathParam("id") Long id, Model model, Principal principal) {
		SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
		if(principal != null){
			String username = principal.getName();
			User user = userService.findByUsername(username);
			model.addAttribute("user", user);
			Product product = productService.findOne(id);
			model.addAttribute("product", product);
			listItemService.addProductToListItem(product,user);
			WishList wishList = user.getWishList();
			List<ListItem> listItemList = listItemService.findByWishList(wishList);
			model.addAttribute("itemCount",listItemList.size());
			model.addAttribute("user",user);
			model.addAttribute("listItemList",listItemList);
			model.addAttribute("wishList",wishList);
			
		}else {
			return "myAccount";
		}
		return "redirect:/customer/wishlist";
	}
	
	@RequestMapping("/removeListItem")
	public String removeListItem(@RequestParam("id") Long id){
		
		listItemService.removeListItem(listItemService.findById(id));
		
		return "forward:/customer/wishlist";
	}

}
