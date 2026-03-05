package com.hrishabh.algocrack.models;

import jakarta.persistence.*;
import lombok.*;

/**
 * User entity — owned by AuthService.
 * Cross-service relationships (questionsSolved, pastSubmissions) have been
 * removed.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseModel {

    private String name;

    private String password;

    private String email;

    @Column(unique = true, nullable = false)
    private String userId;

    private String imgUrl; // Optional

    @Column(name = "ranking")
    private String rank;

    private Integer achievementPoints;

    private String headline;

    private String about;

    private String location;

    private String website;

    private String githubProfile;

    private String twitterProfile;

    private String linkedinProfile;

    private String skills;

    private String school;

}
