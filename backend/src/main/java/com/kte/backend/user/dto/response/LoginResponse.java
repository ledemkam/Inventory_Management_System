package com.kte.backend.user.dto.response;

import lombok.Builder;

@Builder
public record LoginResponse(
        String accessToken,
        String tokenType
) {
}
