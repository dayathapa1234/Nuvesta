package com.nuvesta.user_service.service;

import com.nuvesta.user_service.model.UserAccount;
import com.nuvesta.user_service.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    public UserAccount getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)){
            throw new IllegalStateException("No authenticated user available");
        }
        return principal.user();
    }
}
