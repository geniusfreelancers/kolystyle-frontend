package com.kolystyle.repository;

import org.springframework.data.repository.CrudRepository;

import com.kolystyle.domain.ProductAttr;

public interface ProductAttrRepository extends CrudRepository<ProductAttr, Long>{

	//Category findByCategorySlug(String categorySlug);
	

}
