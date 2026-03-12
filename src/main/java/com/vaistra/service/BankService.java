package com.vaistra.service;

import com.vaistra.dto.BankDetailsDto;
import com.vaistra.dto.response.DataResponse;
import com.vaistra.dto.response.HttpResponse;
import com.vaistra.dto.response.MessageResponse;
import com.vaistra.dto.update.BankUpdateDto;

import java.util.Map;

public interface BankService {

    MessageResponse addBankDetails( BankDetailsDto bank, Map<String, String> headers);

    HttpResponse getAllBankDetails(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, String isDeleted, String status, Map<String, String> headers);

    DataResponse getBankDetailsById(int bankId, Map<String, String> headers);

    MessageResponse updateBankDetails(BankUpdateDto bankDto, int bankId, Map<String, String> headers);

    MessageResponse softDeleteBankDetailsById(int bankId, Map<String, String> headers);

    MessageResponse restoreBankDetailsById(int bankId, Map<String, String> headers);
}
