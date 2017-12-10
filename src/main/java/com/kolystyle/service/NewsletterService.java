package com.kolystyle.service;

import com.kolystyle.domain.Newsletter;

public interface NewsletterService {

	Newsletter findByEmail(String email);
}
