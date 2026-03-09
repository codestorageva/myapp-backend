package com.vaistra.controller;

import com.vaistra.dto.CustomerDto;
import com.vaistra.dto.InvoiceDto;
import com.vaistra.dto.response.DataResponse;
import com.vaistra.dto.response.HttpResponse;
import com.vaistra.dto.response.MessageResponse;
import com.vaistra.dto.update.CustomerUpdateDto;
import com.vaistra.dto.update.InvoiceUpdateDto;
import com.vaistra.service.CustomerService;
import com.vaistra.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/customer")
@CrossOrigin(origins = "*")
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> addCustomer(@Valid @RequestBody CustomerDto dto, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(customerService.addCustomer(dto, headers), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<HttpResponse> getAllCustomers(
            @RequestParam(value = "companyId") Integer companyId, // <-- added
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "2147483647", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "customerId", required = false) String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "asc", required = false) String sortDirection,
            @RequestParam(value = "isDeleted", defaultValue = "false", required = false) String isDeleted,
            @RequestParam(value = "status", defaultValue = "true", required = false) String status,
            @RequestHeader Map<String, String> headers) {

        return new ResponseEntity<>(
                customerService.getAllCustomers(companyId, keyword, pageNumber, pageSize, sortBy, sortDirection, isDeleted, status, headers),
                HttpStatus.OK
        );
    }

    @GetMapping("{customerId}")
    public ResponseEntity<DataResponse> getCustomerById(@PathVariable int customerId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(customerService.getCustomerById(customerId, headers), HttpStatus.OK);
    }



    @PutMapping("{customerId}")
    public ResponseEntity<MessageResponse> updateCustomer(@RequestBody CustomerUpdateDto updateDto, @PathVariable int customerId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(customerService.updateCustomer(updateDto, customerId, headers), HttpStatus.OK);
    }

    @PutMapping("softDelete/{customerId}")
    public ResponseEntity<MessageResponse> softDeleteCustomerById(@PathVariable int customerId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(customerService.softDeleteCustomerById(customerId, headers), HttpStatus.OK);
    }

    @PutMapping("restore/{customerId}")
    public ResponseEntity<MessageResponse> restoreCustomerById(@PathVariable int customerId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(customerService.restoreCustomerById(customerId, headers), HttpStatus.OK);
    }
}
