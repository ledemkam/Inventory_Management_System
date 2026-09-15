package com.kte.backend.common;

import java.util.Arrays;
import java.util.Objects;

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

    // A record's generated equals/hashCode/toString compare array-typed components
    // by reference, not content, so two uploads carrying identical bytes would
    // otherwise be considered different. Override all three to use Arrays.* on
    // "content" instead.

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImageUpload that)) {
            return false;
        }
        return Arrays.equals(content, that.content)
                && Objects.equals(filename, that.filename)
                && Objects.equals(contentType, that.contentType);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(filename, contentType) + Arrays.hashCode(content);
    }

    @Override
    public String toString() {
        // content.length (not Arrays.toString(content)) keeps this readable: an
        // uploaded image is easily several MB, and printing every byte as a
        // comma-separated decimal would flood logs without adding useful information.
        return "ImageUpload[content=" + (content == null ? "null" : content.length + " bytes")
                + ", filename=" + filename
                + ", contentType=" + contentType
                + ']';
    }
}
