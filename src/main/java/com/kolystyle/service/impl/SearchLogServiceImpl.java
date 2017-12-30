package com.kolystyle.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.domain.SearchLog;
import com.kolystyle.repository.SearchLogRepository;
import com.kolystyle.service.SearchLogService;

@Service
public class SearchLogServiceImpl implements SearchLogService {
	@Autowired
	private SearchLogRepository searchLogRepository;

	
	public List<SearchLog> findAll() {
		return (List<SearchLog>) searchLogRepository.findAll();
	}

	public List<SearchLog> findAllByOrderByIdDesc() {
		return searchLogRepository.findAllByOrderByIdDesc();
	}


}
