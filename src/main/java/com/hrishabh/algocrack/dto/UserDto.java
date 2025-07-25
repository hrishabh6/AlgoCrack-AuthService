package com.hrishabh.algocrack.dto;

import com.hrishabh.algocrackentityservice.models.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private String id;

    private String name;

    private String user_id;

    private String email;

    private String createdAt;

    private String password;

    public static UserDto toDto(User user) {

        return UserDto.builder()
                .id(Long.toString(user.getId()))
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .user_id(user.getUserId())
                .createdAt(String.valueOf(user.getCreatedAt()))
                .build();
    }


}
