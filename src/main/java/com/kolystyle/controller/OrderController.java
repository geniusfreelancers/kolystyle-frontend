package com.kolystyle.controller;

import com.kolystyle.domain.*;
import com.kolystyle.repository.ShoppingCartRepository;
import com.kolystyle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;


@Controller
@RequestMapping("/customize")
public class OrderController {

    @Autowired
    private PromoCodesService promoCodesService;

	@Autowired
	private ShoppingCartService shoppingCartService;
	
	@Autowired
	private SiteSettingService siteSettingService;
	
	@Autowired
	private ShoppingCartRepository shoppingCartRepository;

    @RequestMapping("/minicart")
    public @ResponseBody
    ShoppingCart getCartById(HttpServletRequest request){
    	ShoppingCart shoppingCart = shoppingCartService.findCartByCookie(request);
    	if(shoppingCart == null) {
    		return shoppingCart;
    	}else {
    		System.out.println("SUCCESSFUL WITH COOKIE LOGIC");
        	//ShoppingCart shoppingCart = shoppingCartRepository.findOne(new Long(cartId));
        	return shoppingCart;
    	}
		
    }

    
   // @RequestMapping(value = "/add/{shippingMethod}/{cartId}", method = RequestMethod.PUT)
    @RequestMapping("/add/{shippingMethod}/{cartId}")
    public @ResponseBody
    ShoppingCart addShippingMethod(@PathVariable(value = "shippingMethod") String shippingMethod,
    		@PathVariable(value = "cartId") int cartId){
    	ShoppingCart shoppingCart =shoppingCartRepository.findOne(new Long(cartId));
    	SiteSetting siteSetting= siteSettingService.findOne((long) 1);
    	BigDecimal currentCost = shoppingCart.getShippingCost();
    	BigDecimal premiumCost = siteSetting.getPremiumShippingCost();
    	BigDecimal orderTotal = shoppingCart.getOrderTotal();
    	
    	if(shippingMethod.equalsIgnoreCase("premiumShipping")) {
    		if(shoppingCart.getShippingMethod().equalsIgnoreCase("groundShipping")) {
    			shoppingCart.setShippingMethod("premiumShipping");
    			shoppingCart.setShippingCost(currentCost.add(premiumCost));
    			shoppingCart.setOrderTotal(orderTotal.add(premiumCost));
    			shoppingCartRepository.save(shoppingCart);
    			return shoppingCart;
    		}
    	}
    	
    	if(shippingMethod.equalsIgnoreCase("groundShipping")) {
    		if(shoppingCart.getShippingMethod().equalsIgnoreCase("premiumShipping")) {
    			shoppingCart.setShippingMethod("groundShipping");
    			shoppingCart.setShippingCost(currentCost.subtract(premiumCost));
    			shoppingCart.setOrderTotal(orderTotal.subtract(premiumCost));
    			shoppingCartRepository.save(shoppingCart);
    			return shoppingCart;
    		}
    	}
    	
        return shoppingCart;
    }
    
    /*
    @RequestMapping(value = "/add/{productId}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void addItem(@PathVariable(value = "productId") int productId, @AuthenticationPrincipal User activeUser){

        User customer = customerService.findByUsername(activeUser.getUsername());
        ShoppingCart shoppingCart = customer.getShoppingCart();
        Product product = productService.getProductById(productId);
        List<CartItem> cartItems = cart.getCartItems();

        for (int i=0; i<cartItems.size(); i++){
            if(product.getProductId()==cartItems.get(i).getProduct().getProductId()){
                CartItem cartItem = cartItems.get(i);
                cartItem.setQuantity(cartItem.getQuantity()+1);
                cartItem.setTotalPrice(product.getProductPrice()*cartItem.getQuantity());
                cartItemService.addCartItem(cartItem);

                return;
            }
        }

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItem.setTotalPrice(product.getProductPrice()*cartItem.getQuantity());
        cartItem.setCart(cart);
        cartItemService.addCartItem(cartItem);
    }

    @RequestMapping(value = "/remove/{productId}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void removeItem(@PathVariable(value = "productId") int productId){
        CartItem cartItem = cartItemService.getCartItemByProductId(productId);
        cartItemService.removeCartItem(cartItem);
    }
    
    @RequestMapping(value= "/removeItem", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void removeItem(@RequestParam("id") Long id){
		cartItemService.removeCartItem(cartItemService.findById(id));
	}

    @RequestMapping(value = "/{cartId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void clearCart(@PathVariable(value = "cartId") Long cartId){
        ShoppingCart shoppingCart = shoppingCartRepository.findOne(cartId);
    //    cartItemService.removeAllCartItems(shoppingCart);
    }*/

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Illegal request, please verify your payload.")
    public void handleClientErrors(Exception e){}

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Internal Server Error.")
    public void handleServerErrors(Exception e){}
}