package com.demo.chatApp.controllers;

import com.demo.chatApp.entities.Messages;
import com.demo.chatApp.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@Controller
public class MessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService){
        this.messageService=messageService;
    }




    @MessageMapping("/sendMessage")
    @SendTo("/queue/messages")
    public Messages sendMessage(Messages message) {
        // You could save the message to the database here
        messagingTemplate.convertAndSendToUser(message.getReceiver().toString(), "/queue/messages", message);
        return message;
    }

    @GetMapping("/admin/getMessages/{senderId}/{receiverId}")
    public ResponseEntity<?> getChats(@PathVariable UUID senderId, @PathVariable UUID receiverId) {

        try {
            System.out.println("Fetching chat history between: " + senderId + " and " + receiverId);

            List<Messages> messages = messageService.getChats(senderId, receiverId);

            System.out.println("Chat history retrieved successfully: " + messages.size() + " messages found.");

            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            System.err.println("Error retrieving chat history: " + e.getMessage());
            e.printStackTrace(); // Log full stack trace

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching chat history: " + e.getMessage());
        }
    }
}
