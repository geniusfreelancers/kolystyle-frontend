package com.kolystyle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kolystyle.domain.StaticPage;
import com.kolystyle.service.StaticPageService;

@Controller
@RequestMapping("/public")
public class PublicController {
	
	@Autowired
	private StaticPageService staticPageService;

	@RequestMapping("/contact")
	public String contact() {
		return "contact";
	}
	
	@RequestMapping("/{pagename}")
	public String pages(@PathVariable String pagename, Model model)
	{
		StaticPage staticpage = staticPageService.findByPagename(pagename);
		if(staticpage != null) {
			model.addAttribute("staticpage", staticpage);
			return "staticpage";
		}
		
		return "badRequestPage";
	}
	
}
