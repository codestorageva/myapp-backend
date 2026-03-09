package com.vaistra.service;

import com.vaistra.dto.InvoiceDto;
import com.vaistra.dto.response.DataResponse;
import com.vaistra.dto.response.HttpResponse;
import com.vaistra.dto.response.InvoiceResponse;
import com.vaistra.dto.response.MessageResponse;
import com.vaistra.dto.update.InvoiceUpdateDto;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface InvoiceService {

    public InvoiceResponse addInvoice(InvoiceDto invoice, Integer companyId);



    HttpResponse getAllInvoices( Integer companyId,String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, String isDeleted, String status, Map<String, String> headers);

    MessageResponse updateInvoice(InvoiceUpdateDto invoiceUpdateDto, int invoiceId, Map<String, String> headers);

    DataResponse getInvoiceById(int invoiceId, Map<String, String> headers);

    MessageResponse softDeleteInvoiceById(int invoiceId, Map<String, String> headers);

    MessageResponse restoreInvoiceById(int invoiceId, Map<String, String> headers);

    HttpResponse getAllInvoiceReports(
            String keyword,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortDirection,
            Map<String, String> headers
    );
    void saveExcelData(MultipartFile file) throws Exception;

    HttpResponse getAllSoftDeletedInvoices(
            Integer companyId,
            String keyword,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortDirection,
            Map<String, String> headers
    );

}
