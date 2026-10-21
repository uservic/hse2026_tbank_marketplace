package com.marketplace.auth.controller;

import com.marketplace.auth.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        //  Проверяем логин и пароль через Spring Security (in-memory)
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // Достаем роль (Spring Security хранит её как ROLE_SELLER, отрезаем префикс)
        String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        String accessToken = jwtUtil.generateAccessToken(auth.getName(), role);
        String refreshToken = jwtUtil.generateRefreshToken(auth.getName());

        return ResponseEntity.ok(Map.of("accessToken", accessToken, "refreshToken", refreshToken));
    }

    public record LoginRequest(String username, String password) {}

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        try {
            Claims claims = jwtUtil.parseToken(refreshToken);
            if (!"refresh".equals(claims.get("type"))) {
                return ResponseEntity.status(401).body(Map.of("error_code", "REFRESH_TOKEN_INVALID"));
            }
            String newAccessToken = jwtUtil.generateAccessToken(claims.getSubject(), "USER"); // тут можно достать роль
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error_code", "REFRESH_TOKEN_INVALID"));
        }
    }
}
