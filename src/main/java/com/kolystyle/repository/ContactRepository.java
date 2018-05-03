package com.kolystyle.repository;

import org.springframework.data.repository.CrudRepository;

import com.kolystyle.domain.Contact;

public interface ContactRepository extends CrudRepository<Contact, Long>{
	

}

