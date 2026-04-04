package com.chhavi.firstjobapp.auth;

import org.springframework.security.core.Authentication;

public class AdminUtils {
    public static Long getAdminCompanyId(Authentication auth) {
        if (auth == null) return null;
        Object details = auth.getDetails();
        if (details instanceof Long) return (Long) details;
        if (details instanceof Integer) return ((Integer) details).longValue();
        return null;
    }

    public static boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}