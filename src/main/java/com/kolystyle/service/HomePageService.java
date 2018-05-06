package com.kolystyle.service;

import java.util.List;

import com.kolystyle.domain.HomePage;

public interface HomePageService {
	HomePage updateHomePage(HomePage homePage);
	HomePage findOne(Long id);
	List<HomePage> findAll();
	void save(HomePage homePage);
}
