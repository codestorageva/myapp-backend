package com.vaistra.controller;

import com.vaistra.dto.BankDetailsDto;
import com.vaistra.dto.CompanyDto;
import com.vaistra.dto.response.DataResponse;
import com.vaistra.dto.response.HttpResponse;
import com.vaistra.dto.response.MessageResponse;
import com.vaistra.dto.update.CompanyUpdateDto;
import com.vaistra.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/company")
@CrossOrigin(origins = "*")
public class CompanyController {

    //---------------------------------------------------CONSTRUCTOR INJECTION------------------------------------------

    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }


//    @PostMapping
//    public ResponseEntity<MessageResponse> addCompany(@Valid @RequestBody CompanyDto company, @RequestHeader Map<String, String> headers) {
//        return new ResponseEntity<>(companyService.addCompany(company, headers), HttpStatus.OK);
//    }

    @PostMapping("/add")
    public ResponseEntity<MessageResponse> addCompany(

            @RequestParam String companyName,
            @RequestParam String ownerName,
            @RequestParam String password,
            @RequestParam(required = false) MultipartFile logoFile,

            @RequestParam String email,
            @RequestParam(required = false) String cinNumber,
            @RequestParam(required = false) String lrNumber,
            @RequestParam(required = false) String transport,
            @RequestParam(required = false) String commissionerate,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String division,

            @RequestParam String mobileNumber,
            @RequestParam(required = false) String alternateMobileNumber,

            @RequestParam String billingAddress1,
            @RequestParam(required = false) String billingAddress2,
            @RequestParam(required = false) String billingAddress3,
            @RequestParam Integer billingStateId,
            @RequestParam Integer billingCityId,
            @RequestParam String billingPincode,

            @RequestParam String panNumber,
            @RequestParam String gstNumber,
            @RequestParam(required = false) String serviceDescription,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) Boolean isDeleted,

            @RequestParam(required = false) List<String> bankName,
            @RequestParam(required = false) List<String> ifscCode,
            @RequestParam(required = false) List<String> branch,
            @RequestParam(required = false) List<String> accountNumber,
            @RequestParam(required = false) List<String> accHolderName,
            @RequestParam(required = false) List<String> bankAddress,
            @RequestParam(required = false) List<Boolean> bankStatus,
            @RequestParam(required = false) List<Boolean> bankIsDeleted,

            @RequestHeader Map<String, String> headers
    ) {

        MessageResponse response = companyService.addCompany(
                companyName, ownerName, password, logoFile,

                email, cinNumber, lrNumber, transport,
                commissionerate, range, division,
                mobileNumber, alternateMobileNumber,

                billingAddress1, billingAddress2, billingAddress3,
                billingStateId, billingCityId, billingPincode,

                panNumber, gstNumber, serviceDescription, industry,
                status, isDeleted,

                bankName, ifscCode, branch, accountNumber,
                accHolderName, bankAddress, bankStatus, bankIsDeleted,
                headers
        );

        return ResponseEntity.ok(response);
    }



    @GetMapping
    public ResponseEntity<HttpResponse> getAllCompanies(@RequestParam(value = "keyword", required = false) String keyword,
                                                        @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
                                                        @RequestParam(value = "pageSize", defaultValue = "2147483647", required = false) Integer pageSize,
                                                        @RequestParam(value = "sortBy", defaultValue = "companyId", required = false) String sortBy,
                                                        @RequestParam(value = "sortDirection", defaultValue = "asc", required = false) String sortDirection,
                                                        @RequestParam(value = "isDeleted", defaultValue = "false", required = false) String isDeleted,
                                                        @RequestParam(value = "status", defaultValue = "true", required = false) String status,
                                                        @RequestHeader Map<String, String> headers) {

        return new ResponseEntity<>(companyService.getAllCompanies(keyword, pageNumber, pageSize, sortBy, sortDirection, isDeleted, status, headers), HttpStatus.OK);
    }


    @GetMapping("{companyId}")
    public ResponseEntity<DataResponse> getCompanyById(@PathVariable int companyId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(companyService.getCompanyById(companyId, headers), HttpStatus.OK);
    }

    @GetMapping("password/{companyId}")
    public ResponseEntity<DataResponse> getCompanyPassword(@PathVariable int companyId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(companyService.getCompanyPassword(companyId, headers), HttpStatus.OK);
    }





//    @PutMapping("{companyId}")
//    public ResponseEntity<MessageResponse> updateCompany( @RequestBody CompanyUpdateDto companyDto, @PathVariable int companyId, @RequestHeader Map<String, String> headers) {
//        return new ResponseEntity<>(companyService.updateCompany(companyDto, companyId, headers), HttpStatus.OK);
//    }

    @PutMapping("/{companyId}")
    public ResponseEntity<MessageResponse> updateCompany(

            @PathVariable int companyId,

            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String ownerName,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) MultipartFile logoFile,

            @RequestParam(required = false) String email,
            @RequestParam(required = false) String cinNumber,
            @RequestParam(required = false) String lrNumber,
            @RequestParam(required = false) String transport,
            @RequestParam(required = false) String commissionerate,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String division,

            @RequestParam(required = false) String mobileNumber,
            @RequestParam(required = false) String alternateMobileNumber,

            @RequestParam(required = false) String billingAddress1,
            @RequestParam(required = false) String billingAddress2,
            @RequestParam(required = false) String billingAddress3,
            @RequestParam(required = false) Integer billingStateId,
            @RequestParam(required = false) Integer billingCityId,
            @RequestParam(required = false) String billingPincode,

            @RequestParam(required = false) String panNumber,
            @RequestParam(required = false) String gstNumber,
            @RequestParam(required = false) String serviceDescription,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) Boolean isDeleted,

            @RequestParam(required = false) List<String> bankName,
            @RequestParam(required = false) List<String> ifscCode,
            @RequestParam(required = false) List<String> branch,
            @RequestParam(required = false) List<String> accountNumber,
            @RequestParam(required = false) List<String> accHolderName,
            @RequestParam(required = false) List<String> bankAddress,
            @RequestParam(required = false) List<Boolean> bankStatus,
            @RequestParam(required = false) List<Boolean> bankIsDeleted,

            @RequestHeader Map<String, String> headers
    ) {

        MessageResponse response = companyService.updateCompany(
                companyName, ownerName, password, logoFile,
                email, cinNumber, lrNumber, transport,
                commissionerate, range, division,
                mobileNumber, alternateMobileNumber,
                billingAddress1, billingAddress2, billingAddress3,
                billingStateId, billingCityId, billingPincode,
                panNumber, gstNumber, serviceDescription, industry,
                status, isDeleted,
                bankName, ifscCode, branch, accountNumber,
                accHolderName, bankAddress, bankStatus, bankIsDeleted,
                companyId, headers
        );

        return ResponseEntity.ok(response);
    }



//    @PutMapping("uploadLogoPicture/{companyId}")
//    public ResponseEntity<MessageResponse> uploadLogoPicture(@RequestParam(name = "file", required = false) MultipartFile file, @RequestHeader Map<String, String> headers) throws IOException {
//        return new ResponseEntity<>(companyService.uploadLogoPicture(file, headers), HttpStatus.OK);
//    }


    @PutMapping("uploadLogoPicture/{companyId}")
    public ResponseEntity<MessageResponse> uploadLogoPicture(
            @RequestParam(name = "file", required = false) MultipartFile file,
            @PathVariable int companyId,
            @RequestHeader Map<String, String> headers) throws IOException {

        return new ResponseEntity<>(companyService.uploadLogoPicture(file, companyId, headers), HttpStatus.OK);
    }


    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
//    @DeleteMapping("hardDelete/{companyId}")
//    public ResponseEntity<MessageResponse> deleteCompanyById(@PathVariable int companyId, @RequestHeader Map<String, String> headers) {
//        return new ResponseEntity<>(companyService.deleteCountryById(companyId, headers), HttpStatus.OK);
//    }


    @PutMapping("softDelete/{companyId}")
    public ResponseEntity<MessageResponse> softDeleteCompanyById(@PathVariable int companyId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(companyService.softDeleteCompanyById(companyId, headers), HttpStatus.OK);
    }

    @PutMapping("restore/{companyId}")
    public ResponseEntity<MessageResponse> restoreCompanyById(@PathVariable int companyId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(companyService.restoreCompanyById(companyId, headers), HttpStatus.OK);
    }
}