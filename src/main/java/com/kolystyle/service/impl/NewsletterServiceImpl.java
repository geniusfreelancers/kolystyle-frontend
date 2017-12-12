package com.kolystyle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.domain.Newsletter;
import com.kolystyle.repository.NewsletterRepository;
import com.kolystyle.service.NewsletterService;

@Service
public class NewsletterServiceImpl implements NewsletterService {

	@Autowired
	private NewsletterRepository newsletterRepository;
	
	public Newsletter findByEmail(String email) {
		return newsletterRepository.findByEmail(email);
	
	}
}
