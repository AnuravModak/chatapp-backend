package com.demo.chatApp.repos;

import com.demo.chatApp.entities.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findUserById(@Param("id") UUID id);

    @Query("Update User u set u.isOnline= :isOnline WHERE u.id = :id")
    boolean updateUserOnlineStatus(@Param("id") UUID id, @Param("isOnline") boolean isOnline);

}
