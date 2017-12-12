package com.kolystyle.repository;

import org.springframework.data.repository.CrudRepository;

import com.kolystyle.domain.Newsletter;

public interface NewsletterRepository extends CrudRepository<Newsletter, Long>{

	Newsletter findByEmail(String email);
}
