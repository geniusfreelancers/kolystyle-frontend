package com.kolystyle.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.kolystyle.domain.Category;
import com.kolystyle.domain.Product;
import com.kolystyle.domain.SubCategory;
import com.kolystyle.domain.SubSubCategory;
import com.kolystyle.repository.ProductRepository;
import com.kolystyle.service.CategoryService;
import com.kolystyle.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService{

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CategoryService categoryService;
	
	public List<Product> findAll(){
		List<Product> productList = (List<Product>) productRepository.findAll();
		List<Product> activeProductList = new ArrayList<>();
		
		for(Product product: productList){
			if(product.isActive()){
				activeProductList.add(product);
			}
		}
		return activeProductList;
	}
	
	
	public Product findOne(Long id){
		return productRepository.findOne(id);
	}
	
	public List<Product> findTop12ByCategory(Category category){
		return productRepository.findTop12ByCategoryOrderByIdDesc(category);
	}
	public List<Product> findTop5ByCategory(Category category){
		return productRepository.findTop5ByCategoryOrderByIdDesc(category);
	}
	public List<Product> findTop15ByBrand(String brand){
		return productRepository.findTop15ByBrandOrderByIdDesc(brand);
	}
	
	public List<Product> findTop8ByFeatureOrderByIdDesc(String feature){
		return productRepository.findTop8ByFeatureOrderByIdDesc(feature);
	}
	
	public List<Product> findTop12ByOrderByIdDesc(){
		return productRepository.findTop12ByOrderByIdDesc();
	}
	public List<Product> findByCategory(Category category){
		List<Product> productList = productRepository.findByCategory(category);
		
		List<Product> activeProductList = new ArrayList<>();
		
		for(Product product: productList){
			if(product.isActive()){
				activeProductList.add(product);
			}
		}
		
		return activeProductList;
	}
	
	
	public List<Product> findBySubCategory(Category category, String subCategory, String mainSubCategory){
		List<Product> productList = productRepository.findByCategory(category);
		/*List<Product> productSubList = productRepository.findBySubCategory(subCategory);
		List<Product> producMaintList = productRepository.findByMainSubCategory(mainSubCategory);*/
		
		List<Product> activeProductList = new ArrayList<>();
		
		for(Product product: productList){
			if(product.isActive()){
				activeProductList.add(product);
			}
		}
		
		List<Product> validProductList = new ArrayList<>();
		
		for(Product product: activeProductList){
			String subCat = product.getSubCategory().getSubCategorySlug();
			String submainCat = product.getMainSubCategory().getSubSubCategorySlug();
			
				if(subCategory == "" || subCategory.isEmpty() || subCategory.equalsIgnoreCase("none") || subCategory == null) {
					validProductList.add(product);
				}else{
					if(subCat != null) {
						if(subCat.equalsIgnoreCase(subCategory)){
							if(mainSubCategory == "" || mainSubCategory.isEmpty() || mainSubCategory.equalsIgnoreCase("none") || mainSubCategory == null) {
								validProductList.add(product);
							}else{
								if(submainCat != null) {
									if(submainCat.equalsIgnoreCase(mainSubCategory)) {
										validProductList.add(product);
									}
								}
							} 
						}
					}
					
				}	
			}

		return validProductList;
	}
	
	public List<Product> blurrySearch(String title){
		List<Product> productList = productRepository.findByTitleContaining(title);
		if (productList.size() == 0){
			productList = productRepository.findBySku(title);
		}
		if (productList.size() == 0){
			productList = productRepository.findByBrandContaining(title);
		}
		List<Product> activeProductList = new ArrayList<>();
		
		for(Product product: productList){
			if(product.isActive()){
				activeProductList.add(product);
			}
		}
			
		
		return activeProductList;
	}
	
	public List<Product> findAllByOrderByIdDesc(){
		return productRepository.findAllByOrderByIdDesc();
	}
	
	public List<Product> findByCategoryByOrderByIdDesc(Category category){
		return productRepository.findByCategoryOrderByIdDesc(category);
	}
	
	public List<Product> findByBrandByOrderByIdDesc(String brand){
		return productRepository.findByBrandOrderByIdDesc(brand);
	}
	


	
	@Override
	public List<Product> findTop6ByProductTagsContaining(String productTags) {
		return productRepository.findTop3ByProductTagsContaining(productTags);
	}


	@Override
	public Product findTop1ByProductTagsContaining(String productTags) {
		return productRepository.findTop1ByProductTagsContaining(productTags);
	}


	@Override
	public List<Product> findAllByProductTagsContaining(String productTags) {
		return productRepository.findAllByProductTagsContaining(productTags);
	}


	@Override
	public Page<Product> findByBrandByOrderByIdDesc(String brand, PageRequest pageRequest) {
		return productRepository.findByBrandContaining(brand, pageRequest);
	}

	public Page<Product> findByCategoryByOrderByIdDesc(Category category, PageRequest pageRequest) {
		return productRepository.findByCategoryOrderByIdDesc(category,pageRequest);
	}


	public Page<Product> findByCategoryAndSubCategory(Category category, SubCategory subCategory, PageRequest pageRequest) {
		return productRepository.findByCategoryAndSubCategory(category, subCategory,pageRequest);
	}


	@Override
	public Page<Product> findByCategoryAndSubSubCategory(Category category, SubSubCategory subSubCategory,
			PageRequest pageRequest) {
		return productRepository.findByCategoryAndMainSubCategory(category, subSubCategory,pageRequest);
	}


	@Override
	public Page<Product> searchByKeyword(String keyword, PageRequest pageRequest) {
		Page<Product> productList = productRepository.findByTitleContaining(keyword,pageRequest);
		if (productList.hasContent() != true){
			productList = productRepository.findBySku(keyword,pageRequest);
		}
		if (productList.hasContent() != true){
			productList = productRepository.findByBrandContaining(keyword,pageRequest);
		}
		if (productList.hasContent() != true){
			productList = productRepository.findByDescriptionContaining(keyword,pageRequest);
		}
		if (productList.hasContent() != true){
			productList = productRepository.findByCategoryOrderByIdDesc(categoryService.findCategoryBySlug(keyword),pageRequest);
		}
		
		return productList;
	}

}
