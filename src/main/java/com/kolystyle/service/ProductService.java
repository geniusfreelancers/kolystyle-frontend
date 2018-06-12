package com.kolystyle.service;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.kolystyle.domain.Category;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.SubCategory;
import com.kolystyle.domain.SubSubCategory;

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
	List<Product> findAllByProductTagsContaining(String productTags);
	Product findTop1ByProductTagsContaining(String productTags);

	Page<Product> findByBrandByOrderByIdDesc(String brand, PageRequest pageRequest);

	Page<Product> findByCategoryByOrderByIdDesc(Category category, PageRequest pageRequest);

	Page<Product> findByCategoryAndSubCategory(Category category, SubCategory subCategory, PageRequest pageRequest);

	Page<Product> findByCategoryAndSubSubCategory(Category category, SubSubCategory subSubCategory,
			PageRequest pageRequest);
	List<Product> findTop12ByOrderByIdDesc();

	Page<Product> searchByKeyword(String keyword, PageRequest pageRequest);
	

}