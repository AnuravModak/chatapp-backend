package com.demo.chatApp.repos;

import com.demo.chatApp.entities.Messages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Messages, Long> {
    List<Messages> findBySenderIdOrReceiverId(UUID senderId, UUID receiverId);
}
