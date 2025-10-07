package com.renter.auth.controller;

import com.renter.auth.dto.LoginRequest;
import com.renter.auth.dto.RegisterRequest;
import com.renter.auth.model.User;
import com.renter.auth.security.CurrentUser;
import com.renter.auth.service.KeycloakService;
import com.renter.auth.service.UserService;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private KeycloakService keycloakService;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        log.info("Registration attempt for email: {}", request.getEmail());

        String keycloakUserId = null;

        try {
            // Step 1: Create user in Keycloak first
            keycloakUserId = keycloakService.createUser(
                    request.getEmail(), // Use email as username
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPassword()
            );
            log.info("KeycloakUserId is {}", keycloakUserId);



            // Step 2: Create business user record linked to Keycloak
            User user = new User(
                    keycloakUserId,
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPhoneNumber(),
                    request.getUserType()
            );
            User savedUser = userService.saveUser(user);

            log.info("Registration successful for email: {} with userId: {}", request.getEmail(), savedUser.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully",
                    "userId", savedUser.getId(),
                    "keycloakUserId", keycloakUserId
            ));

        } catch (Exception e) {
            // If business user creation fails, we should ideally clean up Keycloak user
            // For now, just return error
            log.error("Registration failed for email: {} - Error: {}", request.getEmail(), e.getMessage());
            if (keycloakUserId != null){
                try {
                    keycloakService.deleteUser(keycloakUserId);
                    log.info("Rolled back Keycloak user: {}", keycloakUserId);

                } catch (Exception cleanupEx) {
                    // log but don't override main error
                    log.error("Failed to rollback Keycloak user: {}", cleanupEx.getMessage());

                }
            }

            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }


    //First we do not need this login thing from //let's keep this endpoint hanging
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            
            Optional<User> userOpt = userService.findByEmail(request.getUsername());

            if(!userOpt.isPresent()){
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();

            // Delegate to Keycloak for authentication
            String token = keycloakService.authenticateUser(
                    request.getUsername(),
                    request.getPassword()
            );

            return ResponseEntity.ok(Map.of(
                    "user", user,
                    "token", token,
                    "message", "Login successful"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }

    @GetMapping("/current")
    public ResponseEntity<?> getMyIdentity(@CurrentUser User user){
        return ResponseEntity.ok(user);

    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("Auth Service is running!");
    }
}