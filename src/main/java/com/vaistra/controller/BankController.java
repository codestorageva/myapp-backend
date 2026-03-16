package com.vaistra.controller;

import com.vaistra.dto.BankDetailsDto;
import com.vaistra.dto.response.DataResponse;
import com.vaistra.dto.response.HttpResponse;
import com.vaistra.dto.response.MessageResponse;
import com.vaistra.dto.update.BankUpdateDto;
import com.vaistra.service.BankService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/bank")
@CrossOrigin(origins = "*")
public class BankController {


    //---------------------------------------------------CONSTRUCTOR INJECTION------------------------------------------

    private final BankService bankService;

    @Autowired
    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> addBankDetails(@Valid @RequestBody BankDetailsDto bank, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(bankService.addBankDetails(bank, headers), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<HttpResponse> getAllBankDetails(@RequestParam(value = "keyword", required = false) String keyword,
                                                        @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
                                                        @RequestParam(value = "pageSize", defaultValue = "2147483647", required = false) Integer pageSize,
                                                        @RequestParam(value = "sortBy", defaultValue = "bankId", required = false) String sortBy,
                                                        @RequestParam(value = "sortDirection", defaultValue = "asc", required = false) String sortDirection,
                                                        @RequestParam(value = "isDeleted", defaultValue = "false", required = false) String isDeleted,
                                                        @RequestParam(value = "status", defaultValue = "true", required = false) String status,
                                                        @RequestHeader Map<String, String> headers) {

        return new ResponseEntity<>(bankService.getAllBankDetails(keyword, pageNumber, pageSize, sortBy, sortDirection, isDeleted, status, headers), HttpStatus.OK);
    }


    @GetMapping("{bankId}")
    public ResponseEntity<DataResponse> getBankDetailsById(@PathVariable int bankId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(bankService.getBankDetailsById(bankId, headers), HttpStatus.OK);
    }



    @PutMapping("{bankId}")
    public ResponseEntity<MessageResponse> updateBankDetails(@RequestBody BankUpdateDto bankDto, @PathVariable int bankId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(bankService.updateBankDetails(bankDto, bankId, headers), HttpStatus.OK);
    }


    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
//    @DeleteMapping("hardDelete/{companyId}")
//    public ResponseEntity<MessageResponse> deleteCompanyById(@PathVariable int companyId, @RequestHeader Map<String, String> headers) {
//        return new ResponseEntity<>(companyService.deleteCountryById(companyId, headers), HttpStatus.OK);
//    }


    @PutMapping("softDelete/{bankId}")
    public ResponseEntity<MessageResponse> softDeleteBankDetailsById(@PathVariable int bankId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(bankService.softDeleteBankDetailsById(bankId, headers), HttpStatus.OK);
    }

    @PutMapping("restore/{bankId}")
    public ResponseEntity<MessageResponse> restoreBankDetailsById(@PathVariable int bankId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(bankService.restoreBankDetailsById(bankId, headers), HttpStatus.OK);
    }
}
