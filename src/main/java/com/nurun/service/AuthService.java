package com.nurun.service;

import com.nurun.dto.LoginResponseDto;
import com.nurun.dto.RegisterResponseDto;
import com.nurun.dto.LoginRequestDto;
import com.nurun.dto.RegisterRequestDto;
import com.nurun.exception.AlreadyExistsException;
import com.nurun.model.User;
import com.nurun.repository.UserRepository;
import com.nurun.security.JwtService;

import com.nurun.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;



    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }


    public RegisterResponseDto register(RegisterRequestDto registerRequestDto) {

        if (userRepository.existsByEmail(registerRequestDto.getEmail())) {
            throw new AlreadyExistsException("User already exists");
        }

        User user = new User();
        user.setEmail(registerRequestDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequestDto.getPassword()));
        user.setCreatedAt(Instant.now());

        String defaultName = registerRequestDto.getEmail().split("@")[0];
        user.setDisplayName(defaultName);

        User savedUser = userRepository.save(user);

        UserPrincipal userPrincipal = new UserPrincipal(savedUser);

        String token = jwtService.generateToken(userPrincipal.getId());

        return RegisterResponseDto.builder()
                .email(savedUser.getEmail())
                .token(token)
                .displayName(savedUser.getDisplayName())
                .avatarUrl(savedUser.getAvatarUrl())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {

        var authentication = authenticationManager.authenticate(

                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();


        String token = jwtService.generateToken(userPrincipal.getId());

        return LoginResponseDto.builder()
                .email(userPrincipal.getUsername())
                .token(token)
                .build();
    }

}
