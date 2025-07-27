package com.hrishabh.algocrack.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service class for handling JWT (JSON Web Token) operations.
 * This class provides methods for creating, parsing, and validating JWTs.
 * It implements CommandLineRunner to demonstrate token generation and extraction on application startup.
 */
@Service
public class JwtService {

    /**
     * Injects the JWT expiry time in seconds from application properties (e.g., application.properties or application.properties).
     */
    @Value("${jwt.expiry}")
    private int expiry;

    /**
     * Injects the JWT secret key from application properties. This key is used for signing and verifying tokens.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Creates a new JWT token.
     *
     * @param payload A map of key-value pairs representing the custom claims to be included in the token's payload.
     * @param email The subject of the token, typically the username or user ID.
     * @return A signed JWT string.
     */
    public String createToken(Map<String, Object> payload, String email) {
        // Calculate the expiration date for the token.
        // It's current time + expiry (in seconds) converted to milliseconds.
        Date expiryDate = new Date(System.currentTimeMillis() + expiry * 1000L);

        // Build the JWT token using the Jwts.builder() fluent API.
        return Jwts.builder()
                .claims(payload) // Set the custom claims (payload).
                .issuedAt(new Date(System.currentTimeMillis())) // Set the token issuance time to the current time.
                .expiration(expiryDate) // Set the token expiration date.
                .subject(email) // Set the subject of the token.
                .signWith(getKey()) // Sign the token with the secret key.
                .compact(); // Compact the JWT into its final string representation.
    }

    public String createToken(String email) {
        return createToken(new HashMap<>(), email);
    }

    /**
     * Generates a SecretKey from the configured secret string.
     * This key is used for signing JWTs and verifying their signatures.
     *
     * @return A Key object suitable for HMAC-SHA signing.
     */
    public Key getKey() {
        // Convert the secret string to bytes using UTF-8 encoding and generate an HMAC-SHA key.
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Extracts the payload (claims) from a given JWT token.
     *
     * @param token The JWT string from which to extract the payload.
     * @return A Claims object containing all the claims from the token's payload.
     */
    public Claims extractPayload(String token) {
        // Use Jwts.parser() to create a parser, verify the token's signature,
        // build the parser, parse the signed claims, and then get the payload.
        return Jwts.parser()
                .verifyWith((SecretKey) getKey()) // Verify the token's signature using the secret key.
                .build() // Build the JwtParser instance.
                .parseSignedClaims(token) // Parse the signed JWT string.
                .getPayload(); // Get the claims (payload) from the parsed token.
    }

    /**
     * A generic method to extract a specific claim from the JWT payload.
     *
     * @param token The JWT string.
     * @param claimsResolver A function that takes a Claims object and returns the desired type T.
     * @param <T> The type of the claim to be extracted.
     * @return The extracted claim of type T.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractPayload(token); // First, extract all claims from the token.
        return claimsResolver.apply(claims); // Then, apply the provided function to get the specific claim.
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token The JWT string.
     * @return The expiration Date of the token.
     */
    public Date extractExpiryDate(String token) {
        // Use the generic extractClaim method to get the expiration date claim.
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Checks if a JWT token has expired.
     *
     * @param token The JWT string.
     * @return True if the token has expired, false otherwise.
     */
    public Boolean isTokenExpired(String token) {
        // Get the expiration date and compare it with the current date.
        return extractExpiryDate(token).before(new Date());
    }

    /**
     * Extracts the subject (username) from a JWT token.
     *
     * @param token The JWT string.
     * @return The subject string (username) of the token.
     */
    public String getEmail(String token) {
        // Use the generic extractClaim method to get the subject claim.
        return extractClaim(token, Claims::getSubject);
    }

    public Boolean validateToken(String token, String email) {
        final String userEmailFetchedFromToken = getEmail(token);
        return (userEmailFetchedFromToken.equals(email)) && !isTokenExpired(token);
    }



    /**
     * This method runs automatically when the Spring Boot application starts.
     * It demonstrates the usage of the JWT service methods: creating a token,
     * extracting the subject, and extracting the full payload.
     *
     * @param args Command line arguments (not used in this demonstration).
     * @throws Exception If an error occurs during token operations.
     */
//    @Override
//    public void run(String... args) throws Exception {
//        // Create a sample payload for the JWT.
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("username", "admin");
//        payload.put("password", "admin"); // In a real application, avoid putting sensitive info like passwords in JWT payload.
//
//        // Create a JWT token with the sample payload and a subject.
//        String result = createToken(payload, "hrishabh");
//        System.out.println("Generated JWT Token: " + result);
//
//        // Extract and print the subject (username) from the generated token.
//        System.out.println("Subject JWT Token: " + getEmail(result));
//
//        // Extract and print the entire payload (claims) from the generated token.
//        Claims extractedClaims = extractPayload(result);
//        System.out.println("Extracted JWT Payload: " + extractedClaims);
//
//        // Iterate through the extracted claims and print each key-value pair individually for clarity.
//        System.out.println("Individual Claims:");
//        extractedClaims.forEach((key, value) -> System.out.println(key + ": " + value));
//    }
}
