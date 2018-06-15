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
    }
	
	@ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Illegal request, please verify your payload.")
    public void handleClientErrors(Exception e){}

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Internal Server Error.")
    public void handleServerErrors(Exception e){}
}
