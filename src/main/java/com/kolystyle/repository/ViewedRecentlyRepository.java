package com.kolystyle.repository;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import com.kolystyle.domain.ViewedRecently;

@Transactional
public interface ViewedRecentlyRepository extends CrudRepository<ViewedRecently, Long> {
	
	
	ViewedRecently findByCookieValueOrderByIdDesc(String cookieValue);
	
	ViewedRecently findByBagIdOrderByIdDesc(String bagId);
	
}
