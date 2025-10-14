package com.renter.auth.service;

import com.renter.auth.dto.TokenInfo;
import com.renter.auth.dto.TokenResponse;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.adapters.action.GlobalRequestResult;
import org.keycloak.representations.idm.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.annotation.JsonProperty;

@Service
public class KeycloakService {

    @Autowired
    private Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.client-id}")
    private String clientId;

    private final RestTemplate restTemplate = new RestTemplate();

    public String createUser(String username, String email, String firstName, String lastName, String password) {
        try {
            RealmResource realmResource = keycloak.realm(realm);

            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEnabled(true);
            user.setEmailVerified(true);

            Response response = realmResource.users().create(user);
            System.out.println("Create user response status: " + response.getStatus());

            if (response.getStatus() == 201) {
                String userId = extractUserIdFromResponse(response);
                System.out.println("User ID extracted: " + userId);
                setUserPassword(userId, password);
                System.out.println("Password set successfully");
                return userId;
            } else {
                throw new RuntimeException("Failed to create user. Status: " + response.getStatus());
            }

        } catch (Exception e) {
            throw new RuntimeException("Error creating user: " + e.getMessage(), e);
        }
    }

    public String authenticateUser(String username, String password) {
        try {
            String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            // Prepare form data for token request
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", OAuth2Constants.PASSWORD); // Use PASSWORD grant type for user authentication
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("username", username);
            formData.add("password", password);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

            // Make the token request
            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                    tokenUrl,
                    request,
                    TokenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                TokenResponse tokenResponse = response.getBody();
                return tokenResponse.getAccessToken();
            } else {
                throw new RuntimeException("Authentication failed: Invalid response");
            }

        } catch (HttpClientErrorException e) {
            // Handle specific HTTP errors
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new RuntimeException("Invalid credentials");
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new RuntimeException("Bad request - check client configuration");
            } else {
                throw new RuntimeException("Authentication failed: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Authentication error: " + e.getMessage(), e);
        }
    }

    public TokenInfo getTokenInfo(String accessToken) {
        try {
            String tokenInfoUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/userinfo";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenInfoUrl,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> userInfo = response.getBody();
                return new TokenInfo(
                        (String) userInfo.get("sub"), // Keycloak user ID
                        (String) userInfo.get("email"),
                        (String) userInfo.get("given_name"),
                        (String) userInfo.get("family_name"),
                        (String) userInfo.get("preferred_username")
                );
            } else {
                throw new RuntimeException("Failed to get token info");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error getting token info: " + e.getMessage(), e);
        }
    }

    public boolean validateToken(String accessToken) {
        try {
            getTokenInfo(accessToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void setUserPassword(String userId, String password) {
        RealmResource realmResource = keycloak.realm(realm);
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        realmResource.users().get(userId).resetPassword(credential);
    }

    private String extractUserIdFromResponse(Response response) {
        String location = response.getHeaderString("Location");
        return location.substring(location.lastIndexOf('/') + 1);
    }


    public void deleteUser(String keycloakUserId) {
        try {
            RealmResource realmResource = keycloak.realm(realm);

            realmResource.users().get(keycloakUserId).remove();

        } catch (Exception e) {
            // Log properly instead of ignoring
            System.err.println("Failed to delete Keycloak user " + keycloakUserId + ": " + e.getMessage());
        }
    }


}