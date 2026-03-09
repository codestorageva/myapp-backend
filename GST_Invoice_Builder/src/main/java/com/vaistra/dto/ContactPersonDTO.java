package com.vaistra.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContactPersonDTO {

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
}
