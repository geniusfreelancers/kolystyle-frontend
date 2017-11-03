package com.kolystyle.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.kolystyle.domain.Category;
import com.kolystyle.domain.Product;

public interface ProductRepository extends CrudRepository<Product, Long> {

	List<Product> findByCategory(Category category);
	/*List<Product> findBySubCategory(String subCategory);
	List<Product> findByMainSubCategory(String mainSubCategory);*/
	
	List<Product> findByTitleContaining(String title);

	List<Product> findTop12ByCategory(Category category);
}
