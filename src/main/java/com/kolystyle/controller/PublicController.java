package com.kolystyle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kolystyle.domain.SiteSetting;
import com.kolystyle.domain.StaticPage;
import com.kolystyle.service.SiteSettingService;
import com.kolystyle.service.StaticPageService;

@Controller
@RequestMapping("/public")
public class PublicController {
	
	@Autowired
	private StaticPageService staticPageService;

	@Autowired
	private SiteSettingService siteSettingService;
	
	@RequestMapping("/contact")
	public String contact(Model model) {
		SiteSetting siteSettings = siteSettingService.findOne(new Long(1));
        model.addAttribute("siteSettings",siteSettings);
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
