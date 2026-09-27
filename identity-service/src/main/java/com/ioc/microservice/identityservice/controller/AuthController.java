package com.ioc.microservice.identityservice.controller;

import com.ioc.microservice.identityservice.dto.*;
import com.ioc.microservice.identityservice.entity.RefreshToken;
import com.ioc.microservice.identityservice.entity.User;
import com.ioc.microservice.identityservice.repository.UserRepository;
import com.ioc.microservice.identityservice.service.RefreshTokenService;
import com.ioc.microservice.identityservice.utils.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtils jwtUtils;

    public AuthController(UserRepository userRepository,
                          RefreshTokenService refreshTokenService,
                          JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Error: User not found!"));

        if (!user.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.badRequest().body("Error: Invalid password!");
        }

        String accessToken = jwtUtils.generateAccessToken(user.getUsername(), user.getRole());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken.getToken()));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String newAccessToken = jwtUtils.generateAccessToken(user.getUsername(),user.getRole());
                    return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, requestRefreshToken));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refreshToken_v2(@RequestBody RefreshTokenRequest request) {
        TokenResponseDTO tokenResponse = refreshTokenService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(tokenResponse);
    }
}