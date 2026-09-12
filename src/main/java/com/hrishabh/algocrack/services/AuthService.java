package com.hrishabh.algocrack.services;



import com.hrishabh.algocrack.dto.UserDto;
import com.hrishabh.algocrack.dto.UserSignupRequestDto;
import com.hrishabh.algocrack.logging.LoggingConstants;
import com.hrishabh.algocrack.logging.StructuredLogger;
import com.hrishabh.algocrack.repository.UserRepository;
import com.hrishabh.algocrack.models.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final StructuredLogger structuredLogger = new StructuredLogger(AuthService.class, "AuthService");

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    public AuthService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public UserDto signUp(UserSignupRequestDto userSignupRequestDto){
        structuredLogger.debug("Creating auth user",
                LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.AUTH,
                LoggingConstants.OPERATION, "signup_create_user",
                LoggingConstants.USER_ID, userSignupRequestDto.getUser_id(),
                "email_present", userSignupRequestDto.getEmail() != null);

        User passenger = User.builder()
                .name(userSignupRequestDto.getName())
                .email(userSignupRequestDto.getEmail())
                .password(bCryptPasswordEncoder.encode(userSignupRequestDto.getPassword())) //Todo : Encrypt the password
                .userId(userSignupRequestDto.getUser_id())
                .build();
        User newUser = userRepository.save(passenger);

        structuredLogger.info("Auth user created",
                LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.AUTH,
                LoggingConstants.OPERATION, "signup_create_user",
                LoggingConstants.USER_ID, newUser.getUserId(),
                LoggingConstants.STATUS, "SUCCESS");

        return UserDto.toDto(newUser);

    }

}
