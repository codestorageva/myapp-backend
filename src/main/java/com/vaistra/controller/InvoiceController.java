package com.vaistra.controller;

import com.vaistra.dto.BankDetailsDto;
import com.vaistra.dto.InvoiceDto;
import com.vaistra.dto.response.DataResponse;
import com.vaistra.dto.response.HttpResponse;
import com.vaistra.dto.response.InvoiceResponse;
import com.vaistra.dto.response.MessageResponse;
import com.vaistra.dto.update.BankUpdateDto;
import com.vaistra.dto.update.InvoiceUpdateDto;
import com.vaistra.service.InvoiceReportService;
import com.vaistra.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/invoice")

@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceReportService invoiceReportService;

    public InvoiceController(InvoiceService invoiceService, InvoiceReportService invoiceReportService) {
        this.invoiceService = invoiceService;
        this.invoiceReportService = invoiceReportService;
    }


    @PostMapping
    public ResponseEntity<InvoiceResponse> addInvoice(@Valid @RequestBody InvoiceDto invoice) {
        if (invoice.getCompanyId() == null) {
            return new ResponseEntity<>(
                    new InvoiceResponse(false, HttpStatus.BAD_REQUEST, "Company ID is required.", null),
                    HttpStatus.BAD_REQUEST
            );
        }

        return new ResponseEntity<>(
                invoiceService.addInvoice(invoice, invoice.getCompanyId()),
                HttpStatus.OK
        );
    }



    @GetMapping
    public ResponseEntity<HttpResponse> getAllInvoices(@RequestParam Integer companyId,@RequestParam(value = "keyword", required = false) String keyword,
                                                          @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
                                                          @RequestParam(value = "pageSize", defaultValue = "2147483647", required = false) Integer pageSize,
                                                          @RequestParam(value = "sortBy", defaultValue = "invoiceId", required = false) String sortBy,
                                                          @RequestParam(value = "sortDirection", defaultValue = "asc", required = false) String sortDirection,
                                                          @RequestParam(value = "isDeleted", defaultValue = "false", required = false) String isDeleted,
                                                          @RequestParam(value = "status", defaultValue = "true", required = false) String status,
                                                          @RequestHeader Map<String, String> headers) {

        return new ResponseEntity<>(invoiceService.getAllInvoices(companyId,keyword, pageNumber, pageSize, sortBy, sortDirection, isDeleted, status, headers), HttpStatus.OK);
    }


    @GetMapping("{invoiceId}")
    public ResponseEntity<DataResponse> getInvoiceById(@PathVariable int invoiceId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(invoiceService.getInvoiceById(invoiceId, headers), HttpStatus.OK);
    }



    @PutMapping("{invoiceId}")
    public ResponseEntity<MessageResponse> updateInvoice(
            @RequestBody InvoiceUpdateDto invoiceUpdateDto,
            @PathVariable int invoiceId,
            @RequestHeader Map<String, String> headers) {

        System.out.println("UPDATE API CALLED");

        MessageResponse response =
                invoiceService.updateInvoice(invoiceUpdateDto, invoiceId, headers);

        System.out.println("Response: " + response);

        return ResponseEntity.ok(response);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
//    @DeleteMapping("hardDelete/{invoiceId}")
//    public ResponseEntity<MessageResponse> deleteInvoiceById(@PathVariable int invoiceId, @RequestHeader Map<String, String> headers) {
//        return new ResponseEntity<>(invoiceService.deleteInvoiceById(invoiceId, headers), HttpStatus.OK);
//    }


    @PutMapping("softDelete/{invoiceId}")
    public ResponseEntity<MessageResponse> softDeleteInvoiceById(@PathVariable int invoiceId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(invoiceService.softDeleteInvoiceById(invoiceId, headers), HttpStatus.OK);
    }

    @PutMapping("restore/{invoiceId}")
    public ResponseEntity<MessageResponse> restoreInvoiceById(@PathVariable int invoiceId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(invoiceService.restoreInvoiceById(invoiceId, headers), HttpStatus.OK);
    }
//    @GetMapping("/invoice-report")
//    public ResponseEntity<byte[]> downloadInvoiceReport(@RequestHeader Map<String, String> headers) throws Exception {
//
//        byte[] excelData = invoiceReportService.generateInvoiceReport();
//
//        System.out.println("Auth Header = " + headers.get("authorization"));
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_report.xlsx")
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .body(excelData);
//    }
@GetMapping("/invoice-report")
public ResponseEntity<?> generateReport() {

    try {
        byte[] excelBytes = invoiceReportService.generateInvoiceReport();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);

    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Report generation failed"));
    }
}
    @GetMapping("/invoice-report-list")
    public HttpResponse getAllInvoiceReports(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "invoiceDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestHeader Map<String, String> headers) {

        return invoiceService.getAllInvoiceReports(
                keyword, pageNumber, pageSize, sortBy, sortDirection, headers);
    }

    @PostMapping("/upload-excel")
    public ResponseEntity<Map<String, Object>> uploadExcel(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "File is empty"
            ));
        }

        try {
            invoiceService.saveExcelData(file);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "File Uploaded Successfully"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    @GetMapping("/soft-deleted")
    public ResponseEntity<HttpResponse> getAllSoftDeletedInvoices(
            @RequestParam Integer companyId, // mandatory
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "invoiceDate") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "desc") String sortDirection,
            @RequestHeader Map<String, String> headers
    ) {
        HttpResponse response = invoiceService.getAllSoftDeletedInvoices(
                companyId, keyword, pageNumber, pageSize, sortBy, sortDirection, headers
        );
        return ResponseEntity.ok(response);
    }

}
