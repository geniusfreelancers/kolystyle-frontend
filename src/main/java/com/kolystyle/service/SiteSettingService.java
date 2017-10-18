package com.kolystyle.service;

import java.util.List;

import com.kolystyle.domain.SiteSetting;

public interface SiteSettingService {
	SiteSetting updateSiteSetting(SiteSetting siteSetting);
	SiteSetting findOne(Long id);
	List<SiteSetting> findAll();
}
