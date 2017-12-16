package com.kolystyle.repository;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import com.kolystyle.domain.Order;
import com.kolystyle.domain.OrderLog;

@Transactional
public interface OrderLogRepository extends CrudRepository<OrderLog, Long> {
	List<OrderLog> findByOrder(Order order);

	List<OrderLog> findAllByOrderByIdDesc();

	List<OrderLog> findByOrderOrderByIdDesc(Order order);
	
}
