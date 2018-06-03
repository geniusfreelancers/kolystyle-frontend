package com.kolystyle.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kolystyle.domain.HomePage;
import com.kolystyle.domain.HomePageAdditional;
import com.kolystyle.repository.HomePageAdditionalRepository;
import com.kolystyle.repository.HomePageRepository;
import com.kolystyle.service.HomePageService;
@Service
public class HomePageServiceImpl implements HomePageService{

	@Autowired
	private HomePageRepository homePageRepository;
	
	@Autowired
	private HomePageAdditionalRepository homePageAdditionalRepository;
	
	public HomePage updateHomePage(HomePage homePage) {
		return homePageRepository.save(homePage);	
	}
	
	public HomePage findOne(Long id) {
		return homePageRepository.findOne(id);	
	}
	
	public List<HomePage> findAll() {
		return (List<HomePage>) homePageRepository.findAll();	
	}

	public void save(HomePage homePage) {
		homePageRepository.save(homePage);
		
	}
	@Override
	public HomePageAdditional findAdditionalHomePage(Long id) {
		
		return homePageAdditionalRepository.findOne(id);
	}
	
	public void saveAdditionalHomePage(HomePageAdditional homePageAdditional) {
		homePageAdditionalRepository.save(homePageAdditional);
	}

}
