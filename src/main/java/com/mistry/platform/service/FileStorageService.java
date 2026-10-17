package com.mistry.platform.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {

    @Autowired
    private Cloudinary cloudinary;

    public String store(MultipartFile file, String label) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "mistry/service-providers",
                            "public_id", label + "_" + UUID.randomUUID(),
                            "resource_type", "auto",
                            "type", "authenticated"
                    )
            );
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + label, e);
        }
    }
}