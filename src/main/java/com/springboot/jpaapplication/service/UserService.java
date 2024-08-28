package com.springboot.jpaapplication.service;

import com.springboot.jpaapplication.model.CurrentUserDetails;
import com.springboot.jpaapplication.model.User;
import com.springboot.jpaapplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @description Allow the use of Spring Security via the UserDetailsService class. The
 * UserRepository allows for CRUD operations to be performed.
 */
@Service
public class UserService implements UserDetailsService {

    private UserRepository userRepo;

    @Autowired
    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public User findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public User findByConfirmationToken(String confirmationToken) {
        return userRepo.findByConfirmationToken(confirmationToken);
    }

    public void saveUser(User user) {
        userRepo.save(user);
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User can not be found");
        }

        return new CurrentUserDetails(user);
    }
}
