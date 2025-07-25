package com.hrishabh.algocrack.helpers;

import com.hrishabh.algocrackentityservice.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPassengerDetails extends User implements UserDetails {

    private final String username;
    private final String password;

    public UserPassengerDetails(User user){
        this.username = user.getName();
        this.password = user.getPassword();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
