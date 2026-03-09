package com.vaistra.service;

import com.vaistra.dto.CustomerDto;
import com.vaistra.dto.response.DataResponse;
import com.vaistra.dto.response.HttpResponse;
import com.vaistra.dto.response.MessageResponse;
import com.vaistra.dto.update.CustomerUpdateDto;
import jakarta.validation.Valid;

import java.util.Map;

public interface CustomerService {

    MessageResponse addCustomer( CustomerDto dto, Map<String, String> headers);

    HttpResponse getAllCustomers(Integer companyId, String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, String isDeleted, String status, Map<String, String> headers);

    DataResponse getCustomerById(int customerId, Map<String, String> headers);

    MessageResponse updateCustomer(CustomerUpdateDto updateDto, int customerId, Map<String, String> headers);

    MessageResponse softDeleteCustomerById(int customerId, Map<String, String> headers);

    MessageResponse restoreCustomerById(int customerId, Map<String, String> headers);


}
