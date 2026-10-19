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
    public String signedUrl(String storedUrl) {
        if (storedUrl == null || !storedUrl.contains("/authenticated/")) {
            return storedUrl;
        }
        String afterType = storedUrl.substring(storedUrl.indexOf("/authenticated/") + "/authenticated/".length());
        String[] parts = afterType.split("/", 2);
        String rest = parts.length == 2 ? parts[1] : parts[0];
        if (rest.startsWith("v") && rest.indexOf('/') > 0) {
            rest = rest.substring(rest.indexOf('/') + 1);
        }
        int dot = rest.lastIndexOf('.');
        String publicId = dot > 0 ? rest.substring(0, dot) : rest;
        String format = dot > 0 ? rest.substring(dot + 1) : "png";

        long expiresAt = (System.currentTimeMillis() / 1000L) + 600;
        try {
            return cloudinary.privateDownload(publicId, format,
                    ObjectUtils.asMap("type", "authenticated", "resource_type", "image", "expires_at", expiresAt));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create signed link", e);
        }
    }
}