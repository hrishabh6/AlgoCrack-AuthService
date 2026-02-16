package com.hrishabh.algocrack.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service class for handling JWT (JSON Web Token) operations using RS256
 * (asymmetric).
 *
 * Signs tokens with RSA private key.
 * Validates tokens with RSA public key.
 *
 * Only this service holds the private key — the API Gateway only has the public
 * key
 * and can validate but never issue tokens.
 */
@Service
public class JwtService {

    @Value("${jwt.expiry}")
    private int expiry;

    @Value("${jwt.private-key-path:classpath:keys/private.pem}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key-path:classpath:keys/public.pem}")
    private Resource publicKeyResource;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            // Load private key (for signing)
            String privateKeyPem = new String(privateKeyResource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);
            privateKeyPem = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPem);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.privateKey = keyFactory.generatePrivate(privateKeySpec);

            // Load public key (for validation)
            String publicKeyPem = new String(publicKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            publicKeyPem = publicKeyPem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPem);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            this.publicKey = keyFactory.generatePublic(publicKeySpec);

            System.out.println("✅ RSA key pair loaded successfully for JWT operations");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA keys for JWT", e);
        }
    }

    /**
     * Creates a new JWT token signed with RS256 (RSA private key).
     *
     * @param payload Custom claims to include
     * @param email   The subject (email) of the token
     * @return Signed JWT string
     */
    public String createToken(Map<String, Object> payload, String email) {
        Date expiryDate = new Date(System.currentTimeMillis() + expiry * 1000L);

        return Jwts.builder()
                .claims(payload)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expiryDate)
                .subject(email)
                .signWith(privateKey) // RS256 — signed with private key
                .compact();
    }

    public String createToken(String email) {
        return createToken(new HashMap<>(), email);
    }

    /**
     * Extracts the payload (claims) from a given JWT token.
     * Validates signature using the public key.
     */
    public Claims extractPayload(String token) {
        return Jwts.parser()
                .verifyWith(publicKey) // Verify with public key
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract a specific claim from the JWT payload.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractPayload(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts the expiration date from a JWT token.
     */
    public Date extractExpiryDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Checks if a JWT token has expired.
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiryDate(token).before(new Date());
    }

    /**
     * Extracts the subject (email) from a JWT token.
     */
    public String getEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validates a token against the expected email.
     */
    public Boolean validateToken(String token, String email) {
        final String userEmailFetchedFromToken = getEmail(token);
        return (userEmailFetchedFromToken.equals(email)) && !isTokenExpired(token);
    }
}
