package com.hrishabh.algocrack.helpers;
import com.hrishabh.algocrack.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class Validations {

    private final UserRepository userRepository;

    public Validations(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateUniqueUserId(String name, String email) {
        String base = (name + "_" + email.split("@")[0]).toLowerCase().replaceAll("[^a-z0-9]", "_");
        String userId = base;
        int suffix = 1;

        while (userRepository.existsByUserId(userId)) {
            userId = base + "_" + suffix;
            suffix++;
        }

        return userId;
    }


}
