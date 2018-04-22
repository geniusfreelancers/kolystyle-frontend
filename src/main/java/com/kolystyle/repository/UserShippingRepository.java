package com.kolystyle.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.kolystyle.domain.UserShipping;

public interface UserShippingRepository extends CrudRepository<UserShipping, Long> {
	List<UserShipping> findByUserId(Long id);
}
