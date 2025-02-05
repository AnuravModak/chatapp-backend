package com.demo.chatApp.services;

import com.demo.chatApp.entities.Messages;
import com.demo.chatApp.repos.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;  // RedisTemplate for publishing messages

    @Autowired
    private SimpMessagingTemplate messagingTemplate;  // Spring's template to send WebSocket messages

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    public List<Messages> getMessages(UUID userId) {
        return messageRepository.findBySenderIdOrReceiverId(userId, userId);
    }

    public Messages save(Messages message) {
        if (message.getTimestamp()==null){
            message.setTimestamp(LocalDateTime.now());
        }

        if (message.isRead()==false){
            message.setRead(false);
        }
        // Save message to the database
        Messages savedMessage = messageRepository.save(message);

        redisTemplate.convertAndSend("chatRoom: "+message.getReceiver().getId(), savedMessage);

        messagingTemplate.convertAndSendToUser(message.getReceiver().getId().toString(),
                "/queue/messages",
                savedMessage
                );

        return savedMessage;
    }
    // Method to handle JWT validation for WebSocket connections
    public boolean validateJwt(String token) {
        return jwtTokenUtil.validateToken(token);  // Use your JWT utility to validate the token
    }
}
