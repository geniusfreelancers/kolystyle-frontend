package com.kolystyle.repository;

import org.springframework.data.repository.CrudRepository;

import com.kolystyle.domain.security.Role;

public interface RoleRepository extends CrudRepository<Role, Long> {
	
	Role findByname(String name);
}
