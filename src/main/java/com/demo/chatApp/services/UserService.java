package com.demo.chatApp.services;

import com.demo.chatApp.entities.User;
import com.demo.chatApp.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;


    private  BCryptPasswordEncoder passwordEncoder;


    @Autowired
    private JwtTokenUtil jwtUtil;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User registerUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public String loginUser(String username, String password){
        Optional<User> userOpt=userRepository.findByUsername(username);
        if (userOpt.isPresent() && passwordEncoder.matches(password,userOpt.get().getPassword())){
            return jwtUtil.generateToken((UserDetails) userOpt.get());
        }
        throw new RuntimeException("Invalid username or password");
    }

    public User oauthLogin(String email, String provider, String oauthId) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return userOpt.get();
        } else {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setOauthProvider(provider);
            newUser.setOauthId(oauthId);
            return userRepository.save(newUser);  // Add this line
        }

    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch the user from the database by username
        Optional<User> user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        // Create UserDetails object to return (Spring Security will use it for authentication)
        org.springframework.security.core.userdetails.User.UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(user.get().getUsername());
        builder.password(user.get().getPassword());  // Set password

        return builder.build();
    }

    // Update user profile
    public User updateUserProfile(User user) {
        return userRepository.save(user);
    }

    // Validate JWT Token
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

}
