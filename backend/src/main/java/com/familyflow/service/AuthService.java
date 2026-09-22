package com.familyflow.service;

import com.familyflow.api.ApiDtos.*;
import com.familyflow.domain.Models.StoredUser;
import com.familyflow.infrastructure.JsonStore;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final JsonStore store;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    public AuthService(JsonStore store) { this.store = store; }

    public AuthResponse login(LoginRequest request) {
        List<StoredUser> users = store.read("users.json", StoredUser.class);
        StoredUser user = users.stream().filter(item -> item.userName.equalsIgnoreCase(request.userName())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));
        boolean legacy = user.password != null && !user.password.startsWith("$2");
        boolean matches = legacy ? user.password.equals(request.password()) : encoder.matches(request.password(), user.password);
        if (!matches) throw new IllegalArgumentException("Invalid username or password.");
        if (legacy) { user.password = encoder.encode(request.password()); store.write("users.json", users); }
        return session(user);
    }

    public AuthResponse register(RegisterRequest request) {
        List<StoredUser> users = store.read("users.json", StoredUser.class);
        if (users.stream().anyMatch(user -> user.userName.equalsIgnoreCase(request.userName())))
            throw new IllegalArgumentException("That username is already in use.");
        String familyId;
        if ("parent".equals(request.userType())) familyId = UUID.randomUUID().toString();
        else {
            familyId = request.familyGroupId() == null ? "" : request.familyGroupId().trim();
            if (familyId.isBlank() || users.stream().noneMatch(user -> familyId.equals(user.familyGroupId)))
                throw new IllegalArgumentException("Enter an existing family group ID.");
        }
        StoredUser user = new StoredUser();
        user.userId = UUID.randomUUID().toString(); user.userName = request.userName().trim();
        user.password = encoder.encode(request.password()); user.userType = request.userType(); user.familyGroupId = familyId;
        users.add(user); store.write("users.json", users);
        return session(user);
    }

    public StoredUser requireUser(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) throw new SecurityException("Sign in required.");
        String userId = sessions.get(authorization.substring(7));
        if (userId == null) throw new SecurityException("Your session has expired. Please sign in again.");
        return store.read("users.json", StoredUser.class).stream().filter(user -> user.userId.equals(userId)).findFirst()
                .orElseThrow(() -> new SecurityException("User no longer exists."));
    }

    public void logout(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) sessions.remove(authorization.substring(7));
    }

    public List<UserView> familyMembers(StoredUser current) {
        return store.read("users.json", StoredUser.class).stream()
                .filter(user -> current.familyGroupId.equals(user.familyGroupId)).map(AuthService::view).toList();
    }

    public static UserView view(StoredUser user) { return new UserView(user.userId, user.userName, user.userType, user.familyGroupId); }

    private AuthResponse session(StoredUser user) {
        String token = UUID.randomUUID().toString(); sessions.put(token, user.userId);
        return new AuthResponse(token, view(user));
    }
}
