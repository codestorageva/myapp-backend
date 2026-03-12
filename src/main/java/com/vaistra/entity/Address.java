package com.vaistra.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Embeddable
public class Address {

    private String attention;
    private String addressLine1;
    private String addressLine2;
    private String pincode;

}
