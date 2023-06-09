package com.dentflow.auth.service;

import com.dentflow.auth.model.AuthenticationRequest;
import com.dentflow.auth.model.AuthenticationResponses;
import com.dentflow.auth.model.RegisterRequest;
import com.dentflow.config.jwt.JwtService;
import com.dentflow.user.model.User;
import com.dentflow.user.model.UserRepository;
import com.dentflow.user.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private JavaMailSender javaMailSender;

    public AuthenticationResponses register(RegisterRequest request) {
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Collections.singleton(Role.USER))
                .build();
        repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponses.builder()
                .token(jwtToken)
                .email(request.getEmail())
                .roles(user.getRoles())
                .build();
    }

    public void registerOwner(RegisterRequest request){
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(Role.OWNER, Role.USER))
                .build();
        repository.save(user);
    }

    public AuthenticationResponses authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponses.builder()
                .token(jwtToken)
                .email(request.getEmail())
                .roles(user.getRoles())
                .build();
    }
}
