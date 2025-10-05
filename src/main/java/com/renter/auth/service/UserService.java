package com.renter.auth.service;

import com.renter.auth.model.User;
import com.renter.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public User saveUser(User user) throws Exception {
        // Normalize last name - set to "LNU" if empty/null
        if (!StringUtils.hasText(user.getLastName())) {
            user.setLastName("LNU");
        }

        // You could add other business validations here
        validateUser(user);

        return userRepository.save(user);
    }

    public Optional<User> findByKeycloakUserId(String keycloakUserId) {
        Optional<User> user =  userRepository.findByKeycloakUserId(keycloakUserId);

        log.info("-------->>> User {}", user);
        return user;

    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    private void validateUser(User user) {
        if (!StringUtils.hasText(user.getEmail())) {
            throw new IllegalArgumentException("Email is required");
        }

        if (user.getUserType() == null) {
            throw new IllegalArgumentException("User type is required");
        }

        if (!StringUtils.hasText(user.getKeycloakUserId())) {
            throw new IllegalArgumentException("Keycloak user ID is required");
        }

        // Add phone number validation if required
        if (user.getUserType() == User.UserType.tenant &&
                !StringUtils.hasText(user.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number is required for tenants");
        }
    }
}