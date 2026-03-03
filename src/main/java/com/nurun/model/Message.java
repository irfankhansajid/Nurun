package com.nurun.model;

import com.nurun.enumlist.MessageRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant sentAt;

    @JoinColumn(name = "conversation_id", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Conversation conversation;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageRole messageRole;

    private String modelUsed;

    private String providerUsed;


}
