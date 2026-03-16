package com.nurun.repository;

import com.nurun.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
    List<Conversation> findByUserId(Long userId);
}
