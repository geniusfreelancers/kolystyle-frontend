package com.kolystyle.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.domain.Reviews;
import com.kolystyle.repository.ReviewsRepository;
import com.kolystyle.service.ReviewsService;

@Service
public class ReviewsServiceImpl implements ReviewsService{
	@Autowired
	private ReviewsRepository reviewsRepository;

	@Override
	public List<Reviews> findByStatusOrderByIdDesc(String status) {
		return reviewsRepository.findByStatusOrderByIdDesc(status);
	}
}
