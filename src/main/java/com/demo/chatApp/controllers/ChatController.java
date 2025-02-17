package com.demo.chatApp.controllers;

import com.demo.chatApp.entities.MessageDTO;
import com.demo.chatApp.entities.MessageStatus;
import com.demo.chatApp.entities.Messages;
import com.demo.chatApp.entities.User;
import com.demo.chatApp.repos.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private MessageConverter messageConverter;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageRepository messageRepository;


    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody MessageDTO messageDTO) {
        try {
            UUID messageId = UUID.randomUUID();

            System.out.println("Sender: " + messageDTO.getSender() + " | Receiver: " + messageDTO.getReceiver());

            messageRepository.insertMessage(
                    messageId,
                    messageDTO.getSender(),
                    messageDTO.getReceiver(),
                    messageDTO.getContent(),
                    LocalDateTime.now(),
                    false,
                    MessageStatus.SENT.toString()
            );

            messagingTemplate.convertAndSendToUser(
                    messageDTO.getReceiver().toString(),
                    "/queue/messages",
                    messageDTO
            );

            return ResponseEntity.ok("Message sent successfully with ID: " + messageId);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending message: " + e.getMessage());
        }
    }



}
