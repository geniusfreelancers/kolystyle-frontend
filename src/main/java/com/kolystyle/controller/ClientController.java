package com.kolystyle.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.kolystyle.service.CategoryService;
import com.kolystyle.service.ProductService;
import com.kolystyle.service.SiteSettingService;
import com.kolystyle.service.impl.AmazonClient;
import com.kolystyle.domain.Product;
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
    /*@Autowired
    SubCategoryService subCategoryService;*/
    
    @GetMapping("/products")
    public ModelAndView homepage(@RequestParam("pageSize") Optional<Integer> pageSize,
            @RequestParam("page") Optional<Integer> page,Model model){
    	SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
        ModelAndView modelAndView = new ModelAndView("products");
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
        System.out.println("client list get total pages" + clientlist.getTotalPages() + "client list get number " + clientlist.getNumber());
        PagerModel pager = new PagerModel(clientlist.getTotalPages(),clientlist.getNumber(),BUTTONS_TO_SHOW);
        // add clientmodel
        modelAndView.addObject("clientlist",clientlist);
        
     // evaluate page size
       // modelAndView.addObject("totalRecord", totalRecord);
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
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
        
        System.out.println("client list get total pages" + clientlist.getTotalPages() + "client list get number " + clientlist.getNumber());
        PagerModel pager = new PagerModel(clientlist.getTotalPages(),clientlist.getNumber(),BUTTONS_TO_SHOW);
        // add clientmodel
        modelAndView.addObject("clientlist",clientlist);
        // evaluate page size
       // modelAndView.addObject("totalRecord", totalRecord);
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
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
        System.out.println("client list get total pages" + clientlist.getTotalPages() + "client list get number " + clientlist.getNumber());
        PagerModel pager = new PagerModel(clientlist.getTotalPages(),clientlist.getNumber(),BUTTONS_TO_SHOW);
        // add clientmodel
        modelAndView.addObject("clientlist",clientlist);
        // evaluate page size
       // modelAndView.addObject("totalRecord", totalRecord);
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
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
        System.out.println("client list get total pages" + clientlist.getTotalPages() + "client list get number " + clientlist.getNumber());
        PagerModel pager = new PagerModel(clientlist.getTotalPages(),clientlist.getNumber(),BUTTONS_TO_SHOW);
        // add clientmodel
        modelAndView.addObject("clientlist",clientlist);
        // evaluate page size
       // modelAndView.addObject("totalRecord", totalRecord);
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
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
        System.out.println("client list get total pages" + clientlist.getTotalPages() + "client list get number " + clientlist.getNumber());
        PagerModel pager = new PagerModel(clientlist.getTotalPages(),clientlist.getNumber(),BUTTONS_TO_SHOW);
        // add clientmodel
        modelAndView.addObject("clientlist",clientlist);
        // evaluate page size
       // modelAndView.addObject("totalRecord", totalRecord);
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
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
    
}