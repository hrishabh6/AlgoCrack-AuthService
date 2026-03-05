package com.hrishabh.algocrack.dto;

import com.hrishabh.algocrack.models.User;
import lombok.*;

/**
 * DTO for exposing user info to other services via API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {
    private String userId;
    private String name;
    private String email;
    private String imgUrl;
    private String rank;
    private String headline;
    private String about;
    private String location;
    private String school;
    private String website;
    private String githubProfile;
    private String twitterProfile;
    private String linkedinProfile;
    private String skills;

    public static UserInfoDto fromEntity(User user) {
        return UserInfoDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .imgUrl(user.getImgUrl())
                .rank(user.getRank())
                .headline(user.getHeadline())
                .about(user.getAbout())
                .location(user.getLocation())
                .school(user.getSchool())
                .website(user.getWebsite())
                .githubProfile(user.getGithubProfile())
                .twitterProfile(user.getTwitterProfile())
                .linkedinProfile(user.getLinkedinProfile())
                .skills(user.getSkills())
                .build();
    }
}
