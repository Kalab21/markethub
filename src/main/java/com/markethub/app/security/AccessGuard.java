package com.markethub.app.security;

import com.markethub.app.model.User;
import com.markethub.app.service.imp.UserDetailsServiceImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resource-level authorization checks used by the controllers.
 *
 * URL rules in {@code FormLoginSecurityConfig} decide which role may reach an endpoint. This class
 * decides whether the signed-in user may act on a specific record: the order, cart or product they
 * own. Admins keep authority over every record. Every refusal is an {@link AccessDeniedException},
 * which the global handler turns into a 403.
 */
@Component
public class AccessGuard {

    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    private final UserDetailsServiceImpl userDetailsService;

    public AccessGuard(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public User currentUser() {
        return userDetailsService.getCurrentUser();
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> ADMIN_AUTHORITY.equals(authority.getAuthority()));
    }

    /** The caller must be the user identified by {@code userId}, or an admin. */
    public void requireSelfOrAdmin(Long userId) {
        if (isAdmin()) {
            return;
        }
        if (userId == null || !userId.equals(currentUser().getUserId())) {
            throw new AccessDeniedException("You can only access your own records");
        }
    }

    /** The caller must own the record (its {@code owner} is the caller), or be an admin. */
    public void requireOwnerOrAdmin(User owner) {
        if (isAdmin()) {
            return;
        }
        if (owner == null || owner.getUserId() == null
                || !owner.getUserId().equals(currentUser().getUserId())) {
            throw new AccessDeniedException("You can only access your own records");
        }
    }

    /** Product management needs an approved seller account; admins are exempt. */
    public void requireApprovedSellerOrAdmin() {
        if (isAdmin()) {
            return;
        }
        if (!currentUser().isApprovedSeller()) {
            throw new AccessDeniedException("Your seller account has not been approved yet");
        }
    }
}
