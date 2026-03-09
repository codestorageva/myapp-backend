package com.vaistra.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class CityExportDTO {


    private Integer cityId;

    private String cityName;

    private String stateName;

    private String updatedAt;

}
