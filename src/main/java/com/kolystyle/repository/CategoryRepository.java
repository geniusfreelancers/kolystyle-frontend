package com.kolystyle.repository;

import org.springframework.data.repository.CrudRepository;

import com.kolystyle.domain.Category;

public interface CategoryRepository extends CrudRepository<Category, Long>{

	Category findByCategorySlug(String categorySlug);

}
