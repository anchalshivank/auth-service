package com.renter.auth.security;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.net.URLConnection;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtUtils {

    private final String jwksUrl;
    private final Map<String, RSAPublicKey> keyCache = new ConcurrentHashMap<>();
    private JWKSet jwkSet;
    private long lastFetchTime = 0;
    private static final long CACHE_VALIDITY_MS = 3600000; // 1 hour

    public JwtUtils(
            @Value("${keycloak.auth-server-url}") String authServerUrl,
            @Value("${keycloak.realm}") String realm
    ){
        this.jwksUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/certs";
        System.out.println("------------>>>JWKS URL: " + this.jwksUrl);
        System.out.println("JwtUtils initialized. JWKS will be fetched on first token validation.");
    }

    public Map<String, Object> validateTokenAndGetClaims(String token) throws Exception {
        try {
            System.out.println("Validating token...");
            SignedJWT signedJWT = SignedJWT.parse(token);
            String kid = signedJWT.getHeader().getKeyID();
            System.out.println("Token kid: " + kid);
            
            RSAPublicKey publicKey = getPublicKey(kid);

            if (publicKey == null){
                throw new IllegalStateException("Public key not found for kid: "+ kid);
            }

            if (!signedJWT.verify(new RSASSAVerifier(publicKey))){
                throw new IllegalStateException("Invalid JWT signature");
            }

            return signedJWT.getJWTClaimsSet().getClaims();
        } catch (Exception e) {
            System.err.println("ERROR validating token: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private RSAPublicKey getPublicKey(String kid) throws Exception {
        try {
            if (keyCache.containsKey(kid)) {
                System.out.println("Found key in cache for kid: " + kid);
                return keyCache.get(kid);
            }

            System.out.println("Key not in cache, fetching JWKS for kid: " + kid);
            
            // Fetch JWKS if not cached or cache expired
            if (jwkSet == null || System.currentTimeMillis() - lastFetchTime > CACHE_VALIDITY_MS) {
                System.out.println("Fetching JWKS from: " + jwksUrl);
                jwkSet = fetchJWKSet();
                lastFetchTime = System.currentTimeMillis();
                System.out.println("JWKS fetched successfully");
            }

            JWK jwk = jwkSet.getKeyByKeyId(kid);
            System.out.println("Found JWK for kid: " + kid);

            if (jwk instanceof RSAKey rsaKey){
                RSAPublicKey publicKey = rsaKey.toRSAPublicKey();
                keyCache.put(kid, publicKey);
                System.out.println("Cached public key for kid: " + kid);
                return publicKey;
            }
            
            throw new IllegalStateException("JWK is not an RSA key for kid: " + kid);
        } catch (Exception e) {
            System.err.println("ERROR getting public key for kid: " + kid);
            System.err.println("Exception: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private JWKSet fetchJWKSet() throws Exception {
        try {
            System.out.println("Opening connection to: " + jwksUrl);
            URL url = new URL(jwksUrl);
            URLConnection connection = url.openConnection();
            
            System.out.println("Setting connection timeouts...");
            connection.setConnectTimeout(15000);  // 15 seconds
            connection.setReadTimeout(15000);     // 15 seconds
            
            System.out.println("Connecting and loading JWKS...");
            JWKSet set = JWKSet.load(connection.getInputStream());
            System.out.println("JWKS loaded successfully with " + set.getKeys().size() + " keys");
            return set;
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("SOCKET TIMEOUT: Connection to Keycloak timed out");
            System.err.println("URL: " + jwksUrl);
            System.err.println("Make sure Keycloak is running and accessible from your network");
            throw new RuntimeException("Keycloak JWKS fetch timed out after 15 seconds. Check network connectivity and Keycloak availability.", e);
        } catch (java.net.ConnectException e) {
            System.err.println("CONNECTION ERROR: Cannot connect to Keycloak");
            System.err.println("URL: " + jwksUrl);
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException("Cannot connect to Keycloak at " + jwksUrl + ". Check if Keycloak is running and the URL is correct.", e);
        } catch (Exception e) {
            System.err.println("ERROR fetching JWKS");
            System.err.println("URL: " + jwksUrl);
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch JWKS from Keycloak: " + e.getMessage(), e);
        }
    }
}