package com.vaistra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "confirmation")

public class Confirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String token;
    private LocalDateTime createdAt;

    private String email;

    private String otp;

    @OneToOne
    @JoinColumn( name = "user_id")
    private User user;

    private Boolean isVerified;

    public Confirmation(String email, String otp)
    {
        this.otp = otp;
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.token = UUID.randomUUID().toString();
        this.isVerified = false;
    }

    public Confirmation(User user) {
        this.user=user;
        this.token=UUID.randomUUID().toString();
        this.createdAt=LocalDateTime.now();
        this.isVerified = false;
    }
}