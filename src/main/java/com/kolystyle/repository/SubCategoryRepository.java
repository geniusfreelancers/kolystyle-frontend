package com.kolystyle.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.kolystyle.domain.Category;
import com.kolystyle.domain.SubCategory;

public interface SubCategoryRepository extends CrudRepository<SubCategory, Long>{

	List<SubCategory> findByCategory(Category category);

}
