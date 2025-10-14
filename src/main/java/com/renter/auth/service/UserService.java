package com.renter.auth.service;

import com.renter.auth.dto.RegisterRequest;
import com.renter.auth.dto.UserProfileDto;
import com.renter.auth.model.User;
import com.renter.auth.model.UserProfile;
import com.renter.auth.repository.UserProfileRepository;
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

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Transactional
    public User saveUser(RegisterRequest request, String keycloakUserId) throws Exception {
        User user = new User(
            keycloakUserId,
            request.getEmail(),
            request.getFirstName(),
            request.getLastName(),
            request.getPhoneNumber(),
            request.getUserType()
        );

        // Normalize last name - set to "LNU" if empty/null
        if (!StringUtils.hasText(user.getLastName())) {
            user.setLastName("LNU");
        }

        // You could add other business validations here
        validateUser(user);

        UserProfileDto profile = request.getProfile();

       
        User savedUser = userRepository.save(user);
        UserProfile savedProfile = this.saveUserProfile(savedUser, profile);
        return savedUser;
    }

    public Optional<User> findByKeycloakUserId(String keycloakUserId) {
        Optional<User> user =  userRepository.findByKeycloakUserId(keycloakUserId);

        log.info("-------->>> User {}", user);
        return user;

    }

    @Transactional
    public UserProfile saveUserProfile(User savedUser, UserProfileDto dto) {
        // Update profile fields
        // Add other profile fields as needed
        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        profile.setAddressLine1(dto.getAddressLine1());
        profile.setAddressLine2(dto.getAddressLine2());
        profile.setCity(dto.getCity());
        profile.setState(dto.getState());
        profile.setCountry(dto.getCountry());
        profile.setPostalCode(dto.getPostalCode());
        profile.setIdProofNumber(dto.getIdProofNumber());
        profile.setIdProofType(dto.getIdProofType());
        profile.setEmergencyContactName(dto.getEmergencyContactName());
        profile.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        // Validate and save the updated user
        validateUserProfile(profile);
        return userProfileRepository.save(profile);
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

    private void validateUserProfile(UserProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("UserProfile cannot be null");
        }

        // --- Address ---
        if (!StringUtils.hasText(profile.getAddressLine1())) {
            throw new IllegalArgumentException("Address Line 1 is required");
        }
        if (!StringUtils.hasText(profile.getCity())) {
            throw new IllegalArgumentException("City is required");
        }
        if (!StringUtils.hasText(profile.getCountry())) {
            throw new IllegalArgumentException("Country is required");
        }

        if (profile.getAddressLine1() != null && profile.getAddressLine1().length() > 500) {
            throw new IllegalArgumentException("Address Line 1 cannot exceed 500 characters");
        }
        if (profile.getAddressLine2() != null && profile.getAddressLine2().length() > 500) {
            throw new IllegalArgumentException("Address Line 2 cannot exceed 500 characters");
        }
        if (profile.getCity() != null && profile.getCity().length() > 100) {
            throw new IllegalArgumentException("City cannot exceed 100 characters");
        }
        if (profile.getState() != null && profile.getState().length() > 100) {
            throw new IllegalArgumentException("State cannot exceed 100 characters");
        }
        if (profile.getCountry() != null && profile.getCountry().length() > 100) {
            throw new IllegalArgumentException("Country cannot exceed 100 characters");
        }
        if (profile.getPostalCode() != null && profile.getPostalCode().length() > 20) {
            throw new IllegalArgumentException("Postal code cannot exceed 20 characters");
        }

        // --- KYC ---
        if (profile.getIdProofType() != null) {
            String type = profile.getIdProofType().toUpperCase();
            if (!type.equals("AADHAAR") && !type.equals("PAN") && !type.equals("PASSPORT") && !type.equals("DRIVING_LICENSE")) {
                throw new IllegalArgumentException("Invalid ID proof type. Allowed values: AADHAAR, PAN, PASSPORT, DRIVING_LICENSE");
            }
        }
        if (profile.getIdProofNumber() != null && profile.getIdProofNumber().length() > 100) {
            throw new IllegalArgumentException("ID proof number cannot exceed 100 characters");
        }

        // --- Bank Details (for owners) ---
        if (profile.getBankAccountNumber() != null && profile.getBankAccountNumber().length() > 50) {
            throw new IllegalArgumentException("Bank account number cannot exceed 50 characters");
        }
        if (profile.getBankIfscCode() != null && profile.getBankIfscCode().length() > 20) {
            throw new IllegalArgumentException("Bank IFSC code cannot exceed 20 characters");
        }
        if (profile.getBankName() != null && profile.getBankName().length() > 200) {
            throw new IllegalArgumentException("Bank name cannot exceed 200 characters");
        }

        // --- Emergency Contact ---
        if (profile.getEmergencyContactName() != null && profile.getEmergencyContactName().length() > 200) {
            throw new IllegalArgumentException("Emergency contact name cannot exceed 200 characters");
        }
        if (profile.getEmergencyContactPhone() != null && profile.getEmergencyContactPhone().length() > 20) {
            throw new IllegalArgumentException("Emergency contact phone cannot exceed 20 characters");
        }
        if (profile.getEmergencyContactRelation() != null && profile.getEmergencyContactRelation().length() > 50) {
            throw new IllegalArgumentException("Emergency contact relation cannot exceed 50 characters");
        }
    }


}