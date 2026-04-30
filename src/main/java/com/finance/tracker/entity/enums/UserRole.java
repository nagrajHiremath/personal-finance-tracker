package com.finance.tracker.entity.enums;

import org.springframework.security.core.GrantedAuthority;

/**
 * Enumeration of user roles in the system.
 * Maps to Spring Security GrantedAuthority.
 */
public enum UserRole implements GrantedAuthority {
    USER("ROLE_USER", "Regular user with basic permissions"),
    ADMIN("ROLE_ADMIN", "Administrator with full permissions");

    private final String authority;
    private final String description;

    UserRole(String authority, String description) {
        this.authority = authority;
        this.description = description;
    }

    @Override
    public String getAuthority() {
        return this.authority;
    }

    public String getDescription() {
        return this.description;
    }

    /**
     * Get role from authority string (e.g., "ROLE_USER" -> UserRole.USER)
     */
    public static UserRole fromAuthority(String authority) {
        for (UserRole role : UserRole.values()) {
            if (role.authority.equals(authority)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown authority: " + authority);
    }
}
