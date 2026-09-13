package com.kte.backend.catalog.internal.storage;

import com.kte.backend.common.ImageUpload;

/**
 * Abstraction over where uploaded files (e.g. product images) actually end up.
 * Swap the implementation (local disk, Cloudinary, S3, ...) without touching callers.
 */
public interface FileStorageService {

    /**
     * Stores the given image and returns a URL usable to retrieve it later.
     *
     * @param image the image to store, never null nor empty (callers are expected to check)
     * @return the publicly accessible URL of the stored file
     */
    String store(ImageUpload image);
}
