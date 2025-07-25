package com.hrishabh.algocrack.services;

import com.hrishabh.algocrack.helpers.UserPassengerDetails;
import com.hrishabh.algocrack.repository.UserRepository;
import com.hrishabh.algocrackentityservice.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;



    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findUserByEmail(email);
        if (user.isPresent()) {
            return new UserPassengerDetails(user.get());
        } else {
            throw new UsernameNotFoundException("Username not found");
        }
    }
}

