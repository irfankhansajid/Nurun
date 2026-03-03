package com.nurun.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nurun.enumlist.AuthProvider;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String providerId; // store provider sub id

    private String displayName; // from provider or local

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = true)
    @JsonIgnore
    private String password;

    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;


    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;


    @OneToMany(mappedBy = "user")
    private List<Conversation> conversations;

}
