package com.vaistra.controller;

import com.vaistra.dto.CityDto;
import com.vaistra.dto.response.*;
import com.vaistra.dto.update.CityUpdateDto;
import com.vaistra.service.CityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/city")
public class CityController {

    private final CityService cityService;

    @Autowired
    public CityController(CityService cityService) {
        this.cityService = cityService;
    }


    @PostMapping
    public ResponseEntity<MessageResponse> addCity(@Valid @RequestBody CityDto cityDto, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(cityService.addCity(cityDto, headers), HttpStatus.OK);
    }

    @GetMapping("{cityId}")
    public ResponseEntity<DataResponse> getCityById(@PathVariable int cityId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(cityService.getCityById(cityId, headers), HttpStatus.OK);
    }


    @GetMapping
    public ResponseEntity<HttpResponse> getAllCities
    (@RequestParam(value = "keyword", required = false) String keyword,
     @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
     @RequestParam(value = "pageSize", defaultValue = "2147483647", required = false) Integer pageSize,
     @RequestParam(value = "sortBy", defaultValue = "updatedAt", required = false) String sortBy,
     @RequestParam(value = "sortDirection", defaultValue = "asc", required = false) String sortDirection,
     @RequestParam(value = "isDeleted", defaultValue = "false", required = false) String isDeleted,
     @RequestParam(value = "status", defaultValue = "true", required = false) String status,
     @RequestHeader Map<String, String> headers
    ) {
        return new ResponseEntity<>(cityService.getAllCities(keyword, pageNumber, pageSize, sortBy, sortDirection, isDeleted, status, headers), HttpStatus.OK);
    }


    @PutMapping("{cityId}")
    public ResponseEntity<MessageResponse> updateCity(@Valid @RequestBody CityUpdateDto cityDto, @PathVariable int cityId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(cityService.updateCity(cityDto, cityId, headers), HttpStatus.OK);
    }


    @PutMapping("softDelete/{cityId}")
    public ResponseEntity<MessageResponse> softDeleteCityById(@PathVariable int cityId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(cityService.softDeleteCityById(cityId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
//    @DeleteMapping("hardDelete/{districtId}")
//    public ResponseEntity<MessageResponse> hardDeleteDistrictById(@PathVariable int districtId, @RequestHeader Map<String, String> headers) {
//        return new ResponseEntity<>(districtService.deleteDistrictById(districtId, headers), HttpStatus.OK);
//    }


    @PutMapping("restore/{cityId}")
    public ResponseEntity<MessageResponse> restoreCityById(@PathVariable int cityId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(cityService.restoreCityById(cityId, headers), HttpStatus.OK);
    }


    @PostMapping("/UploadCsv")
    public ResponseEntity<MessageListResponse> uploadCityCSV(@RequestParam(name = "file", required = false) MultipartFile file, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(cityService.uploadCityCSV(file, headers), HttpStatus.OK);
    }


    @GetMapping("/active")
    public ResponseEntity<ListResponse> getAllCitiesByStateIdAndStatusAndIsDeleted(@RequestParam("stateId") Integer stateId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(cityService.getAllCitiesByStateIdAndStatusAndIsDeleted(stateId, headers), HttpStatus.OK);
    }



    @GetMapping("/exportData")
    public ResponseEntity<ListResponse> exportedCityData(@RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(cityService.exportedCityData(headers), HttpStatus.OK);
    }

    @GetMapping("/totalCity")
    public ResponseEntity<LongResponse> getTotalCity(){
        return new ResponseEntity<>(cityService.getTotalCity(), HttpStatus.OK);
    }

}

