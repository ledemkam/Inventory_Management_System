package com.kte.backend.services.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction over where uploaded files (e.g. product images) actually end up.
 * Swap the implementation (local disk, Cloudinary, S3, ...) without touching callers.
 */
public interface FileStorageService {

    /**
     * Stores the given file and returns a URL usable to retrieve it later.
     *
     * @param file the file to store, never null nor empty (callers are expected to check)
     * @return the publicly accessible URL of the stored file
     */
    String store(MultipartFile file);
}
