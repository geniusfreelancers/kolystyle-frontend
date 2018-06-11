package com.kolystyle.service;

import java.util.List;

import com.kolystyle.domain.Category;
import com.kolystyle.domain.SubCategory;
import com.kolystyle.domain.SubSubCategory;

public interface CategoryService {
	Category save(Category category);
	SubCategory save(SubCategory subcategory);
	SubSubCategory save(SubSubCategory subsubcategory);
	Category findOne(Long id);
	Category findCategoryBySlug(String categorySlug);
	SubCategory findOneSub(Long id);
	SubSubCategory findOneSubSub(Long id);
	List<Category> findAllCategories();
	List<SubCategory> findAllSubCategories();
	List<SubSubCategory> findAllSubSubCategories();
	List<SubCategory> findAllSubCategoriesByCategory(Category category);
	List<SubSubCategory> findAllSubSubCategoriesBySubCategory(SubCategory subCategory);
	List<SubCategory> findSubCategoryBySlug(String subCategorySlug);
	List<SubSubCategory> findSubSubCategoryBySlug(String mainSubCategory);
}
