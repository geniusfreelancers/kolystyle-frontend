package com.kolystyle.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.kolystyle.domain.Reviews;

public interface ReviewsRepository  extends CrudRepository<Reviews, Long>{
	
	List<Reviews> findByStatusOrderByIdDesc(String status);

}
