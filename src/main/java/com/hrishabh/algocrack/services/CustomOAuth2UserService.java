package com.hrishabh.algocrack.services;

import com.hrishabh.algocrack.helpers.Validations;
import com.hrishabh.algocrack.repository.UserRepository;
import com.hrishabh.algocrackentityservice.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    private final Validations validations;

    public CustomOAuth2UserService(Validations validations) {
        this.validations = validations;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");

        if (email == null) {
            logger.error("Email attribute is missing from OAuth2 response.");
            throw new OAuth2AuthenticationException("Email not found in Google response");
        }

        // Generate unique user_id (username)
        String generatedUserId = validations.generateUniqueUserId(name, email);

        // Register or retrieve existing user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    logger.info("User not found in DB. Registering new user.");
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setImgUrl(picture);
                    newUser.setUserId(generatedUserId);
                    return userRepository.save(newUser);
                });

        // Update picture if missing
        if (user.getImgUrl() == null && picture != null) {
            logger.info("Updating user image URL");
            user.setImgUrl(picture);
            userRepository.save(user);
        }

        // Create claims with userId and role
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("role", "USER"); // TODO: use user.getRole() when available

        // Issue JWT with claims
        String token = jwtService.createToken(claims, user.getEmail());
        logger.info("JWT issued for user {}: {}", user.getEmail(), token);

        // Append JWT to attributes
        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
        attributes.put("jwt", token);

        return new DefaultOAuth2User(
                oauth2User.getAuthorities(),
                attributes,
                "email");
    }
}
