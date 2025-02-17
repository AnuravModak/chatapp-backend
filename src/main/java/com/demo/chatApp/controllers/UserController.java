package com.demo.chatApp.controllers;

import com.demo.chatApp.services.UserService;
import com.demo.chatApp.entities.LoginRequest;
import com.demo.chatApp.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/admin/all/users")
    public ResponseEntity<List<User>> getAllUsers(){
        List<User> users= userService.findAllUser();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @GetMapping("admin/user/{userId}")
    public ResponseEntity<Optional<User>> getUserById(@PathVariable String userId){
        Optional<User> user=userService.findByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PostMapping("/online-status")
    public ResponseEntity<String> updateUserOnlineStatus(
            @RequestParam String userId,
            @RequestParam boolean isOnline) {
        UUID uuidUserId = UUID.fromString(userId);
        userService.updateUserOnlineStatus(uuidUserId, isOnline);
        return ResponseEntity.ok("User online status updated");
    }

    @PostMapping("/typing-status")
    public ResponseEntity<String> notifyTyping(
            @RequestParam String senderId,
            @RequestParam String receiverId,
            @RequestParam boolean isTyping) {
        UUID uuidSenderId = UUID.fromString(senderId);
        UUID uuidReceiverId = UUID.fromString(receiverId);
        userService.notifyTyping(uuidSenderId, uuidReceiverId, isTyping);
        return ResponseEntity.ok("Typing status updated");
    }



    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user){
        User savedUser= userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);

    }

    @PostMapping("/login")
    public ResponseEntity<String> login (@RequestBody LoginRequest loginRequest){
        String token= userService.loginUser(loginRequest.getUsername(),loginRequest.getPassword());
        return ResponseEntity.ok(token);
    }
}
