package com.nurun.repository;

import com.nurun.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationIdOrderBySentAtDesc(Long conversationId, Pageable pageable);

    List<Message> findTop15ByConversationIdOrderBySentAtDesc(Long conversationId);

    long countByConversationId(Long conversationId);
}
