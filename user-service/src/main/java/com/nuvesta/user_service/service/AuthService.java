package com.nuvesta.user_service.service;


import com.nuvesta.user_service.dto.AuthResponse;
import com.nuvesta.user_service.dto.RegisterRequest;
import com.nuvesta.user_service.dto.UserSummary;
import com.nuvesta.user_service.model.Role;
import com.nuvesta.user_service.model.UserAccount;
import com.nuvesta.user_service.repository.UserRepository;
import com.nuvesta.user_service.security.JwtService;
import com.nuvesta.user_service.security.UserPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }


    @Transactional
    public AuthResponse register(RegisterRequest request){
        String normalisedEmail = request.email().toLowerCase();
        if (userRepository.existsByEmail(normalisedEmail)){
            throw new IllegalArgumentException("Email already registered");
        }

        UserAccount user = UserAccount.builder()
                .email(normalisedEmail)
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .roles(new HashSet<>())
                .build();

        user.getRoles().add(Role.USER);
        UserAccount saved = userRepository.save(user);

        return buildAuthResponse(saved);
    }

    private AuthResponse buildAuthResponse(UserAccount user){
        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal);
        UserSummary summary = new UserSummary(user.getId(), user.getEmail(), user.getFullName());
        return new AuthResponse(token, summary);
    }
}
