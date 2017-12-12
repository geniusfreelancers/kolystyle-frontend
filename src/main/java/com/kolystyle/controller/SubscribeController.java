package com.kolystyle.controller;

import java.security.Principal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.braintreegateway.Transaction;
import com.kolystyle.domain.Newsletter;
import com.kolystyle.domain.Order;
import com.kolystyle.repository.NewsletterRepository;
import com.kolystyle.service.NewsletterService;
import com.kolystyle.service.UserService;
import com.kolystyle.utility.USConstants;

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
		if(newsletter != null && newsletter.isActive()) {
				return null;
		}else {
			Newsletter newnews;
			String verifyToken;
			if(newsletter != null && !newsletter.isActive()) {
				 newnews = newsletter;
				 newnews.setActive(true);
				 verifyToken = newsletter.getVerifyToken();
				 
			}else {
				 newnews = new Newsletter();
				 verifyToken = USConstants.randomAlphaNumeric(10);
			}
			
			newnews.setEmail(email);
			Date enrolledDate = Calendar.getInstance().getTime();
			newnews.setEnrolledDate(enrolledDate);
			newnews.setVerifyToken(verifyToken);
			newsletterRepository.save(newnews);
			//Send User an confirmation / thankyou email with offers
			//This is the url for unsubscribe (/newsletter/remove/{email}/{token})
	        return newnews;
		}
    }
	
	
	
	//Newsletter Removal or Unsubscription GET
	@RequestMapping(value="/remove/{email}/{token}", method=RequestMethod.GET)
	public String removeSubscriber(@PathVariable String email,@PathVariable String token, Model model) {
		Newsletter newsletter = newsletterService.findByEmail(email);
		if(newsletter != null && token.length() == 10) {
			if(newsletter.getVerifyToken().equalsIgnoreCase(token) && newsletter.isActive()) {
				model.addAttribute("newsletter",newsletter);
				model.addAttribute("token",true);
				model.addAttribute("forged",false);
			}else {
				model.addAttribute("forged",true);
				model.addAttribute("token",false);
			}
		}else {
			model.addAttribute("forged",true);
			model.addAttribute("token",false);
		}
		
		return "unsubscribe";
	}
	
	//Newsletter Removal or Unsubscription POST
	@RequestMapping(value="/remove/{email}/{token}", method=RequestMethod.POST)
	public String removeSubscriberPOST(@ModelAttribute("reason") String reason,@ModelAttribute("otherreason") String otherreason,@PathVariable String email,@PathVariable String token, Model model) {
		Newsletter newsletter = newsletterService.findByEmail(email);
		if(newsletter != null) {
			if(newsletter.getVerifyToken().equalsIgnoreCase(token)) {
				if(reason.equalsIgnoreCase("f")) {
					reason = otherreason;
				}
				newsletter.setReason(reason);
				Date unenrolledDate = Calendar.getInstance().getTime();
				newsletter.setUnenrolledDate(unenrolledDate);
				newsletter.setActive(false);
				newsletterRepository.save(newsletter);
				return "redirect:/newsletter/removed/" +email+"/"+token;  
				
			}else {
				model.addAttribute("removed",false);
				model.addAttribute("token",false);
				model.addAttribute("forged",true);
			}
		}else {
			model.addAttribute("removed",false);
			model.addAttribute("token",false);
			model.addAttribute("forged",true);
		}
		   return "unsubscribe";        
		
	}
	
	 @RequestMapping(value = "/removed/{email}/{token}")
	 public String getRemoved(@PathVariable String email, @PathVariable String token, Model model) {
		 	Newsletter newsletter;
	       try {
	    	   newsletter = newsletterService.findByEmail(email);
	       } catch (Exception e) {
	           System.out.println("Exception: " + e);
	           return "badRequestPage";
	       }
	       	model.addAttribute("newsletter",newsletter);
			model.addAttribute("removed",true);
			model.addAttribute("forged",false);
			model.addAttribute("token",false);
	       return "unsubscribe";
	 }     
}

