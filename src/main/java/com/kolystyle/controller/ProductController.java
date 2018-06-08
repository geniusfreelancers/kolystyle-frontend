package com.kolystyle.controller;

import java.security.Principal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kolystyle.domain.Category;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.ProductAttr;
import com.kolystyle.domain.ProductAttribute;
import com.kolystyle.domain.SiteSetting;
import com.kolystyle.domain.User;
import com.kolystyle.domain.ViewedRecently;
import com.kolystyle.repository.ProductAttrRepository;
import com.kolystyle.repository.ViewedRecentlyRepository;
import com.kolystyle.service.ProductService;
import com.kolystyle.service.SiteSettingService;
import com.kolystyle.service.UserService;
import com.kolystyle.service.ViewedRecentlyService;
import com.kolystyle.service.impl.AmazonClient;
@Controller
public class ProductController {

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
    ProductController(AmazonClient amazonClient) {
        this.amazonClient = amazonClient;
    }
	@Autowired
	private ProductAttrRepository productAttrRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private SiteSettingService siteSettingService;
	@Autowired
	private ProductService productService;
	@Autowired
	private ViewedRecentlyService viewedRecentlyService;
	@Autowired
	private ViewedRecentlyRepository viewedRecentlyRepository;
	
	@RequestMapping("/productshelf")
	public String productshelf(Model model, Principal principal, Pageable page) {
		if(principal != null){
			String username = principal.getName();
			User user = userService.findByUsername(username);
			model.addAttribute("user", user);
		}
		
		List<Product> productList = productService.findAllByOrderByIdDesc();
		model.addAttribute("productList",productList);
		
		List<ProductAttr> productAttrLis = (List<ProductAttr>) productAttrRepository.findAll();
		
		List<String> brandList = new ArrayList<String>();
		List<String> fabricList = new ArrayList<String>();
		List<String> typeList = new ArrayList<String>();
		List<String> sizeList = new ArrayList<String>();
		List<String> priceList = new ArrayList<String>();
		List<String> styleList = new ArrayList<String>();
		List<String> occasionList = new ArrayList<String>();
		List<String> workList = new ArrayList<String>();
		List<String> colorList = new ArrayList<String>();
		List<ProductAttribute> productAttributeList = new ArrayList<ProductAttribute>();
		for(Product product : productList) {
			
			
			for(ProductAttr productAtt : productAttrLis ) {
				
				productAttributeList.addAll(productAtt.getProductAttribute());
				/*for(ProductAttribute productAttribute : productAttributeList ) {
				}*/
			}
			
			
			if(brandList.contains(product.getBrand())) {
					
			}else {
				brandList.add(product.getBrand());
			}
			if(sizeList.contains(product.getBrand())) {
				
			}else {
				sizeList.add(product.getBrand());
			}
			if(typeList.contains(product.getCategory().getCategoryName())) {
				
			}else {
				typeList.add(product.getCategory().getCategoryName());
			}
			if(fabricList.contains(product.getGender())) {
				
			}else {
				fabricList.add(product.getGender());
			}
			if(priceList.contains(product.getBrand())) {
				
			}else {
				priceList.add(product.getBrand());
			}
			if(styleList.contains(product.getBrand())) {
				
			}else {
				styleList.add(product.getBrand());
			}
			if(occasionList.contains(product.getBrand())) {
				
			}else {
				occasionList.add(product.getBrand());
			}
			if(workList.contains(product.getBrand())) {
				
			}else {
				workList.add(product.getBrand());
			}
			if(colorList.contains(product.getBrand())) {
				
			}else {
				colorList.add(product.getBrand());
			}
		}
		model.addAttribute("productAttributeList",productAttributeList);
		model.addAttribute("productAttributeLis",productAttrLis);
		model.addAttribute("brandList",brandList);
		model.addAttribute("typeList",typeList);
		model.addAttribute("sizeList",sizeList);
		model.addAttribute("fabricList",fabricList);
		model.addAttribute("priceList",priceList);
		model.addAttribute("styleList",styleList);
		model.addAttribute("occasionList",occasionList);
		model.addAttribute("workList",workList);
		model.addAttribute("colorList",colorList);
		model.addAttribute("activeAll",true);
		 SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
	        model.addAttribute("siteSettings",siteSettings);
		return "productshelf";
	}
	
	@RequestMapping("/productDetail")
	public String productDetail(@PathParam("id") Long id, Model model, Principal principal,HttpServletRequest request,HttpServletResponse response) 
	{
		if(principal != null){
			String username = principal.getName();
			User user = userService.findByUsername(username);
			model.addAttribute("user", user);
		}
		ViewedRecently viewedRecently = null;
		String alreadyInList;
		String inList="";
		List<String> alreadyList;
		Product product = productService.findOne(id);
		if(product == null) {
			return "redirect:/productshelf";
		}else {
			/*Get bagId and or cookieValue and find product id if exist else
			set cookie with value and create new ViewedRecently object */
			
			
			Cookie[] cookies = request.getCookies();
			boolean foundCookie = false;
			boolean foundBagId =false;
			String bagId = null;
			String cookieValue = null;
			 if (cookies != null){
			int cookieLength = cookies.length;
			
	   	 //Check cookie value
			if (cookieLength >0) {
	        for(int i = 0; i < cookieLength; i++) { 
	            Cookie cartID = cookies[i];
	            if (cartID.getName().equalsIgnoreCase("BagId")) {
	                System.out.println("BagId = " + cartID.getValue());
	                foundBagId = true;
	                bagId = cartID.getValue();
	            }
	            if (cartID.getName().equalsIgnoreCase("CookieValue")) {
	                System.out.println("CookieValue = " + cartID.getValue());
	                foundCookie = true;
	                cookieValue = cartID.getValue();
	            }
	            
	        }
	       
			}}	
		if (foundCookie==false && foundBagId==true) {
			 viewedRecently = viewedRecentlyService.findByBagId(bagId);
			if(viewedRecently == null) {
				viewedRecently = new ViewedRecently();
				alreadyInList = id.toString();
			}else {
				alreadyInList = viewedRecently.getProductList();
				alreadyInList = alreadyInList+","+id.toString();
			}
				Random rand = new Random();
    			int  newrandom = rand.nextInt(99) + 10;
    			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    			cookieValue = newrandom+"PRO"+timestamp.getTime();
				viewedRecently.setCookieValue(cookieValue);
				viewedRecently.setBagId(bagId);

				alreadyList = Arrays.asList(alreadyInList.split("\\s*,\\s*"));
				if(alreadyList.size()< 1) {
					inList = id.toString();
				}else {
				int count = 1;
				for(String already : alreadyList) {
					if(!already.equalsIgnoreCase(id.toString()) ) {
						if(count == 1) {
							inList += already;
						}else {
							inList += ","+already;
						}
					}else {
						if(count == 1) {
							inList += already;
						}else {
							inList = inList+"";
						}
					}
					count++;
				}
				}
				
    	            Cookie cookie1 = new Cookie("CookieValue",cookieValue);
    	            cookie1.setPath("/");
    	            cookie1.setMaxAge(30*24*60*60);
    	            response.addCookie(cookie1); 
		}else {
			 viewedRecently = viewedRecentlyService.findByCookieValue(cookieValue);
			//Just testing need to change accordingly to match requiremnet
			 if(viewedRecently != null) {
				 	alreadyInList = viewedRecently.getProductList();
					alreadyInList = alreadyInList+","+id.toString();
					if(viewedRecently.getBagId() == null && foundBagId == true) {
						viewedRecently.setBagId(bagId);
					}
				}else {
					viewedRecently = new ViewedRecently();
					alreadyInList = id.toString();
					Random rand = new Random();
	    			int  newrandom = rand.nextInt(99) + 10;
	    			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	    			cookieValue = newrandom+"PRO"+timestamp.getTime();
					viewedRecently.setCookieValue(cookieValue);
					if(foundBagId == true) {
						viewedRecently.setBagId(bagId);
					}
					Cookie[] cookiese = request.getCookies();
					 if (cookiese != null){
	    	            for (Cookie cookie11 : cookiese) {
				            	 if (cookie11.getName().equalsIgnoreCase("CookieValue")) {
				            		 	cookie11.setValue(cookieValue);
				     	            	cookie11.setPath("/");
				     	            	cookie11.setMaxAge(30*24*60*60);
				     	            	response.addCookie(cookie11); 
				     	            	System.out.println("Cookie for CookieValue is modified");
				            	 }
	    	            	}
					 }else{
						 	Cookie cookie12 = new Cookie("CookieValue",cookieValue);
		    	            cookie12.setPath("/");
		    	            cookie12.setMaxAge(30*24*60*60);
		    	            response.addCookie(cookie12); 
		    	            System.out.println("Cookie for CookieValue is added");
					 }
				}
					alreadyList = Arrays.asList(alreadyInList.split("\\s*,\\s*"));
					if(alreadyList.size()<1) {
						inList = id.toString();
					}else {
						int count = 1;
						int track = 1;
						for(String already : alreadyList) {
							if(!already.equalsIgnoreCase(id.toString()) ) {
								if(count == 1) {
									inList += already;
								}else {
									inList += ","+already;
								}
							}else {
								
								if(count == 1) {
									inList += already;
								}else {
									if(track == 1 && count != 1) {
										inList = inList+","+already;
										track++;
									}else {
										inList = inList+"";
									}
								}
							}
							count++;
						}
					}
					
			 
			}
		}
		
		viewedRecently.setProductList(inList);
		List<String> viewedRecentlyList = Arrays.asList(inList.split("\\s*,\\s*"));
		List<Product> viewedProduct = new ArrayList<Product>();
		for(String recent : viewedRecentlyList) {
			viewedProduct.add(productService.findOne(new Long(recent)));
		}
		viewedRecently.setUpdatedDate(Calendar.getInstance().getTime());
		viewedRecentlyRepository.save(viewedRecently);
		model.addAttribute("viewedRecently",viewedProduct);
				
		String availableSize = product.getSize();
		List<String> sizeList = Arrays.asList(availableSize.split("\\s*,\\s*"));
		String productImages = product.getProductImagesName();
		List<String> productImagesList = Arrays.asList(productImages.split("\\s*,\\s*"));
		model.addAttribute("product", product);
		 SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
	        model.addAttribute("siteSettings",siteSettings);
		//List<Integer> qtyList = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
		int qtly = product.getInStockNumber();
		List<Integer> qtyList = new ArrayList<Integer>();
		for (int i=1; i<=qtly;i++) {
			qtyList.add(i);
		}
		Category category = product.getCategory();
		List<Product> productList = productService.findTop12ByCategory(category);
		if(qtyList.size()<1) {
			model.addAttribute("noMore", true);
		}
		
		String brand = product.getBrand();
		List<Product> brandList = productService.findTop15ByBrand(brand);
		
		//List<Product> productList = productService.findByCategory(category);
		model.addAttribute("productList", productList);
		model.addAttribute("brandList", brandList);
		
	
		model.addAttribute("sizeList", sizeList);
		model.addAttribute("productImagesList", productImagesList);
		model.addAttribute("qtyList", qtyList);
		
		return "productinformation";
	//	return "productDetail";
	}
	
	
}
