package com.kolystyle.controller;

import java.security.Principal;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kolystyle.domain.Category;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.SearchLog;
import com.kolystyle.domain.SiteSetting;
import com.kolystyle.domain.User;
import com.kolystyle.repository.SearchLogRepository;
import com.kolystyle.service.CategoryService;
import com.kolystyle.service.ProductService;
import com.kolystyle.service.ShoppingCartService;
import com.kolystyle.service.SiteSettingService;
import com.kolystyle.service.UserService;

@Controller
public class SearchController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private SiteSettingService siteSettingService;
	@Autowired
	private ShoppingCartService shoppingCartService;
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private SearchLogRepository searchLogRepository;

	@RequestMapping("/searchByCategory")
	public String searchByCategory(@RequestParam("category") String category,
			Model model, Principal principal){
		 SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
	        model.addAttribute("siteSettings",siteSettings);
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
		model.addAttribute("productCategory",true);
		model.addAttribute("procategory",category);
		
		model.addAttribute("productList",productList);
		
		return "productshelf";
	}
	
	@RequestMapping("/searchProduct")
	public String searchProduct(@ModelAttribute("keyword") String keyword, Principal principal, Model model, HttpServletRequest request){
		keyword = keyword.trim();
		SearchLog searchLog = new SearchLog();
		searchLog.setSearchStarted(Calendar.getInstance().getTime());
		searchLog.setSessionId(request.getSession().getId());
		searchLog.setSearchKeyword(keyword);
		searchLog.setUsedBrowser(request.getHeader("User-Agent"));
		searchLog.setSearchedOnPage(request.getHeader("referer"));
		//request.getHeader("referer")
	//	ShoppingCart shoppingCart = shoppingCartService.findCartByCookie(request);
		
		
		
		///
		Cookie[] cookies = request.getCookies();
		 String cartBagId = null;
		 if (cookies != null){
		int cookieLength = cookies.length;
		
   	 //Check cookie value
		if (cookieLength >0) {
        for(int i = 0; i < cookieLength; i++) { 
            Cookie cartID = cookies[i];
            if (cartID.getName().equalsIgnoreCase("BagId")) {
                System.out.println("BagId = " + cartID.getValue());
                
               cartBagId = cartID.getValue();
            }
        }
		}
		 }
        searchLog.setCartId(cartBagId);
        
		///
		
		System.out.println("SUCCESSFUL WITH COOKIE LOGIC");
		 SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
		  model.addAttribute("siteSettings",siteSettings);
		if(principal != null){
			String username = principal.getName();
			User user = userService.findByUsername(username);
			model.addAttribute("user", user);
		}
		List<Product> productList = productService.blurrySearch(keyword);
		searchLog.setResultReturned(productList.size());
		searchLog.setSearchEnded(Calendar.getInstance().getTime());
		searchLogRepository.save(searchLog);
		if(productList.isEmpty()){
			model.addAttribute("emptyList", true);
			return "productshelf";
		}
		if (productList.size()==1) {
			model.addAttribute("product",productList);
			
		      
			return "redirect:/productDetail?id="+productList.get(0).getId();
		}
		model.addAttribute("productList",productList);
		return "productshelf";
	}
	
	@RequestMapping("/searchBySubCategory")
	public String searchBySubCategory(@RequestParam("category") String category,
			@RequestParam("subCategory") String subCategory,
			@RequestParam("mainsubCategory") String mainsubCategory,
			Model model, Principal principal){
		 SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
	        model.addAttribute("siteSettings",siteSettings);
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
