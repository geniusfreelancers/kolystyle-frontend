package com.kolystyle.service;

import org.springframework.web.multipart.MultipartFile;

public interface ContactService {

	String updateImage(String imageName, MultipartFile image);

}
