package com.kolystyle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kolystyle.service.impl.AmazonClient;
import com.kolystyle.repository.ContactRepository;
import com.kolystyle.service.ContactService;
@Service
public class ContactServiceImpl implements ContactService{
	private AmazonClient amazonClient;
	@Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;
    @Value("${amazonProperties.bucketName}")
    private String bucketName;
    @Value("${amazonProperties.accessKey}")
    private String accessKey;
    @Value("${amazonProperties.secretKey}")
    private String secretKey;
    @Autowired
    ContactServiceImpl(AmazonClient amazonClient) {
        this.amazonClient = amazonClient;
    }
	@Autowired
	private ContactRepository contactRepository;
	
	public String updateImage(String imageName, MultipartFile image) {
		if(image != null && !image.isEmpty()) {
			if(imageName != null) {
				amazonClient.deleteFileFromS3BucketByFilename(imageName);
			}
			
			imageName = amazonClient.uploadFile(image);	
		}
		return imageName;
	}
	
}
