package com.kolystyle.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.kolystyle.domain.Category;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.SubCategory;
import com.kolystyle.domain.SubSubCategory;

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
	List<Product> findTop3ByProductTagsContaining(String productTags);
	Product findTop1ByProductTagsContaining(String productTags);
	List<Product> findAllByProductTagsContaining(String productTags);
	
	Page<Product> findByBrandContaining(String brand, Pageable pageable);
	Page<Product> findByCategoryOrderByIdDesc(Category category, Pageable pageable);
	Page<Product> findByCategoryAndSubCategory(Category category, SubCategory subCategory, Pageable pageable);
	Page<Product> findByCategoryAndMainSubCategory(Category category, SubSubCategory mainSubCategory,Pageable pageable);
	List<Product> findTop12ByOrderByIdDesc();
	Page<Product> findByTitleContaining(String keyword, Pageable pageable);
	Page<Product> findBySku(String keyword, Pageable pageable);
	Page<Product> findByDescriptionContaining(String keyword, Pageable pageable);
	List<Product> findTop5ByCategoryOrderByIdDesc(Category category);
	Page<Product> findByProductTagsContaining(String deal, Pageable pageable);
}
