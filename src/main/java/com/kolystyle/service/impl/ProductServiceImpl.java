package com.kolystyle.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.domain.Category;
import com.kolystyle.domain.Product;
import com.kolystyle.repository.ProductRepository;
import com.kolystyle.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService{

	@Autowired
	private ProductRepository productRepository;
	
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
		return productRepository.findTop12ByCategory(category);
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
}
