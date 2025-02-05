package com.demo.chatApp.controllers;

import com.demo.chatApp.entities.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @MessageMapping("/sendMessage")
    @SendTo("/queue/messages")
    public Messages sendMessage(Messages message) {
        // You could save the message to the database here
        messagingTemplate.convertAndSendToUser(message.getReceiver().getId().toString(), "/queue/messages", message);
        return message;
    }
}
