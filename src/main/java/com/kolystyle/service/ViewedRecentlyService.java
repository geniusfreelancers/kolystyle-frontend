package com.kolystyle.service;

import com.kolystyle.domain.ViewedRecently;

public interface ViewedRecentlyService {
	ViewedRecently findByCookieValue(String cookieValue);
	
	ViewedRecently findByBagId(String bagId);

}
