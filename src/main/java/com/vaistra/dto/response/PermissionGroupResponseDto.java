package com.vaistra.dto.response;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionGroupResponseDto {
    private boolean success;
    HttpStatus successCode;
    private Integer pageNumber;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    private Boolean isLastPage;
    List<PermissionSuperGroupResponse> data = new ArrayList<>();
}
