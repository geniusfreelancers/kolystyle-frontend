package com.kolystyle.repository;

import org.springframework.data.repository.CrudRepository;

import com.kolystyle.domain.Category;
import com.kolystyle.domain.StaticPage;

public interface StaticPageRepository extends CrudRepository<StaticPage, Long>{

	StaticPage findByTitle(String title);
	
	StaticPage findByPagename(String pagename);

}
