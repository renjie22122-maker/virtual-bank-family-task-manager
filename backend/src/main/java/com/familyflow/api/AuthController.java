package com.familyflow.api;

import com.familyflow.api.ApiDtos.*;
import com.familyflow.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }
    @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest request) { return auth.login(request); }
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) { return auth.register(request); }
    @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(value = "Authorization", required = false) String authorization) { auth.logout(authorization); }
}
