package com.vaistra.dto.update;


import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CityUpdateDto {

    private Integer cityId;

    @Pattern(regexp = "^[a-zA-Z ]+$", message = "State name must contain only alphabets and white spaces")
    private String cityName;

    //    @NotNull(message = "Country ID should not be null!")
//    @Min(value = 1, message = "Country ID must be a positive integer!")
//    private Integer countryId;
//    private String countryName;

    private Integer stateId;
    private String stateName;

    private Boolean status;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
