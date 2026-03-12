package com.vaistra.service;


import com.vaistra.dto.CityDto;
import com.vaistra.dto.response.*;
import com.vaistra.dto.update.CityUpdateDto;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;


import java.util.Map;

public interface CityService {

    MessageResponse addCity( CityDto cityDto, Map<String, String> headers);

    DataResponse getCityById(int cityId, Map<String, String> headers);

    HttpResponse getAllCities(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, String isDeleted, String status, Map<String, String> headers);

    MessageResponse updateCity( CityUpdateDto cityDto, int cityId, Map<String, String> headers);

    MessageResponse softDeleteCityById(int cityId, Map<String, String> headers);

    MessageResponse restoreCityById(int cityId, Map<String, String> headers);

    MessageListResponse uploadCityCSV(MultipartFile file, Map<String, String> headers);

    ListResponse getAllCitiesByStateIdAndStatusAndIsDeleted(Integer stateId, Map<String, String> headers);

    ListResponse exportedCityData(Map<String, String> headers);

    LongResponse getTotalCity();
}
