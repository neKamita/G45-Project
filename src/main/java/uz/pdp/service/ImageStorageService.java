package uz.pdp.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;

@Service
public class ImageStorageService {
    private static final String DOOR_IMAGES_PREFIX = "doors/";
    
    private final AmazonS3 s3Client;
    
    @Value("${aws.s3.bucket}")
    private String bucketName;

    public ImageStorageService(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }


    public String storeImage(MultipartFile file) throws IOException {
        String fileName = DOOR_IMAGES_PREFIX + UUID.randomUUID() + "_" + file.getOriginalFilename();
        
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            
            s3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);
            
            return s3Client.getUrl(bucketName, fileName).toString();
        } catch (AmazonServiceException e) {
            throw new IOException("Failed to upload file to S3", e);
        }
    }

    public void deleteImage(String imageUrl) throws IOException {
        String key = imageUrl.substring(imageUrl.indexOf(DOOR_IMAGES_PREFIX));
        try {
            s3Client.deleteObject(bucketName, key);
        } catch (AmazonServiceException e) {
            throw new IOException("Failed to delete file from S3", e);
        }
    }
}