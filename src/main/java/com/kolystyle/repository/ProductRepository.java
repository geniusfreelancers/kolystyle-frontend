package com.kolystyle.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.kolystyle.domain.Category;
import com.kolystyle.domain.Product;

public interface ProductRepository extends CrudRepository<Product, Long> {

	List<Product> findByCategory(Category category);
	/*List<Product> findBySubCategory(String subCategory);
	List<Product> findByMainSubCategory(String mainSubCategory);*/
	List<Product> findAllByOrderByIdDesc();
	List<Product> findByCategoryOrderByIdDesc(Category category);
	List<Product> findByBrandOrderByIdDesc(String brand);
	List<Product> findByTitleContaining(String title);

	List<Product> findTop12ByCategoryOrderByIdDesc(Category category);
	List<Product> findBySku(String sku);

	List<Product> findByBrandContaining(String brand);
	
	List<Product> findTop15ByBrandOrderByIdDesc(String brand);
	List<Product> findTop8ByFeatureOrderByIdDesc(String feature);
	@Query("select p from Product p where p.title like ?1 or p.sku like ?1 or p.description like ?1")
	List<Product> search(String query);
}
