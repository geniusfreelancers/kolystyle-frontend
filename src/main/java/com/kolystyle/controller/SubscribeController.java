package com.kolystyle.controller;

import java.security.Principal;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kolystyle.domain.Newsletter;
import com.kolystyle.repository.NewsletterRepository;
import com.kolystyle.service.NewsletterService;
import com.kolystyle.service.UserService;

@Controller
@RequestMapping("/newsletter")
public class SubscribeController {
	@Autowired
	private UserService userService;
	
	@Autowired
	private NewsletterService newsletterService;
	
	@Autowired
	private NewsletterRepository newsletterRepository;
	
	//Newsletter Subscription
	@RequestMapping(value="/add", method=RequestMethod.POST)
    public  @ResponseBody  Newsletter addSuscriber(@ModelAttribute("email") String email, Principal principal, Model model){
		Newsletter newsletter;
		if(email != null) {
			newsletter = newsletterService.findByEmail(email);
		}else {
			return null;
		}
		if(newsletter != null) {
				return null;
		}
		Newsletter newnews = new Newsletter();
		newnews.setEmail(email);
		Date enrolledDate = Calendar.getInstance().getTime();
		newnews.setEnrolledDate(enrolledDate);
		newsletterRepository.save(newnews);
		//Send User an confirmation / thankyou email with offers
		
        return newnews;
    }
	
	
	
	//Newsletter Removal or Unsubscription

}

