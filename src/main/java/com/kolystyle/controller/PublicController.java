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

	/*@RequestMapping("/about")
	public String about() {
		return "about";
	}*/
	
	@RequestMapping("/{title}")
	public String product(@PathVariable String title, Model model)
	{
		StaticPage staticpage = staticPageService.getPageByTitle(title);
		model.addAttribute("staticpage", staticpage);
		return "staticpage";
	}
	
}
