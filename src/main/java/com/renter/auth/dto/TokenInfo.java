package com.renter.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


public class TokenInfo {
    private String keycloakUserId;
    private String email;
    private String firstName;
    private String lastName;
    private String username;

    public TokenInfo(String keycloakUserId, String email, String firstName,
                     String lastName, String username) {
        this.keycloakUserId = keycloakUserId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
    }

    // Getters
    public String getKeycloakUserId() { return keycloakUserId; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUsername() { return username; }
}