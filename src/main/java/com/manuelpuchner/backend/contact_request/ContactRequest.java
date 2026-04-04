package com.manuelpuchner.backend.contact_request;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
public class ContactRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String company;

    private String message;

    @Enumerated(EnumType.STRING)
    private ContactRequestStatus status = ContactRequestStatus.NEW;

    @CreationTimestamp
    private LocalDateTime createdAt;




}
