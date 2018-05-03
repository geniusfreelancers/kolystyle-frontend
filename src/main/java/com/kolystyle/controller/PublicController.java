package com.kolystyle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.kolystyle.domain.Contact;
import com.kolystyle.domain.SiteSetting;
import com.kolystyle.domain.StaticPage;
import com.kolystyle.domain.UserShipping;
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
	
	@RequestMapping("/contact")
	public String contact(Model model) {
		SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
        Contact contact = new Contact();
        model.addAttribute("contact",contact);
		return "contact";
	}
	
	@RequestMapping(value="/contact", method = RequestMethod.POST)
	public String contactPost(@ModelAttribute("contact") Contact contact, Model model) {
		SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
        //Do empty validation here
        contactRepository.save(contact);
        model.addAttribute("contact",contact);
        model.addAttribute("success",true);
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
