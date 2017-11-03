package com.kolystyle.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kolystyle.domain.Category;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.User;
import com.kolystyle.service.CategoryService;
import com.kolystyle.service.ProductService;
import com.kolystyle.service.UserService;

@Controller
public class SearchController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private CategoryService categoryService;

	@RequestMapping("/searchByCategory")
	public String searchByCategory(@RequestParam("category") String category,
			Model model, Principal principal){
		
		if(principal != null){
			String username = principal.getName();
			User user = userService.findByUsername(username);
			model.addAttribute("user", user);
		}
		
		String classActiveCategory = "active"+category;
		classActiveCategory = classActiveCategory.replaceAll("\\s+", "");
		classActiveCategory = classActiveCategory.replaceAll("&", "");
		
		model.addAttribute("classActiveCategory", true);
		Category thiscategory = categoryService.findCategoryBySlug(category);
		List<Product> productList = productService.findByCategory(thiscategory);
		
		if(productList.isEmpty()){
			model.addAttribute("emptyList", true);
			return "productshelf";
		}
		
		model.addAttribute("productList",productList);
		
		return "productshelf";
	}
	
	@RequestMapping("/searchProduct")
	public String searchProduct(@ModelAttribute("keyword") String keyword, Principal principal, Model model){
		
		if(principal != null){
			String username = principal.getName();
			User user = userService.findByUsername(username);
			model.addAttribute("user", user);
		}
		List<Product> productList = productService.blurrySearch(keyword);
		
		if(productList.isEmpty()){
			model.addAttribute("emptyList", true);
			return "productshelf";
		}
		
		model.addAttribute("productList",productList);
		return "productshelf";
	}
	
	@RequestMapping("/searchBySubCategory")
	public String searchBySubCategory(@RequestParam("category") String category,
			@RequestParam("subCategory") String subCategory,
			@RequestParam("mainsubCategory") String mainsubCategory,
			Model model, Principal principal){
		
		if(principal != null){
			String username = principal.getName();
			User user = userService.findByUsername(username);
			model.addAttribute("user", user);
		}
		
		String classActiveCategory = "active"+category;
		classActiveCategory = classActiveCategory.replaceAll("\\s+", "");
		classActiveCategory = classActiveCategory.replaceAll("&", "");
		
		model.addAttribute("classActiveCategory", true);
		Category thiscategory = categoryService.findCategoryBySlug(category);
		List<Product> productList = productService.findBySubCategory(thiscategory, subCategory, mainsubCategory);
		
		if(productList.isEmpty()){
			model.addAttribute("emptyList", true);
			return "productshelf";
		}
		
		model.addAttribute("productList",productList);
		
		return "productshelf";
	}
	
}
