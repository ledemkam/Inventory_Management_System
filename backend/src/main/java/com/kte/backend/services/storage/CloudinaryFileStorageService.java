package com.kte.backend.services.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryFileStorageService implements FileStorageService {

    private static final String UPLOAD_FOLDER = "products";

    private final Cloudinary cloudinary;

    @Override
    public String store(final MultipartFile file) {
        try {
            final Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", UPLOAD_FOLDER,
                    "resource_type", "image"
            ));
            final String secureUrl = (String) result.get("secure_url");
            log.info("Uploaded image to Cloudinary: {}", secureUrl);
            return secureUrl;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to upload image to Cloudinary", e);
        }
    }
}
