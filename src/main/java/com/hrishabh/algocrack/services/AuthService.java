package com.hrishabh.algocrack.services;



import com.hrishabh.algocrack.dto.UserDto;
import com.hrishabh.algocrack.dto.UserSignupRequestDto;
import com.hrishabh.algocrack.repository.UserRepository;
import com.hrishabh.algocrackentityservice.models.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    public AuthService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public UserDto signUp(UserSignupRequestDto userSignupRequestDto){
        User passenger = User.builder()
                .name(userSignupRequestDto.getName())
                .email(userSignupRequestDto.getEmail())
                .password(bCryptPasswordEncoder.encode(userSignupRequestDto.getPassword())) //Todo : Encrypt the password
                .userId(userSignupRequestDto.getUser_id())
                .build();
        User newUser = userRepository.save(passenger);

        return UserDto.toDto(newUser);

    }

}
