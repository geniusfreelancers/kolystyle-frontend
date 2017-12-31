package com.kolystyle.service;

import java.util.List;

import com.kolystyle.domain.SearchLog;

public interface SearchLogService {
	List<SearchLog> findAll();
	List<SearchLog> findAllByOrderByIdDesc();

}
