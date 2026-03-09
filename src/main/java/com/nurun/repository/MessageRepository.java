package com.nurun.repository;

import com.nurun.dto.MessageResponseDto;
import com.nurun.model.Message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationIdOrderBySentAtDesc(Long conversationId, Pageable pageable);


}
