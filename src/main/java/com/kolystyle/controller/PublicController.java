package com.kolystyle.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.kolystyle.domain.Contact;
import com.kolystyle.domain.SiteSetting;
import com.kolystyle.domain.StaticPage;
import com.kolystyle.repository.ContactRepository;
import com.kolystyle.service.SiteSettingService;
import com.kolystyle.service.StaticPageService;

@Controller
@RequestMapping("/public")
public class PublicController {
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private StaticPageService staticPageService;

	@Autowired
	private SiteSettingService siteSettingService;
	@Value("${adminUrl}")
    private String adminPath;
	
	@RequestMapping("/contact")
	public String contact(Model model) {
		SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
        Contact contact = new Contact();
        model.addAttribute("contact",contact);
		return "contact";
	}
	
	@RequestMapping(value="/contact", method = RequestMethod.POST)
	public String contactPost(@ModelAttribute("contact") Contact contact, 
			@RequestBody List<MultipartFile> productImage, Model model,HttpServletRequest request) {
		SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
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
	        contactRepository.save(contact);
	        ///Saving Image
			String productImageName = null;
			 //Get the uploaded files and store them
			int count = 1;	
	        List<MultipartFile> files = productImage;
	        if (files != null && files.size() > 0) 
	        {
	        	
	            for (MultipartFile multipartFile : files) {
	            	
	            	try {
						byte[] bytes = multipartFile.getBytes();
						
						//To generate random number 50 is max and 10 is min
						Random rand = new Random();
						int  newrandom = rand.nextInt(50) + 10;
						
						/*Using Product Id with Time Stamp and Random Number for File name so we can 
						  have unique file always within product id folder*/
						Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						String newFileName = contact.getId()+timestamp.getTime()+newrandom+".png";
						
							if(count == 1) {
								productImageName = newFileName;
								count++;
							}else {
								productImageName = productImageName+","+newFileName;
							}
						
						//String PATH = "http:\\localhost:8083\\image\\product/";
						String PATH = "/src/main/resources/static/image/contact/";
					    
					//	String folderName =  PATH.concat(Long.toString(contact.getId()));
						String folderName =  PATH;
						//Create Folder with product ID as name
						File directory = new File(folderName);
					    if (! directory.exists()){
					        directory.mkdir();     
					    }
						 
						 BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(folderName+"/"+newFileName)));
						 stream.write(bytes);
						 stream.close();
						 
	            	} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	            }
	            contact.setContactImage(productImageName);
	            contactRepository.save(contact);
	        }
	        
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
		if(staticpage != null && staticpage.isPublished()) {
			model.addAttribute("staticpage", staticpage);
			return "staticpage";
		}
		
		return "badRequestPage";
	}
	
}
