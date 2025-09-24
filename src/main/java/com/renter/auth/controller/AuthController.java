package com.renter.auth.controller;

import com.renter.auth.dto.LoginRequest;
import com.renter.auth.dto.RegisterRequest;
import com.renter.auth.service.KeycloakService;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private KeycloakService keycloakService;


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){

        try {
            String user_id = keycloakService.createUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPassword()
            );
            return ResponseEntity.ok("User created successfully with ID: "+ user_id);
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Registration failed: "+ e.getMessage());
        }

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // TODO: Implement login logic
        return ResponseEntity.ok("Login endpoint - to be implemented");
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("Auth Service is running!");
    }


}
