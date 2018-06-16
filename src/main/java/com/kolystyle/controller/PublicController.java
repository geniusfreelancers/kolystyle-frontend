package com.kolystyle.controller;

import java.util.Calendar;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import com.kolystyle.domain.Contact;
import com.kolystyle.domain.SiteSetting;
import com.kolystyle.domain.StaticPage;
import com.kolystyle.repository.ContactRepository;
import com.kolystyle.service.ContactService;
import com.kolystyle.service.SiteSettingService;
import com.kolystyle.service.StaticPageService;
import com.kolystyle.service.impl.AmazonClient;

@Controller
@RequestMapping("/public")
public class PublicController {
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
	    PublicController(AmazonClient amazonClient) {
	        this.amazonClient = amazonClient;
	    }
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private StaticPageService staticPageService;

	@Autowired
	private SiteSettingService siteSettingService;
	@Autowired
	private ContactService contactService;
	@Value("${adminUrl}")
    private String adminPath;
	
	@RequestMapping("/contact")
	public String contact(Model model) {
		SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
        Contact contact = new Contact();
        model.addAttribute("contact",contact);
		return "contact";
	}
	
	@RequestMapping(value="/contact", method = RequestMethod.POST)
	public String contactPost(@ModelAttribute("contact") Contact contact, 
			Model model,HttpServletRequest request) {
		SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
        //Do empty validation here
        if(contact.getFullName().isEmpty() ||
        		contact.getEmail().isEmpty() ||
        		contact.getReason().isEmpty() ||
        		contact.getHelpFor().isEmpty() ||
        		contact.getAssistanceType().isEmpty() ||
        		contact.getReasonType().isEmpty() ||
        		contact.getDetails().isEmpty()) 
        {
        	model.addAttribute("contact",contact);
        	model.addAttribute("missingInput",true);
        	return "contact";
        }else if(contact.getHelpFor().equalsIgnoreCase("Already placed the order") && contact.getOrderNumber().isEmpty()) {
            	model.addAttribute("contact",contact);
            	model.addAttribute("requiredOrder",true);
            	return "contact";	
        }else {
	        contact.setStatus("New");
	        contact.setContactDate(Calendar.getInstance().getTime());
	        
	        List<MultipartFile> productImageList = contact.getProductImage();
	        
	        ///Saving Image
			String contactImage = null;
			String productImageName = null;
			 //Get the uploaded files and store them
			int count = 1;	
	        if (productImageList != null && productImageList.size() > 0) 
	        {
	        	
	            for (MultipartFile productImage : productImageList) {
	            	
	            	contactImage = contactService.updateImage(null, productImage);	
					
						if(count == 1) {
							productImageName = contactImage;
							count++;
						}else {
							productImageName = productImageName+","+contactImage;
						}
	            }
	            contact.setContactImage(productImageName);
	            
	        }
	        contactRepository.save(contact);
	        //Saving Image Ends
	        contact = new Contact();
	        model.addAttribute("contact",contact);
	        model.addAttribute("success",true);
	        model.addAttribute("missingInput",false);
        }
		return "contact";
	}
	
	@RequestMapping("/{pagename}")
	public String pages(@PathVariable String pagename, Model model)
	{
		StaticPage staticpage = staticPageService.findByPagename(pagename);
		SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
        String fileUrl = endpointUrl + "/" + bucketName + "/";
		model.addAttribute("fileUrl", fileUrl);
		if(staticpage != null && staticpage.isPublished()) {
			model.addAttribute("staticpage", staticpage);
			return "staticpage";
		}
		
		return "badRequestPage";
	}
	
}
