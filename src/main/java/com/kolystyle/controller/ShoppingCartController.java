/*package com.kolystyle.controller;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kolystyle.domain.CartItem;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.ShoppingCart;
import com.kolystyle.domain.User;
import com.kolystyle.service.CartItemService;
import com.kolystyle.service.ProductService;
import com.kolystyle.service.ShoppingCartService;
import com.kolystyle.service.UserService;

@Controller
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CartItemService cartItemService;

	@Autowired
	private ShoppingCartService shoppingCartService;
	
	@Autowired
	private ProductService productService;
	
	@RequestMapping("/cart")
	public String shoppingCart(Model model,Principal principal){
		
		//User need to log in If wanted to implement Guest Check out need to work on this
		User user= userService.findByUsername(principal.getName());
		ShoppingCart shoppingCart = user.getShoppingCart();
		
		List<CartItem> cartItemList = cartItemService.findByShoppingCart(shoppingCart);
		
		shoppingCartService.updateShoppingCart(shoppingCart);
		
		model.addAttribute("cartItemList",cartItemList);
		model.addAttribute("shoppingCart",shoppingCart);
		
		return "shoppingCart";
	}
	
	@RequestMapping("/addItem")
	public String addItem(@ModelAttribute("product") Product product,@ModelAttribute("qty") String qty, Model model, Principal principal){
		//Modify this line if you want Guest to add items to cart
		User user = userService.findByUsername(principal.getName());
		product = productService.findOne(product.getId());
	
		//May be useful for coupons options with different mapping Else you can delete it
//		List<CartItem> cartItemList = cartItemService.findByShoppingCart(user.getShoppingCart());
//		for(CartItem cartItem : cartItemList){
//			if(product.getId() == cartItem.getProduct().getId()){
//				if(product.getInStockNumber() < (cartItem.getQty()+Integer.parseInt(qty))){
//					model.addAttribute("notEnoughStock",true);
//					return "forward:/productDetail?id="+product.getId();
//				}	}	}		

		
		if(Integer.parseInt(qty) > product.getInStockNumber()){
			model.addAttribute("notEnoughStock",true);
			return "forward:/productDetail?id="+product.getId();
		}
		
		//Also modify this for Guest Option
		CartItem cartItem = cartItemService.addProductToCartItem(product,user,Integer.parseInt(qty));
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
*/
