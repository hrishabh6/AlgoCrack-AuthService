package com.hrishabh.algocrack.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSignupRequestDto {

    private String name;
    private String email;
    private String password;
    private String user_id;


}
