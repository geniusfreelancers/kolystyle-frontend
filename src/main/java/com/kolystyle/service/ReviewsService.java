package com.kolystyle.service;

import java.util.List;

import com.kolystyle.domain.Reviews;

public interface ReviewsService {

	List<Reviews> findByStatusOrderByIdDesc(String status);

}
