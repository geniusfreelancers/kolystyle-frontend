package com.kolystyle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.domain.StaticPage;
import com.kolystyle.repository.StaticPageRepository;
import com.kolystyle.service.StaticPageService;

@Service
public class StaticPageServiceImpl implements StaticPageService{

	@Autowired
	private StaticPageRepository staticPageRepository;
	
	public StaticPage getPageByTitle(String title) {
		return staticPageRepository.findByTitle(title);
	}
}
