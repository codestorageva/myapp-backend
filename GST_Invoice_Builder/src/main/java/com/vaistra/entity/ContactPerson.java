package com.vaistra.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "contact_persons")
public class ContactPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contactperson_id")
    private Integer contactPersonId;

    private String salutation;
    private String firstName;
    private String lastName;
    private String email;
    private String workPhone;
    private String mobileNumber;
    private Boolean status;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
}

