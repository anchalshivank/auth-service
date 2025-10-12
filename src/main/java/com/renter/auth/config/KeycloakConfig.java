package com.renter.auth.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig{


    @Value("${keycloak.auth-server-url}")
    private String authServiceUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.admin-user}")
    private String adminUser;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    @Bean
    public Keycloak keycloak() {

        return KeycloakBuilder.builder()
                .serverUrl(authServiceUrl)
                .realm(realm)
                // .username(adminUser)
                // .password(adminPassword)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .build();
    }
}