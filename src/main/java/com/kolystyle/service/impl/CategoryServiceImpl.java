package com.kolystyle.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.domain.Category;
import com.kolystyle.domain.SubCategory;
import com.kolystyle.domain.SubSubCategory;
import com.kolystyle.repository.CategoryRepository;
import com.kolystyle.repository.SubCategoryRepository;
import com.kolystyle.repository.SubSubCategoryRepository;
import com.kolystyle.service.CategoryService;

@Service
public class CategoryServiceImpl implements  CategoryService{

	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private SubCategoryRepository subCategoryRepository;
	
	@Autowired
	private SubSubCategoryRepository subSubCategoryRepository;
	
	public Category findCategoryBySlug(String categorySlug) {
		return categoryRepository.findByCategorySlug(categorySlug);
	}

	public Category save(Category category) {
		return categoryRepository.save(category);
	}
	public List<SubCategory> findSubCategoryBySlug(String subCategorySlug) {
		return subCategoryRepository.findBySubCategorySlug(subCategorySlug);
	}
	public SubCategory save(SubCategory subcategory) {
		return subCategoryRepository.save(subcategory);
	}
	
	public SubSubCategory save(SubSubCategory subsubcategory) {
		return subSubCategoryRepository.save(subsubcategory);
	}
	
	public Category findOne(Long id) {
		return categoryRepository.findOne(id);
		
	}
	public SubCategory findOneSub(Long id) {
		return subCategoryRepository.findOne(id);
		
	}
	public SubSubCategory findOneSubSub(Long id) {
		return subSubCategoryRepository.findOne(id);
		
	}
	public List<Category> findAllCategories(){
		return (List<Category>) categoryRepository.findAll();
	}
	
	public List<SubCategory> findAllSubCategories(){
		return (List<SubCategory>) subCategoryRepository.findAll();
	}
	
	public List<SubSubCategory> findAllSubSubCategories(){
		return (List<SubSubCategory>) subSubCategoryRepository.findAll();
	}
	
	public List<SubCategory> findAllSubCategoriesByCategory(Category category){
		return (List<SubCategory>) subCategoryRepository.findByCategory(category);
	}
	
	public List<SubSubCategory> findAllSubSubCategoriesBySubCategory(SubCategory subCategory){
		return (List<SubSubCategory>) subSubCategoryRepository.findBySubCategory(subCategory);
	}

	@Override
	public List<SubSubCategory> findSubSubCategoryBySlug(String mainSubCategory) {
		return subSubCategoryRepository.findBySubSubCategorySlug(mainSubCategory);
	}
	
	
}
