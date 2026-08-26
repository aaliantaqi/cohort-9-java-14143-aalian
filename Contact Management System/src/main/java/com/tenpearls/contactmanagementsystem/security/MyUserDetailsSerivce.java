package com.tenpearls.contactmanagementsystem.security;

import com.tenpearls.contactmanagementsystem.model.User;
import com.tenpearls.contactmanagementsystem.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsSerivce implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsSerivce(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Spring Security still calls this method "loadUserByUsername" (that name is
        // baked into the interface), but "identifier" here is whatever the person
        // typed at login - could be an email or a phone number.
        User user = userRepository.findByEmailOrPhone(identifier);
        if (user == null) {
            throw new UsernameNotFoundException("This User doesn't exists in the Database");
        }
        return new UserPrincipal(user);
    }
}