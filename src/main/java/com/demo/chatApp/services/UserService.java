package com.demo.chatApp.services;

import com.demo.chatApp.entities.TypingStatus;
import com.demo.chatApp.entities.TypingStatusMessage;
import com.demo.chatApp.entities.User;
import com.demo.chatApp.entities.UserStatus;
import com.demo.chatApp.exceptions.EmailAlreadyInUseException;
import com.demo.chatApp.exceptions.InvalidCredentialsException;
import com.demo.chatApp.exceptions.UsernameAlreadyInUseException;
import com.demo.chatApp.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private  JwtTokenUtil jwtUtil;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = (BCryptPasswordEncoder) passwordEncoder;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByUserId(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return userRepository.findUserById(uuid);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid UUID format: " + id);
            return Optional.empty();
        }
    }

    public List<User> findAllUser(){
        return  userRepository.findAll();
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User registerUser(User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new EmailAlreadyInUseException("Email already in use.");
        }

        Optional<User> existingUsername = userRepository.findByUsername(user.getUsername());
        if (existingUsername.isPresent()) {
            throw new UsernameAlreadyInUseException("Username already in use.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public String loginUser(String username, String password){
        Optional<User> userOpt=userRepository.findByUsername(username);

        if (userOpt.isPresent() && passwordEncoder.matches(password,userOpt.get().getPassword())){
            return jwtUtil.generateToken( userOpt.get());
        }
        throw new InvalidCredentialsException("Invalid username or password.");
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
            if (user == null) {
                throw new InvalidCredentialsException("User not found with username: " + username);
            }
        }

        // Create UserDetails object to return (Spring Security will use it for authentication)
        org.springframework.security.core.userdetails.User.UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(user.get().getUsername());
        builder.password(user.get().getPassword());  // Set password

        return builder.build();
    }

    public void updateUserOnlineStatus(UUID userId, boolean isOnline){
        userRepository.updateUserOnlineStatus(userId, isOnline);
        messagingTemplate.convertAndSend("/topic/online-status", new UserStatus(userId, isOnline));
    }

    public void notifyTyping(UUID senderId, UUID receiverId, boolean isTyping) {
        TypingStatus typingStatus = isTyping ? TypingStatus.TYPING : TypingStatus.NOT_TYPING;

        messagingTemplate.convertAndSendToUser(
                receiverId.toString(), "/queue/typing", new TypingStatusMessage(senderId, typingStatus)
        );
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
