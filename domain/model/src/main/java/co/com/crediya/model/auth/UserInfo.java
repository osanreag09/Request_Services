package co.com.crediya.model.auth;

import lombok.Builder;

@Builder
public record UserInfo(
        Long id,
        String email,
        String fullName,
        String role
) {}