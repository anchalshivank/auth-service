package com.renter.auth.security;


import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtUtils {

    private final String jwksUrl;
    private final Map<String, RSAPublicKey> keyCache = new ConcurrentHashMap<>();

    public JwtUtils(
            @Value("${keycloak.auth-server-url}") String authServerUrl,
            @Value("${keycloak.realm}") String realm
    ){
        this.jwksUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/certs";
        System.out.println("------------>>>JWKS URL: " + this.jwksUrl);
    }

    public Map<String, Object> validateTokenAndGetClaims(String token) throws Exception {

        SignedJWT signedJWT = SignedJWT.parse(token);
        String kid = signedJWT.getHeader().getKeyID();
        RSAPublicKey publicKey = getPublicKey(kid);

        if (publicKey == null){
            throw new IllegalStateException("Public key not found for kid: "+ kid);
        }

        if (!signedJWT.verify(new RSASSAVerifier(publicKey))){

            throw new IllegalStateException("Invalid JWT signature");

        }

        return signedJWT.getJWTClaimsSet().getClaims();

    }

    private RSAPublicKey getPublicKey(String kid) throws Exception{

        if (keyCache.containsKey(kid)) return keyCache.get(kid);

        System.out.println("------------------------------>>>>>"+ jwksUrl);

        JWKSet jwkSet = JWKSet.load(new URL(jwksUrl));
        System.out.println("-------------2----------------->>>>>"+ jwkSet);

        JWK jwk = jwkSet.getKeyByKeyId(kid);

        System.out.println("-------------3----------------->>>>>"+ jwk);
        if (jwk instanceof RSAKey rsaKey){

            RSAPublicKey publicKey = rsaKey.toRSAPublicKey();
            keyCache.put(kid, publicKey);
            return publicKey;

        }

        return null;

    }




}
