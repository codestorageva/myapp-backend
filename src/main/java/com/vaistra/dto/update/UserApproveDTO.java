package com.vaistra.dto.update;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserApproveDTO {
    private Integer userId;

    private String vid;

    @NotNull(message = "approvalStatus should not be Empty!")
    @NotEmpty(message = "approvalStatus should not be null!")
    private String approvalStatus;

    private String approvedBy;
    private LocalDateTime approvedAt;

    private String rejectedBy;
    private LocalDateTime rejectedAt;
    private String rejectionReason;
}
