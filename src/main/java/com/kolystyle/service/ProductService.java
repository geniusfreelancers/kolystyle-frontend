package com.kolystyle.service;


import java.util.List;

import com.kolystyle.domain.Category;
import com.kolystyle.domain.Product;

public interface ProductService {
	
	List<Product> findAll();
	
	Product findOne(Long id);
	List<Product> findAllByOrderByIdDesc();
	List<Product> findByCategory(Category category);
	List<Product> findTop12ByCategory(Category category);
	List<Product> findByBrandByOrderByIdDesc(String brand);
	List<Product> findBySubCategory(Category category, String subCategory, String mainSubCategory);
	List<Product> findByCategoryByOrderByIdDesc(Category category);
	List<Product> blurrySearch(String title);
	List<Product> findTop15ByBrand(String brand);
	List<Product> findTop8ByFeatureOrderByIdDesc(String feature);

	List<Product> findTop6ByProductTagsContaining(String productTags);

	Product findTop1ByProductTagsContaining(String productTags);
}