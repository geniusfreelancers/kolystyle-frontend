package com.kolystyle.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.kolystyle.service.UserService;

@Controller
@RequestMapping("/newsletter")
public class SubscribeController {
	@Autowired
	private UserService userService;
	
	@RequestMapping(value="/add", method=RequestMethod.POST)
    public String addSuscriber(@ModelAttribute("email") String email, Principal principal, Model model){

        return "success";
    }

}

