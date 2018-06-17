package com.kolystyle.controller;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.kolystyle.domain.Category;
import com.kolystyle.domain.PagerModel;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.kolystyle.repository.ProductsRepository;
import com.kolystyle.repository.SearchLogRepository;
import com.kolystyle.service.CategoryService;
import com.kolystyle.service.ProductService;
import com.kolystyle.service.SiteSettingService;
import com.kolystyle.service.impl.AmazonClient;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.SearchLog;
import com.kolystyle.domain.SiteSetting;
import com.kolystyle.domain.SubCategory;
import com.kolystyle.domain.SubSubCategory;
@Controller
public class ClientController {
    private static final int BUTTONS_TO_SHOW = 3;
    private static final int INITIAL_PAGE = 0;
    private static final int INITIAL_PAGE_SIZE = 20;
    private static final int[] PAGE_SIZES = { 20, 50, 100};
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
    ClientController(AmazonClient amazonClient) {
        this.amazonClient = amazonClient;
    }
    @Autowired
    ProductsRepository productsRepository;
    @Autowired
    ProductService productService;
    @Autowired
    SiteSettingService siteSettingService;
    @Autowired
    CategoryService categoryService;
    @Autowired
	SearchLogRepository searchLogRepository;
    
    @GetMapping("/currentdeals")
    public ModelAndView currentDeals(@RequestParam("pageSize") Optional<Integer> pageSize,
            @RequestParam("page") Optional<Integer> page,@RequestParam("deal") String deal,Model model){
    	SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
        if(!deal.equalsIgnoreCase(siteSettings.getHotDeal())) {
        	deal =siteSettings.getHotDeal();
        }
        ModelAndView modelAndView = new ModelAndView("products");
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
        //
        // Evaluate page size. If requested parameter is null, return initial
        // page size
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        // Evaluate page. If requested parameter is null or less than 0 (to
        // prevent exception), return initial size. Otherwise, return value of
        // param. decreased by 1.
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;
        
        // print repo
        System.out.println("here is client repo " + productService.findByProductTagsContaining(deal,new PageRequest(evalPage, evalPageSize)));
       /* Page<Product> clientlist = productsRepository.findAll(new PageRequest(evalPage, evalPageSize));*/
        Page<Product> clientlist = productService.findByProductTagsContaining(deal,new PageRequest(evalPage, evalPageSize,Sort.Direction.DESC,"id"));
        if(clientlist.hasContent() == false) {
        	model.addAttribute("emptyList", true);
        	List<Product> productList = productService.findTop12ByOrderByIdDesc();
        	model.addAttribute("productList", productList);
        	modelAndView.addObject("hotDeal", false);
        	return modelAndView;
        }else {
        	model.addAttribute("emptyList", false);
        	modelAndView.addObject("hotDeal", true);
        }
        System.out.println("client list get total pages" + clientlist.getTotalPages() + "client list get number " + clientlist.getNumber());
        PagerModel pager = new PagerModel(clientlist.getTotalPages(),clientlist.getNumber(),BUTTONS_TO_SHOW);
        // add clientmodel
        modelAndView.addObject("clientlist",clientlist);
       
        // evaluate page size
        modelAndView.addObject("selectedPageSize", evalPageSize);
        
        modelAndView.addObject("pageType", "/currentdeals?deal="+deal);
        // add page sizes
        modelAndView.addObject("pageSizes", PAGE_SIZES);
        // add pager
        modelAndView.addObject("pager", pager);
        return modelAndView;
    }
    @GetMapping("/products")
    public ModelAndView homepage(@RequestParam("pageSize") Optional<Integer> pageSize,
            @RequestParam("page") Optional<Integer> page,Model model){
    	SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
        ModelAndView modelAndView = new ModelAndView("products");
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
        //
        // Evaluate page size. If requested parameter is null, return initial
        // page size
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        // Evaluate page. If requested parameter is null or less than 0 (to
        // prevent exception), return initial size. Otherwise, return value of
        // param. decreased by 1.
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;
        // print repo
        System.out.println("here is client repo " + productsRepository.findAll());
        Page<Product> clientlist = productsRepository.findAll(new PageRequest(evalPage, evalPageSize));
        if(clientlist.hasContent() == false) {
        	model.addAttribute("emptyList", true);
        	return modelAndView;
        }else {
        	model.addAttribute("emptyList", false);
        }
        System.out.println("client list get total pages" + clientlist.getTotalPages() + "client list get number " + clientlist.getNumber());
        PagerModel pager = new PagerModel(clientlist.getTotalPages(),clientlist.getNumber(),BUTTONS_TO_SHOW);
        // add clientmodel
        modelAndView.addObject("clientlist",clientlist);
        
     // evaluate page size
       // modelAndView.addObject("totalRecord", totalRecord);
        
        // evaluate page size
        modelAndView.addObject("selectedPageSize", evalPageSize);
        modelAndView.addObject("pageType", "/products");
        // add page sizes
        modelAndView.addObject("pageSizes", PAGE_SIZES);
        // add pager
        modelAndView.addObject("pager", pager);
        return modelAndView;
    }
    
    @GetMapping("/searchByBrand")
    public ModelAndView searchByBrand(@RequestParam("pageSize") Optional<Integer> pageSize,
            @RequestParam("page") Optional<Integer> page,@RequestParam("brand") String brand,Model model){
    	SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
        ModelAndView modelAndView = new ModelAndView("products");
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
        //
        // Evaluate page size. If requested parameter is null, return initial
        // page size
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        // Evaluate page. If requested parameter is null or less than 0 (to
        // prevent exception), return initial size. Otherwise, return value of
        // param. decreased by 1.
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;
        
       
        // print repo
        System.out.println("here is client repo " + productService.findByBrandByOrderByIdDesc(brand,new PageRequest(evalPage, evalPageSize)));
       /* Page<Product> clientlist = productsRepository.findAll(new PageRequest(evalPage, evalPageSize));*/
        Page<Product> clientlist = productService.findByBrandByOrderByIdDesc(brand,new PageRequest(evalPage, evalPageSize,Sort.Direction.DESC,"id"));
        if(clientlist.hasContent() == false) {
        	model.addAttribute("emptyList", true);
        	List<Product> productList = productService.findTop12ByOrderByIdDesc();
        	model.addAttribute("productList", productList);
        	return modelAndView;
        }else {
        	model.addAttribute("emptyList", false);
        }
        System.out.println("client list get total pages" + clientlist.getTotalPages() + "client list get number " + clientlist.getNumber());
        PagerModel pager = new PagerModel(clientlist.getTotalPages(),clientlist.getNumber(),BUTTONS_TO_SHOW);
        // add clientmodel
        modelAndView.addObject("clientlist",clientlist);
        // evaluate page size
       // modelAndView.addObject("totalRecord", totalRecord);
        
        // evaluate page size
        modelAndView.addObject("selectedPageSize", evalPageSize);
        
        modelAndView.addObject("pageType", "/searchByBrand?brand="+brand);
        // add page sizes
        modelAndView.addObject("pageSizes", PAGE_SIZES);
        // add pager
        modelAndView.addObject("pager", pager);
        return modelAndView;
    }
    
    @GetMapping("/searchByCategory")
    public ModelAndView searchByCategory(@RequestParam("pageSize") Optional<Integer> pageSize,
            @RequestParam("page") Optional<Integer> page,@RequestParam("category") String category,Model model){
    	SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
        ModelAndView modelAndView = new ModelAndView("products");
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
        //
        // Evaluate page size. If requested parameter is null, return initial
        // page size
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        // Evaluate page. If requested parameter is null or less than 0 (to
        // prevent exception), return initial size. Otherwise, return value of
        // param. decreased by 1.
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;
        Category thiscategory = categoryService.findCategoryBySlug(category);
       
        // print repo
        System.out.println("here is client repo " + productService.findByCategoryByOrderByIdDesc(thiscategory,new PageRequest(evalPage, evalPageSize)));
       /* Page<Product> clientlist = productsRepository.findAll(new PageRequest(evalPage, evalPageSize));*/
        Page<Product> clientlist = productService.findByCategoryByOrderByIdDesc(thiscategory,new PageRequest(evalPage, evalPageSize,Sort.Direction.DESC,"id"));
        if(clientlist.hasContent() == false) {
        	model.addAttribute("emptyList", true);
        	List<Product> productList = productService.findTop12ByOrderByIdDesc();
        	model.addAttribute("productList", productList);
        	return modelAndView;
        }else {
        	model.addAttribute("emptyList", false);
        }
        System.out.println("client list get total pages" + clientlist.getTotalPages() + "client list get number " + clientlist.getNumber());
        PagerModel pager = new PagerModel(clientlist.getTotalPages(),clientlist.getNumber(),BUTTONS_TO_SHOW);
        // add clientmodel
        modelAndView.addObject("clientlist",clientlist);
        // evaluate page size
       // modelAndView.addObject("totalRecord", totalRecord);
        
        // evaluate page size
        modelAndView.addObject("selectedPageSize", evalPageSize);
        model.addAttribute("productCategory",true);
		model.addAttribute("procategory",category);
        modelAndView.addObject("pageType", "/searchByCategory?category="+category);
        // add page sizes
        modelAndView.addObject("pageSizes", PAGE_SIZES);
        // add pager
        modelAndView.addObject("pager", pager);
        return modelAndView;
    }

    @GetMapping("/searchBySubCategory")
    public ModelAndView searchBySubCategory(@RequestParam("pageSize") Optional<Integer> pageSize,
            @RequestParam("page") Optional<Integer> page,@RequestParam("category") String category,
            @RequestParam("subCategory") String subCategory,Model model){
    	SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
        ModelAndView modelAndView = new ModelAndView("products");
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
        //
        // Evaluate page size. If requested parameter is null, return initial
        // page size
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        // Evaluate page. If requested parameter is null or less than 0 (to
        // prevent exception), return initial size. Otherwise, return value of
        // param. decreased by 1.
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;
        Category thiscategory = categoryService.findCategoryBySlug(category);
        List<SubCategory> subCategoryList = categoryService.findSubCategoryBySlug(subCategory);
        SubCategory thisSubCategory = null;
        for(SubCategory subCat : subCategoryList) {
        	if(subCat.getCategory().equals(thiscategory)) {
        		thisSubCategory=subCat;
        	}
        }
        // print repo
        System.out.println("here is client repo " + productService.findByCategoryAndSubCategory(thiscategory, thisSubCategory,new PageRequest(evalPage, evalPageSize)));
       /* Page<Product> clientlist = productsRepository.findAll(new PageRequest(evalPage, evalPageSize));*/
        Page<Product> clientlist = productService.findByCategoryAndSubCategory(thiscategory, thisSubCategory,new PageRequest(evalPage, evalPageSize,Sort.Direction.DESC,"id"));;
        if(clientlist.hasContent() == false) {
        	model.addAttribute("emptyList", true);
        	List<Product> productList = productService.findTop12ByOrderByIdDesc();
        	model.addAttribute("productList", productList);
        	return modelAndView;
        }else {
        	model.addAttribute("emptyList", false);
        }
        System.out.println("client list get total pages" + clientlist.getTotalPages() + "client list get number " + clientlist.getNumber());
        PagerModel pager = new PagerModel(clientlist.getTotalPages(),clientlist.getNumber(),BUTTONS_TO_SHOW);
        // add clientmodel
        modelAndView.addObject("clientlist",clientlist);
        // evaluate page size
       // modelAndView.addObject("totalRecord", totalRecord);
      
        // evaluate page size
        modelAndView.addObject("selectedPageSize", evalPageSize);
        model.addAttribute("productCategory",true);
		model.addAttribute("procategory",category);
        modelAndView.addObject("pageType", "/searchBySubCategory?category="+category+"&subCategory="+subCategory);
        // add page sizes
        modelAndView.addObject("pageSizes", PAGE_SIZES);
        // add pager
        modelAndView.addObject("pager", pager);
        return modelAndView;
    }

    @GetMapping("/searchBySubSubCategory")
    public ModelAndView searchBySubSubCategory(@RequestParam("pageSize") Optional<Integer> pageSize,
            @RequestParam("page") Optional<Integer> page,@RequestParam("category") String category,
            @RequestParam("subCategory") String subCategory,@RequestParam("mainsubCategory") String mainSubCategory,
            Model model){
    	SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
        ModelAndView modelAndView = new ModelAndView("products");
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
        //
        // Evaluate page size. If requested parameter is null, return initial
        // page size
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        // Evaluate page. If requested parameter is null or less than 0 (to
        // prevent exception), return initial size. Otherwise, return value of
        // param. decreased by 1.
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;
        Category thiscategory = categoryService.findCategoryBySlug(category);
        List<SubSubCategory> subSubCategoryList = categoryService.findSubSubCategoryBySlug(mainSubCategory);
      // List<SubCategory> subCategoryList = categoryService.findSubCategoryBySlug(subCategory);
        SubSubCategory thisSubSubCategory = null;
        for(SubSubCategory subSubCat : subSubCategoryList) {
        	if(subSubCat.getSubCategory().getCategory().equals(thiscategory) &&
        	   subSubCat.getSubCategory().getSubCategorySlug().equalsIgnoreCase(subCategory)) {
        		thisSubSubCategory=subSubCat;
        	}
        }
        // print repo
        System.out.println("here is client repo " + productService.findByCategoryAndSubSubCategory(thiscategory, thisSubSubCategory,new PageRequest(evalPage, evalPageSize)));
       
        /* Page<Product> clientlist = productsRepository.findAll(new PageRequest(evalPage, evalPageSize));*/
        Page<Product> clientlist = productService.findByCategoryAndSubSubCategory(thiscategory, thisSubSubCategory,new PageRequest(evalPage, evalPageSize,Sort.Direction.DESC,"id"));;
        if(clientlist.hasContent() == false) {
        	model.addAttribute("emptyList", true);
        	List<Product> productList = productService.findTop12ByOrderByIdDesc();
        	model.addAttribute("productList", productList);
        	return modelAndView;
        }else {
        	model.addAttribute("emptyList", false);
        }
        System.out.println("client list get total pages" + clientlist.getTotalPages() + "client list get number " + clientlist.getNumber());
        PagerModel pager = new PagerModel(clientlist.getTotalPages(),clientlist.getNumber(),BUTTONS_TO_SHOW);
        // add clientmodel
        modelAndView.addObject("clientlist",clientlist);
        // evaluate page size
       // modelAndView.addObject("totalRecord", totalRecord);

        // evaluate page size
        modelAndView.addObject("selectedPageSize", evalPageSize);
        model.addAttribute("productCategory",true);
		model.addAttribute("procategory",category);
        modelAndView.addObject("pageType", "/searchBySubSubCategory?category="+category+"&subCategory="+subCategory+"&mainsubCategory="+mainSubCategory);
        // add page sizes
        modelAndView.addObject("pageSizes", PAGE_SIZES);
        // add pager
        modelAndView.addObject("pager", pager);
        return modelAndView;
    }
    
    @GetMapping("/searchProduct")
    public ModelAndView searchProduct(@RequestParam("pageSize") Optional<Integer> pageSize,
            @RequestParam("page") Optional<Integer> page,@ModelAttribute("keyword") String keyword,Model model,HttpServletRequest request){
    	SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
        ModelAndView modelAndView = new ModelAndView("products");
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
        keyword = keyword.trim();
		SearchLog searchLog = new SearchLog();
		searchLog.setSearchStarted(Calendar.getInstance().getTime());
		searchLog.setSessionId(request.getSession().getId());
		searchLog.setSearchKeyword(keyword);
		searchLog.setUsedBrowser(request.getHeader("User-Agent"));
		searchLog.setSearchedOnPage(request.getHeader("referer"));
        //Get Cookie Value for Bag Id
		Cookie[] cookies = request.getCookies();
		 String cartBagId = null;
		 if (cookies != null){
			int cookieLength = cookies.length;
			//Check cookie value
			if (cookieLength >0) {
			   for(int i = 0; i < cookieLength; i++) { 
		           Cookie cartID = cookies[i];
		           if (cartID.getName().equalsIgnoreCase("BagId")) {
		               System.out.println("BagId retrived for search = " + cartID.getValue()); 
		               cartBagId = cartID.getValue();
		           }
	       		}
			}
		}
       searchLog.setCartId(cartBagId);
        // Evaluate page size. If requested parameter is null, return initial
        // page size
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        // Evaluate page. If requested parameter is null or less than 0 (to
        // prevent exception), return initial size. Otherwise, return value of
        // param. decreased by 1.
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;       
        // print repo
        System.out.println("here is client repo " + productService.searchByKeyword(keyword,new PageRequest(evalPage, evalPageSize)));
       /* Page<Product> clientlist = productsRepository.findAll(new PageRequest(evalPage, evalPageSize));*/
        Page<Product> clientlist = productService.searchByKeyword(keyword,new PageRequest(evalPage, evalPageSize,Sort.Direction.DESC,"id"));
        if(clientlist.hasContent() == false) {
        	model.addAttribute("emptyList", true);
        	List<Product> productList = productService.findTop12ByOrderByIdDesc();
        	model.addAttribute("productList", productList);
        	return modelAndView;
        }else {
        	model.addAttribute("emptyList", false);
        	
        }
        searchLog.setResultReturned(clientlist.getNumberOfElements());
		searchLog.setSearchEnded(Calendar.getInstance().getTime());
		searchLogRepository.save(searchLog);
        System.out.println("client list get total pages" + clientlist.getTotalPages() + "client list get number " + clientlist.getNumber());
        PagerModel pager = new PagerModel(clientlist.getTotalPages(),clientlist.getNumber(),BUTTONS_TO_SHOW);
        // add clientmodel
        modelAndView.addObject("clientlist",clientlist);
        // evaluate page size
       // modelAndView.addObject("totalRecord", totalRecord);
   
        // evaluate page size
        modelAndView.addObject("selectedPageSize", evalPageSize);
        model.addAttribute("searchResult",true);
		model.addAttribute("keyword",keyword);
        modelAndView.addObject("pageType", "/searchProduct?keyword="+keyword);
        // add page sizes
        modelAndView.addObject("pageSizes", PAGE_SIZES);
        // add pager
        modelAndView.addObject("pager", pager);
        return modelAndView;
    }
    
    @PostMapping("/searchProduct")
    public ModelAndView searchProductPost(@RequestParam("pageSize") Optional<Integer> pageSize,
            @RequestParam("page") Optional<Integer> page,@ModelAttribute("keyword") String keyword,Model model,HttpServletRequest request){
    	SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
        ModelAndView modelAndView = new ModelAndView("products");
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
        keyword = keyword.trim();
		SearchLog searchLog = new SearchLog();
		searchLog.setSearchStarted(Calendar.getInstance().getTime());
		searchLog.setSessionId(request.getSession().getId());
		searchLog.setSearchKeyword(keyword);
		searchLog.setUsedBrowser(request.getHeader("User-Agent"));
		searchLog.setSearchedOnPage(request.getHeader("referer"));
        //Get Cookie Value for Bag Id
		Cookie[] cookies = request.getCookies();
		 String cartBagId = null;
		 if (cookies != null){
			int cookieLength = cookies.length;
			//Check cookie value
			if (cookieLength >0) {
			   for(int i = 0; i < cookieLength; i++) { 
		           Cookie cartID = cookies[i];
		           if (cartID.getName().equalsIgnoreCase("BagId")) {
		               System.out.println("BagId retrived for search = " + cartID.getValue()); 
		               cartBagId = cartID.getValue();
		           }
	       		}
			}
		}
       searchLog.setCartId(cartBagId);
        // Evaluate page size. If requested parameter is null, return initial
        // page size
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        // Evaluate page. If requested parameter is null or less than 0 (to
        // prevent exception), return initial size. Otherwise, return value of
        // param. decreased by 1.
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;       
        // print repo
        System.out.println("here is client repo " + productService.searchByKeyword(keyword,new PageRequest(evalPage, evalPageSize)));
       /* Page<Product> clientlist = productsRepository.findAll(new PageRequest(evalPage, evalPageSize));*/
        Page<Product> clientlist = productService.searchByKeyword(keyword,new PageRequest(evalPage, evalPageSize,Sort.Direction.DESC,"id"));
        if(clientlist.hasContent() == false) {
        	model.addAttribute("emptyList", true);
        	List<Product> productList = productService.findTop12ByOrderByIdDesc();
        	model.addAttribute("productList", productList);
        	return modelAndView;
        }else {
        	model.addAttribute("emptyList", false);
        	
        }
        searchLog.setResultReturned(clientlist.getNumberOfElements());
        
		searchLog.setSearchEnded(Calendar.getInstance().getTime());
		searchLogRepository.save(searchLog);
        System.out.println("client list get total pages" + clientlist.getTotalPages() + "client list get number " + clientlist.getNumber());
        PagerModel pager = new PagerModel(clientlist.getTotalPages(),clientlist.getNumber(),BUTTONS_TO_SHOW);
        // add clientmodel
        modelAndView.addObject("clientlist",clientlist);
        // evaluate page size
       // modelAndView.addObject("totalRecord", totalRecord);
   
        // evaluate page size
        modelAndView.addObject("selectedPageSize", evalPageSize);
        model.addAttribute("searchResult",true);
		model.addAttribute("keyword",keyword);
        modelAndView.addObject("pageType", "/searchProduct?keyword="+keyword);
        // add page sizes
        modelAndView.addObject("pageSizes", PAGE_SIZES);
        // add pager
        modelAndView.addObject("pager", pager);
        return modelAndView;
    }
    
    @GetMapping("/productByGender")
    public ModelAndView productByGender(@RequestParam("pageSize") Optional<Integer> pageSize,
            @RequestParam("page") Optional<Integer> page,@RequestParam("gender") String gender,Model model){
    	SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
        ModelAndView modelAndView = new ModelAndView("products");
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
        //
        // Evaluate page size. If requested parameter is null, return initial
        // page size
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        // Evaluate page. If requested parameter is null or less than 0 (to
        // prevent exception), return initial size. Otherwise, return value of
        // param. decreased by 1.
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;
        
       
        // print repo
        System.out.println("here is client repo " + productService.findByGenderOrderByIdDesc(gender,new PageRequest(evalPage, evalPageSize)));
       /* Page<Product> clientlist = productsRepository.findAll(new PageRequest(evalPage, evalPageSize));*/
        Page<Product> clientlist = productService.findByGenderOrderByIdDesc(gender,new PageRequest(evalPage, evalPageSize,Sort.Direction.DESC,"id"));
        if(clientlist.hasContent() == false) {
        	model.addAttribute("emptyList", true);
        	List<Product> productList = productService.findTop12ByOrderByIdDesc();
        	model.addAttribute("productList", productList);
        	return modelAndView;
        }else {
        	model.addAttribute("emptyList", false);
        }
        System.out.println("client list get total pages" + clientlist.getTotalPages() + "client list get number " + clientlist.getNumber());
        PagerModel pager = new PagerModel(clientlist.getTotalPages(),clientlist.getNumber(),BUTTONS_TO_SHOW);
        // add clientmodel
        modelAndView.addObject("clientlist",clientlist);
        // evaluate page size
       // modelAndView.addObject("totalRecord", totalRecord);
        
        // evaluate page size
        modelAndView.addObject("selectedPageSize", evalPageSize);
        
        modelAndView.addObject("pageType", "/productByGender?gender="+gender);
        // add page sizes
        modelAndView.addObject("pageSizes", PAGE_SIZES);
        // add pager
        modelAndView.addObject("pager", pager);
        return modelAndView;
    }
    
    @GetMapping("/productsByTags")
    public ModelAndView productsByTags(@RequestParam("pageSize") Optional<Integer> pageSize,
            @RequestParam("page") Optional<Integer> page,@RequestParam("tag") String tag,Model model){
    	SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
       
        ModelAndView modelAndView = new ModelAndView("products");
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
        //
        // Evaluate page size. If requested parameter is null, return initial
        // page size
        int evalPageSize = pageSize.orElse(INITIAL_PAGE_SIZE);
        // Evaluate page. If requested parameter is null or less than 0 (to
        // prevent exception), return initial size. Otherwise, return value of
        // param. decreased by 1.
        int evalPage = (page.orElse(0) < 1) ? INITIAL_PAGE : page.get() - 1;
        
        // print repo
        System.out.println("here is client repo " + productService.findByProductTagsContaining(tag,new PageRequest(evalPage, evalPageSize)));
       /* Page<Product> clientlist = productsRepository.findAll(new PageRequest(evalPage, evalPageSize));*/
        Page<Product> clientlist = productService.findByProductTagsContaining(tag,new PageRequest(evalPage, evalPageSize,Sort.Direction.DESC,"id"));
        if(clientlist.hasContent() == false) {
        	model.addAttribute("emptyList", true);
        	List<Product> productList = productService.findTop12ByOrderByIdDesc();
        	model.addAttribute("productList", productList);
        	modelAndView.addObject("hotDeal", false);
        	return modelAndView;
        }else {
        	model.addAttribute("emptyList", false);
        	modelAndView.addObject("hotDeal", true);
        }
        System.out.println("client list get total pages" + clientlist.getTotalPages() + "client list get number " + clientlist.getNumber());
        PagerModel pager = new PagerModel(clientlist.getTotalPages(),clientlist.getNumber(),BUTTONS_TO_SHOW);
        // add clientmodel
        modelAndView.addObject("clientlist",clientlist);
       
        // evaluate page size
        modelAndView.addObject("selectedPageSize", evalPageSize);
        
        modelAndView.addObject("pageType", "/currentdeals?tag="+tag);
        // add page sizes
        modelAndView.addObject("pageSizes", PAGE_SIZES);
        // add pager
        modelAndView.addObject("pager", pager);
        return modelAndView;
    }
}