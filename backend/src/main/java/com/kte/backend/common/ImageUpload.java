package com.kte.backend.common;

/**
 * Framework-neutral representation of an image being uploaded.
 * <p>
 * Lets the service and storage layers deal with an uploaded file without
 * depending on {@code org.springframework.web.multipart.MultipartFile}: the web
 * layer adapts its {@code MultipartFile} into this type, so the same code path
 * can be driven from a controller, a batch job or a test with equal ease.
 *
 * @param content     the raw bytes of the image, never {@code null}
 * @param filename    the original client-side file name, may be {@code null}
 * @param contentType the MIME type reported by the client, may be {@code null}
 */
public record ImageUpload(byte[] content, String filename, String contentType) {

    /**
     * @return {@code true} when no bytes are carried and there is nothing to store
     */
    public boolean isEmpty() {
        return content == null || content.length == 0;
    }
}
