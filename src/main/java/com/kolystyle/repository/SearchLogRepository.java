package com.kolystyle.repository;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import com.kolystyle.domain.SearchLog;

@Transactional
public interface SearchLogRepository extends CrudRepository<SearchLog, Long> {
	

	List<SearchLog> findAllByOrderByIdDesc();
	
}
