package com.vaistra.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data

public class ChangePasswordDTO
{

    @NotEmpty(message = "Password Should not be Empty!")
    @NotNull(message = "Password Should not be Null!")
    private String oldPassword;

    @NotEmpty(message = "Password Should not be Empty!")
    private String newPassword;
}
