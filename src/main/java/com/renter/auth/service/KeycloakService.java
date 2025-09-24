package com.renter.auth.service;

import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KeycloakService{

    @Autowired
    private Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public String createUser(String username, String email, String firstName, String lastName, String password){

        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            UserRepresentation user = new UserRepresentation();

            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEnabled(true);
            user.setEmailVerified(true);

            Response response = usersResource.create(user);
            System.out.println(response.getStatus());

            if (response.getStatus() == 201){
                String userId = extractUserIdFromResponse(response);
                System.out.println("extraction success");
                setUserPassword(userId, password);
                System.out.println("set password success");
                return userId;
            }else{
                throw new RuntimeException("Failed to create user. Status: "+ response.getStatus());
            }



        } catch (Exception e) {

            throw new RuntimeException("Error creating user: "+ e.getMessage(),e);

        }

    }

    private void setUserPassword(String userId, String password){
        RealmResource realmResource = keycloak.realm(realm);
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setType(password);
        credential.setTemporary(false);

        realmResource.users().get(userId).resetPassword(credential);
    }

    private String extractUserIdFromResponse(Response response){

        String location = response.getHeaderString("Location");
        return location.substring(location.lastIndexOf('/') + 1);

    }



}
