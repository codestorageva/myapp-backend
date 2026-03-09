package com.vaistra.config;

import com.vaistra.entity.User;
import com.vaistra.exception.ResourceNotFoundException;
import com.vaistra.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user =  userRepository.findByEmailIgnoreCase(username);
        if(user == null)
        {
            throw new ResourceNotFoundException("User with email '"+username+"' not found!");
        }
        return user;
    }
}
