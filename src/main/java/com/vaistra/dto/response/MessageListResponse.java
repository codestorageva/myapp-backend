package com.vaistra.dto.response;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageListResponse {
    private boolean success;
    HttpStatus successCode;
    private String message;
    Map<Integer, String[]> data  = new HashMap<>();
}
