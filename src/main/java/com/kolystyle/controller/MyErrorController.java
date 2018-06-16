package com.kolystyle.controller;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kolystyle.domain.HomePage;
import com.kolystyle.domain.HomePageAdditional;
import com.kolystyle.domain.SiteSetting;
import com.kolystyle.service.HomePageService;
import com.kolystyle.service.SiteSettingService;
import com.kolystyle.service.impl.AmazonClient;

@Controller
public class MyErrorController implements ErrorController  {
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
    MyErrorController(AmazonClient amazonClient) {
        this.amazonClient = amazonClient;
    }
	@Autowired
	private SiteSettingService siteSettingService;
	@Autowired
	private HomePageService homePageService;
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request,Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
        HomePage homePage = homePageService.findOne(new Long(1));
        model.addAttribute("homePage",homePage);
        HomePageAdditional homePageAdditional = homePageService.findAdditionalHomePage(new Long(1));
        model.addAttribute("homePageAdditional",homePageAdditional);
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
		
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
         
            if(statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error-404";
            }
            else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error-500";
            }
        }
        return "error";
    }
 
    @Override
    public String getErrorPath() {
        return "/error";
    }
}

