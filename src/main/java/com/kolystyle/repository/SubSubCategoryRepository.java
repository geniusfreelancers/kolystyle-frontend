package com.kolystyle.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.kolystyle.domain.SubCategory;
import com.kolystyle.domain.SubSubCategory;

public interface SubSubCategoryRepository extends CrudRepository<SubSubCategory, Long>{

	List<SubSubCategory> findBySubCategory(SubCategory subCategory);

}
