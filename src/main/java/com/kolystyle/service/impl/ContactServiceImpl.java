package com.kolystyle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kolystyle.repository.ContactRepository;
import com.kolystyle.service.ContactService;
@Service
public class ContactServiceImpl implements ContactService{
	@Autowired
	private ContactRepository contactRepository;
	
}
