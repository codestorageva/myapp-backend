package com.vaistra.service.impl;
import com.vaistra.dto.InvoiceDto;
import com.vaistra.dto.InvoiceItemDTO;
import com.vaistra.dto.InvoiceReportDto;
import org.apache.poi.ss.usermodel.*;

import com.vaistra.dto.response.DataResponse;
import com.vaistra.dto.response.HttpResponse;
import com.vaistra.dto.response.InvoiceResponse;
import com.vaistra.dto.response.MessageResponse;
import com.vaistra.dto.update.InvoiceUpdateDto;
import com.vaistra.entity.*;
import com.vaistra.exception.DuplicateEntryException;
import com.vaistra.exception.ResourceNotFoundException;
import com.vaistra.repository.CompanyRepository;
import com.vaistra.repository.CustomerRepository;
import com.vaistra.repository.InvoiceRepository;
import com.vaistra.repository.ProductRepository;
import com.vaistra.service.InvoiceService;
import com.vaistra.util.AppUtils;
import jakarta.persistence.EntityManager;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Character.getNumericValue;

@Service
@Transactional
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    @PersistenceContext
    private EntityManager entityManager;
    private final InvoiceRepository invoiceRepository;
    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final AppUtils appUtils;
    private final HttpServletRequest request;

    @Autowired
    private RestTemplate restTemplate;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, CompanyRepository companyRepository, ProductRepository productRepository, CustomerRepository customerRepository, AppUtils appUtils, HttpServletRequest request) {
        this.invoiceRepository = invoiceRepository;
        this.companyRepository = companyRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.appUtils = appUtils;
        this.request = request;
    }

//    @Override
//    public InvoiceResponse addInvoice(InvoiceDto i, Map<String, String> headers) {
//        System.out.println("Inside implementation");
//        InvoiceGenerator savedInvoice = null;
//
//        try {
//            System.out.println("Company ID: " + i.getCompanyId());
//
//            if (i.getCompanyId() == null) {
//                throw new IllegalArgumentException("Company ID is required.");
//            }
//
//            CompanyRegistration company = companyRepository.findById(i.getCompanyId())
//                    .orElseThrow(() -> new RuntimeException("Company not found with ID: " + i.getCompanyId()));
//
//            if (i.getCustomerId() != null) {
//                customerRepository.findById(i.getCustomerId())
//                        .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + i.getCustomerId()));
//            }
//
//            String prefix = (i.getInvoicePrefix() != null && !i.getInvoicePrefix().isBlank())
//                    ? i.getInvoicePrefix().toUpperCase()
//                    : "VV";
//
//            String invoiceNumber;
//
//            if (i.getInvoiceNumber() != null && !i.getInvoiceNumber().isBlank()) {
//                invoiceNumber = i.getInvoiceNumber().trim();
//                String fullInvoiceNumber = prefix + "-" + invoiceNumber;
//
//                if (invoiceRepository.existsByInvoiceNumberIgnoreCase(fullInvoiceNumber)) {
//                    throw new DuplicateEntryException("Invoice number '" + fullInvoiceNumber + "' already exists!");
//                }
//            } else {
//                long count = invoiceRepository.countByInvoicePrefix(prefix);
//                invoiceNumber = String.format("%05d", count + 1);
//            }
//
//            System.out.println("Final Invoice Number: " + prefix + "-" + invoiceNumber);
//
//            InvoiceGenerator invoice = new InvoiceGenerator();
//            invoice.setInvoicePrefix(prefix);
//            invoice.setInvoiceNumber(invoiceNumber);
//            invoice.setInvoiceDate(i.getInvoiceDate());
//            invoice.setTerms(i.getTerms() != null ? i.getTerms() : "Default Terms");
//            invoice.setDueDate(i.getDueDate());
//            invoice.setPaymentMode(i.getPaymentMode());
//            invoice.setNarration(i.getNarration());
//
//            invoice.setLrNumber(i.getLrNumber());
//            invoice.setTransport(i.getTransport());
//            invoice.setCommissionerate(i.getCommissionerate());
//            invoice.setRange(i.getRange());
//            invoice.setDivision(i.getDivision());
//
//
//            invoice.setCompanyRegistration(company);
//
//            invoice.setCustomer(i.getCustomerId() != null
//                    ? customerRepository.findById(i.getCustomerId())
//                    .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + i.getCustomerId()))
//                    : null);
//
//            invoice.setStatus(i.getStatus() != null ? i.getStatus() : true);
//            invoice.setIsDeleted(false);
//            invoice.setCreatedAt(LocalDateTime.now());
//            invoice.setUpdatedAt(LocalDateTime.now());
//            invoice.setDeletedAt(null);
//
//            boolean isRCM = i.getIsRcm() != null && i.getIsRcm();
//
//            // Variables for totals
//            double totalTaxableAmount = 0.0;
//            double totalCgst = 0.0;
//            double totalSgst = 0.0;
//            double totalIgst = 0.0;
//
//            List<InvoiceItem> invoiceItems = new ArrayList<>();
//
//            if (i.getItems() == null || i.getItems().isEmpty()) {
//                throw new IllegalArgumentException("Invoice must contain at least one item.");
//            }
//
//            for (InvoiceItemDTO itemDTO : i.getItems()) {
//                Product product = productRepository.findById(itemDTO.getProductId())
//                        .orElseThrow(() -> new RuntimeException("Product not found with ID: " + itemDTO.getProductId()));
//
//                double rate = itemDTO.getRate() > 0 ? itemDTO.getRate() : product.getSellingPrice();
//                double quantity = itemDTO.getQuantity();
////                double taxableAmount = rate * quantity;
//                double baseAmount = rate * quantity;
//
//                double royalty = itemDTO.getRoyalty() != null ? itemDTO.getRoyalty() : 0.0;
//                double dmf = itemDTO.getDmf() != null ? itemDTO.getDmf() : 0.0;
//                double nmet = itemDTO.getNmet() != null ? itemDTO.getNmet() : 0.0;
//
//// Multiply with quantity
//                double royaltyTotal = royalty * quantity;
//                double dmfTotal = dmf * quantity;
//                double nmetTotal = nmet * quantity;
//
//// Final taxable amount
//                double taxableAmount = baseAmount + royaltyTotal + dmfTotal + nmetTotal;
//
//
//                double gstPercent = 0.0;
//                String gstStr = product.getGstPercent();
//                if (gstStr != null && !gstStr.isBlank()) {
//                    try {
//                        gstPercent = Double.parseDouble(gstStr.replace("%", "").trim());
//                    } catch (NumberFormatException e) {
//                        throw new IllegalArgumentException("Invalid GST percent for product ID: " + product.getProductId());
//                    }
//                }
//
//                double cgstPercent = 0.0, sgstPercent = 0.0, igstPercent = 0.0;
//                double cgstAmount = 0.0, sgstAmount = 0.0, igstAmount = 0.0;
//
//                Customer customer = customerRepository.findById(i.getCustomerId())
//                        .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + i.getCustomerId()));
//
//                String placeOfSupply = customer.getPlaceOfSupply().getStateName();
//
//                if ("Gujarat".equalsIgnoreCase(placeOfSupply)) {
//                    cgstPercent = gstPercent / 2;
//                    sgstPercent = gstPercent / 2;
//                    cgstAmount = taxableAmount * cgstPercent / 100;
//                    sgstAmount = taxableAmount * sgstPercent / 100;
//                } else {
//                    igstPercent = gstPercent;
//                    igstAmount = taxableAmount * igstPercent / 100;
//                }
//
//                // Always add taxable amount to totalTaxableAmount
//                totalTaxableAmount += taxableAmount;
//
//                // Always store tax values for display, but only include in totals if not RCM
//                totalCgst += cgstAmount;
//                totalSgst += sgstAmount;
//                totalIgst += igstAmount;
//
//                InvoiceItem item = new InvoiceItem();
//                item.setProduct(product);
//                item.setInvoice(invoice);
//                item.setQuantity(quantity);
//                item.setRate(rate);
//                item.setRoyalty(royalty);
//                item.setDmf(dmf);
//                item.setNmet(nmet);
//
//                item.setRoyaltyAmount(royaltyTotal);
//                item.setDmfAmount(dmfTotal);
//                item.setNmetAmount(nmetTotal);
//
//                item.setTaxableAmount(taxableAmount);
//
//                item.setTaxableAmount(taxableAmount);
//                item.setCgstPercent(cgstPercent);
//                item.setSgstPercent(sgstPercent);
//                item.setIgstPercent(igstPercent);
//                item.setCgstAmount(cgstAmount);
//                item.setSgstAmount(sgstAmount);
//                item.setIgstAmount(igstAmount);
//
//                invoiceItems.add(item);
//            }
//            // Step 1: Calculate visible tax amounts (always for display)
//            double visibleTotalCgst = totalCgst;
//            double visibleTotalSgst = totalSgst;
//            double visibleTotalIgst = totalIgst;
//
//            // Step 2: Calculate the actual total to use in grandTotal
//            double grossTotalForCalculation = totalTaxableAmount;
//
//            if (!isRCM) {
//                grossTotalForCalculation += totalCgst + totalSgst + totalIgst;
//            }
//
//            // Step 3: Subtract other charges if any
//            double otherChargesTotal = 0.0;
//            if (i.getOtherCharge() != null && !i.getOtherCharge().isEmpty()) {
//                for (OtherChargeDto ocDto : i.getOtherCharge()) {
//                    if (ocDto.getValue() != null) {
//                        otherChargesTotal += ocDto.getValue();
//                    }
//                }
//            }
//            double totalBeforeRound = grossTotalForCalculation - otherChargesTotal;
//
//            // Step 4: Round values
//            BigDecimal bdActualTotal = BigDecimal.valueOf(totalBeforeRound).setScale(2, RoundingMode.HALF_UP);
//            BigDecimal bdRoundedTotal = bdActualTotal.setScale(0, RoundingMode.HALF_UP);  // round to nearest integer
//            BigDecimal bdRoundOff = bdRoundedTotal.subtract(bdActualTotal).setScale(2, RoundingMode.HALF_UP);
//            BigDecimal bdGrandTotal = bdActualTotal.add(bdRoundOff).setScale(2, RoundingMode.HALF_UP);
//
//            // Step 5: Set values on invoice (show actual tax even if RCM)
//            invoice.setItems(invoiceItems);
//            invoice.setTotalTaxableAmount(totalTaxableAmount);
//            invoice.setTotalCgst(visibleTotalCgst);
//            invoice.setTotalSgst(visibleTotalSgst);
//            invoice.setTotalIgst(visibleTotalIgst);
//            invoice.setIsRcm(isRCM);
//            invoice.setRoundOff(bdRoundOff.doubleValue());
//            invoice.setGrandTotal(bdGrandTotal.doubleValue());
//
//
//            // Set other charges
//            List<OtherCharges> otherChargeList = new ArrayList<>();
//            if (i.getOtherCharge() != null && !i.getOtherCharge().isEmpty()) {
//                for (OtherChargeDto ocDto : i.getOtherCharge()) {
//                    OtherCharges oc = new OtherCharges();
//                    oc.setLabel(ocDto.getLabel());
//                    oc.setValue(ocDto.getValue());
//                    oc.setInvoice(invoice);
//                    otherChargeList.add(oc);
//                }
//            }
//            invoice.setOtherCharges(otherChargeList);
//
//
//
//            log.info("Saving invoice: {}", invoice.getInvoicePrefix() + "-" + invoice.getInvoiceNumber());
//            savedInvoice = invoiceRepository.save(invoice);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Integer invoiceId = savedInvoice != null ? savedInvoice.getInvoiceId() : null;
//            return new InvoiceResponse(false, HttpStatus.BAD_REQUEST, "Error: " + e.getMessage(), invoiceId);
//        }
//
//        return new InvoiceResponse(true, HttpStatus.OK, "Invoice created successfully", savedInvoice.getInvoiceId());
//    }


@Override
public InvoiceResponse addInvoice(InvoiceDto invoice, Integer companyId) {

    InvoiceGenerator savedInvoice;

    try {
        if (companyId == null) {
            throw new IllegalArgumentException("Company ID is required.");
        }

        CompanyRegistration company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Customer customer = null;
        if (invoice.getCustomerId() != null) {
            customer = customerRepository.findById(invoice.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
        }

        // ===== Invoice Prefix & Number =====
        String prefix = (invoice.getInvoicePrefix() != null && !invoice.getInvoicePrefix().isBlank())
                ? invoice.getInvoicePrefix().toUpperCase()
                : "VV";

        String invoiceNumber;
        if (invoice.getInvoiceNumber() != null && !invoice.getInvoiceNumber().isBlank()) {
            invoiceNumber = invoice.getInvoiceNumber().trim();
            if (invoiceRepository.existsByInvoiceNumberIgnoreCase(prefix + "-" + invoiceNumber)) {
                throw new RuntimeException("Invoice number already exists");
            }
        } else {
            long count = invoiceRepository.countByInvoicePrefix(prefix);
            invoiceNumber = String.format("%05d", count + 1);
        }

        // ===== Invoice Entity =====
        InvoiceGenerator invoiceEntity = new InvoiceGenerator();
        invoiceEntity.setInvoicePrefix(prefix);
        invoiceEntity.setInvoiceNumber(invoiceNumber);
        invoiceEntity.setInvoiceDate(invoice.getInvoiceDate());
        invoiceEntity.setDueDate(invoice.getDueDate());
        invoiceEntity.setTerms(invoice.getTerms());
        invoiceEntity.setPaymentMode(invoice.getPaymentMode());
        invoiceEntity.setNarration(invoice.getNarration());
        invoiceEntity.setLrNumber(invoice.getLrNumber());
        invoiceEntity.setTransport(invoice.getTransport());
        invoiceEntity.setCommissionerate(invoice.getCommissionerate());
        invoiceEntity.setRange(invoice.getRange());
        invoiceEntity.setDivision(invoice.getDivision());
        invoiceEntity.setCompanyRegistration(company);
        invoiceEntity.setCustomer(customer);
        invoiceEntity.setIsRcm(Boolean.TRUE.equals(invoice.getIsRcm()));
        invoiceEntity.setStatus(true);
        invoiceEntity.setIsDeleted(false);
        invoiceEntity.setCreatedAt(LocalDateTime.now());
        invoiceEntity.setUpdatedAt(LocalDateTime.now());

        double totalTaxableAmount = 0;
        double totalCgst = 0;
        double totalSgst = 0;
        double totalIgst = 0;

        List<InvoiceItem> invoiceItems = new ArrayList<>();

        if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
            throw new IllegalArgumentException("Invoice must contain items");
        }

        // ===== ITEM LOOP =====
        for (InvoiceItemDTO itemDTO : invoice.getItems()) {

            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            double qty = itemDTO.getQuantity();
            System.out.println("quantity"+qty);
            double rate = itemDTO.getRate() > 0 ? itemDTO.getRate() : product.getSellingPrice();

            // ===== BASE AMOUNT =====
            double baseAmount = rate * qty;
            System.out.println(baseAmount);

            // ===== MINING (FIXED LOGIC) =====
            double royaltyRate = itemDTO.getRoyalty() != null ? itemDTO.getRoyalty() : 0;
//            double dmfPercent = itemDTO.getDmf() != null ? itemDTO.getDmf() : 0;
//            double nmetPercent = itemDTO.getNmet() != null ? itemDTO.getNmet() : 0;
            double dmfPercent=royaltyRate*30/100;
            double nmetPercent=royaltyRate*2/100;

            double royaltyAmount = royaltyRate * qty;
            System.out.println("royalty amount:"+royaltyAmount);
            double dmfAmount = qty*dmfPercent;
            System.out.println("dmfamount"+dmfAmount);
            double nmetAmount = qty*nmetPercent;
            System.out.println("nemtamount"+nmetAmount);


            double taxableAmount =
                    baseAmount + royaltyAmount + dmfAmount + nmetAmount;
            System.out.println(taxableAmount);

            // ===== GST =====
            double gstPercent = 0;
            if (product.getGstPercent() != null && !product.getGstPercent().isBlank()) {
                gstPercent = Double.parseDouble(
                        product.getGstPercent().replace("%", "")
                );
            }

            double cgstPercent = 0, sgstPercent = 0, igstPercent = 0;
            double cgstAmount = 0, sgstAmount = 0, igstAmount = 0;

            if (customer != null &&
                    customer.getPlaceOfSupply() != null &&
                    "Gujarat".equalsIgnoreCase(customer.getPlaceOfSupply().getStateName())) {

                cgstPercent = gstPercent / 2;
                sgstPercent = gstPercent / 2;

                cgstAmount = taxableAmount * cgstPercent / 100;
                sgstAmount = taxableAmount * sgstPercent / 100;

            } else {
                igstPercent = gstPercent;
                igstAmount = taxableAmount * igstPercent / 100;
            }

            totalTaxableAmount += taxableAmount;
            totalCgst += cgstAmount;
            totalSgst += sgstAmount;
            totalIgst += igstAmount;

            // ===== Invoice Item =====
            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoiceEntity);
            item.setProduct(product);
            item.setQuantity(qty);
            item.setRate(rate);

            item.setBaseAmount(baseAmount);

            item.setRoyalty(royaltyRate);
            item.setDmf(dmfPercent);
            item.setNmet(nmetPercent);

            item.setRoyaltyAmount(royaltyAmount);
            item.setDmfAmount(dmfAmount);
            item.setNmetAmount(nmetAmount);

            item.setTaxableAmount(taxableAmount);

            item.setCgstPercent(cgstPercent);
            item.setSgstPercent(sgstPercent);
            item.setIgstPercent(igstPercent);

            item.setCgstAmount(cgstAmount);
            item.setSgstAmount(sgstAmount);
            item.setIgstAmount(igstAmount);

            invoiceItems.add(item);
        }

        // ===== TOTALS =====
        invoiceEntity.setItems(invoiceItems);
        invoiceEntity.setTotalTaxableAmount(totalTaxableAmount);
        invoiceEntity.setTotalCgst(totalCgst);
        invoiceEntity.setTotalSgst(totalSgst);
        invoiceEntity.setTotalIgst(totalIgst);

        double grandTotal = totalTaxableAmount + totalCgst + totalSgst + totalIgst;
//        double roundOff = Math.round(grandTotal) - grandTotal;
        double roundOff = (invoice.getRoundOff() != null) ? invoice.getRoundOff() : (Math.round(grandTotal) - grandTotal);


        invoiceEntity.setRoundOff(roundOff);
        invoiceEntity.setGrandTotal(grandTotal + roundOff);

        savedInvoice = invoiceRepository.save(invoiceEntity);

        return new InvoiceResponse(
                true,
                HttpStatus.OK,
                "Invoice created successfully",
                savedInvoice.getInvoiceId()
        );

    } catch (Exception e) {
        e.printStackTrace();
        return new InvoiceResponse(
                false,
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                null
        );
    }
}


    @Override
    public HttpResponse getAllInvoices(
            Integer companyId,
            String keyword,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortDirection,
            String isDeleted,
            String status,
            Map<String, String> headers) {

        Page<InvoiceGenerator> pageInvoice = null;
        List<InvoiceDto> dtos = null;

        Boolean softDeleted = null;
        Boolean activeStatus = null;
        Integer intKeyword = null;

        // ----------------------------
        // Safe Boolean Conversion
        // ----------------------------
        if ("true".equalsIgnoreCase(isDeleted)) {
            softDeleted = true;
        } else if ("false".equalsIgnoreCase(isDeleted)) {
            softDeleted = false;
        }

        if ("true".equalsIgnoreCase(status)) {
            activeStatus = true;
        } else if ("false".equalsIgnoreCase(status)) {
            activeStatus = false;
        }

        // ----------------------------
        // Keyword Integer Parse
        // ----------------------------
        try {
            if (keyword != null)
                intKeyword = Integer.parseInt(keyword);
        } catch (Exception ignored) {
        }

        try {

            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<InvoiceGenerator> cq = cb.createQuery(InvoiceGenerator.class);
            Root<InvoiceGenerator> root = cq.from(InvoiceGenerator.class);

            List<Predicate> predicates = new ArrayList<>();

            // ----------------------------
            // Company Filter (MOST IMPORTANT)
            // ----------------------------
            if (companyId != null) {
                predicates.add(
                        cb.equal(
                                root.get("companyRegistration").get("companyId"),
                                companyId
                        )
                );
            }

            // ----------------------------
            // Status Filter
            // ----------------------------
            if (activeStatus != null) {
                predicates.add(cb.equal(root.get("status"), activeStatus));
            }

            // ----------------------------
            // Soft Delete Filter
            // ----------------------------
            if (softDeleted != null) {
                predicates.add(cb.equal(root.get("isDeleted"), softDeleted));
            }

            // ----------------------------
            // Keyword Filter
            // ----------------------------
            if (keyword != null && !keyword.isEmpty()) {

                if (intKeyword != null) {
                    predicates.add(cb.equal(root.get("invoiceId"), intKeyword));
                } else {
                    predicates.add(
                            cb.like(
                                    cb.lower(root.get("invoiceNumber")),
                                    "%" + keyword.toLowerCase() + "%"
                            )
                    );
                }
            }

            // ----------------------------
            // Sorting
            // ----------------------------
            Order order = "asc".equalsIgnoreCase(sortDirection)
                    ? cb.asc(root.get(sortBy))
                    : cb.desc(root.get(sortBy));

            cq.orderBy(order);

            // ----------------------------
            // Apply Predicates
            // ----------------------------
            if (!predicates.isEmpty()) {
                cq.where(cb.and(predicates.toArray(new Predicate[0])));
            }

            Pageable pageable = PageRequest.of(pageNumber, pageSize);

            List<InvoiceGenerator> resultList = entityManager.createQuery(cq)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();

            long totalCount = entityManager.createQuery(cq).getResultList().size();

            pageInvoice = new PageImpl<>(resultList, pageable, totalCount);

            dtos = appUtils.invoicesToDtos(resultList);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .pageNumber(pageInvoice.getNumber())
                .pageSize(pageInvoice.getSize())
                .totalElements(pageInvoice.getTotalElements())
                .totalPages(pageInvoice.getTotalPages())
                .isLastPage(pageInvoice.isLast())
                .data(dtos)
                .build();
    }


//    @Override
//    public MessageResponse updateInvoice(InvoiceUpdateDto invoiceUpdateDto, int invoiceId, Map<String, String> headers) {
//
//         // Fetch existing invoice
//        InvoiceGenerator existingInvoice = invoiceRepository.findById(invoiceId)
//                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));
//
//        // Fetch company and customer (same as addInvoice logic)
//        CompanyRegistration company = companyRepository.findById(invoiceUpdateDto.getCompanyId())
//                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + invoiceUpdateDto.getCompanyId()));
//
//        Customer customer = customerRepository.findById(invoiceUpdateDto.getCustomerId())
//                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + invoiceUpdateDto.getCustomerId()));
//
//        // Handle invoice number update (if provided)
//        String invoiceNumber = existingInvoice.getInvoiceNumber();
//        if (invoiceUpdateDto.getInvoiceNumber() != null && !invoiceUpdateDto.getInvoiceNumber().isBlank()) {
//            if (invoiceRepository.existsByInvoiceNumberIgnoreCase(invoiceUpdateDto.getInvoiceNumber())) {
//                throw new DuplicateEntryException("Invoice number '" + invoiceUpdateDto.getInvoiceNumber() + "' already exists!");
//            }
//            invoiceNumber = invoiceUpdateDto.getInvoiceNumber();
//        }
//
//        // Update invoice fields (same as addInvoice logic)
//        existingInvoice.setInvoicePrefix(invoiceUpdateDto.getInvoicePrefix() != null ? invoiceUpdateDto.getInvoicePrefix() : existingInvoice.getInvoicePrefix());
//        existingInvoice.setInvoiceNumber(invoiceNumber);
//        existingInvoice.setInvoiceDate(invoiceUpdateDto.getInvoiceDate());
//        existingInvoice.setTerms(invoiceUpdateDto.getTerms());
//        existingInvoice.setDueDate(invoiceUpdateDto.getDueDate());
//        existingInvoice.setPaymentMode(invoiceUpdateDto.getPaymentMode());
//        existingInvoice.setLrNumber(invoiceUpdateDto.getLrNumber());
//        existingInvoice.setTransport(invoiceUpdateDto.getTransport());
//        existingInvoice.setCommissionerate(invoiceUpdateDto.getCommissionerate());
//        existingInvoice.setRange(invoiceUpdateDto.getRange());
//        existingInvoice.setDivision(invoiceUpdateDto.getDivision());
//
//        existingInvoice.setCompanyRegistration(company);
//        existingInvoice.setCustomer(customer);
//        existingInvoice.setStatus(invoiceUpdateDto.getStatus() != null ? invoiceUpdateDto.getStatus() : true);
//        existingInvoice.setUpdatedAt(LocalDateTime.now());
//
//        // Process invoice items (same as addInvoice logic)
//        double totalTaxableAmount = 0.0;
//        double totalCgst = 0.0;
//        double totalSgst = 0.0;
//
//        List<InvoiceItem> updatedItems = new ArrayList<>();
//        for (InvoiceItemDTO itemDTO : invoiceUpdateDto.getItems()) {
//            Product product = productRepository.findById(itemDTO.getProductId())
//                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + itemDTO.getProductId()));
//
//            InvoiceItem item = new InvoiceItem();
//            item.setInvoice(existingInvoice);
//            item.setProduct(product);
//            item.setQuantity(itemDTO.getQuantity());
//
//            double rate = product.getSellingPrice();
//            double quantity = itemDTO.getQuantity();
//            double taxableAmount = rate * quantity;
//
//            double gstPercent = product.getTaxValue();
//            double cgstPercent = gstPercent / 2;
//            double sgstPercent = gstPercent / 2;
//
//            double cgstAmount = taxableAmount * cgstPercent / 100;
//            double sgstAmount = taxableAmount * sgstPercent / 100;
//
//            item.setRate(rate);
//            item.setTaxableAmount(taxableAmount);
//            item.setCgstPercent(cgstPercent);
//            item.setSgstPercent(sgstPercent);
//            item.setCgstAmount(cgstAmount);
//            item.setSgstAmount(sgstAmount);
//
//            totalTaxableAmount += taxableAmount;
//            totalCgst += cgstAmount;
//            totalSgst += sgstAmount;
//
//            updatedItems.add(item);
//        }
//
//        // Calculate grand total and round off (same as addInvoice logic)
//        double grandTotal = totalTaxableAmount + totalCgst + totalSgst;
//        double roundOff = Math.round(grandTotal) - grandTotal;
//
//        // Update the totals and items in the invoice
//        existingInvoice.setItems(updatedItems);
//        existingInvoice.setTotalTaxableAmount(totalTaxableAmount);
//        existingInvoice.setTotalCgst(totalCgst);
//        existingInvoice.setTotalSgst(totalSgst);
//        existingInvoice.setGrandTotal(Math.round(grandTotal));
//        existingInvoice.setRoundOff(roundOff);
//
//        // Save the updated invoice
//        invoiceRepository.save(existingInvoice);
//
//        return new MessageResponse(true, HttpStatus.OK, "Invoice updated successfully.");
//    }
@Override
@Transactional
public MessageResponse updateInvoice(InvoiceUpdateDto dto,
                                     int invoiceId,
                                     Map<String, String> headers) {

    InvoiceGenerator invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new RuntimeException("Invoice not found"));

    // ================= BASIC UPDATE =================
    invoice.setInvoiceDate(dto.getInvoiceDate());
    invoice.setDueDate(dto.getDueDate());
    invoice.setInvoicePrefix(dto.getInvoicePrefix());
    invoice.setInvoiceNumber(dto.getInvoiceNumber());
    invoice.setPaymentMode(dto.getPaymentMode());
    invoice.setTerms(dto.getTerms());
    invoice.setNarration(dto.getNarration());
    invoice.setRoundOff(dto.getRoundOff());
    invoice.setIsRcm(dto.getIsRcm());
    invoice.setCommissionerate(dto.getCommissionerate());
    invoice.setDivision(dto.getDivision());
    invoice.setRange(dto.getRange());
    invoice.setLrNumber(dto.getLrNumber());
    invoice.setTransport(dto.getTransport());
    invoice.setUpdatedAt(LocalDateTime.now());

    // ================= CLEAR OLD ITEMS =================
    invoice.getItems().clear();

    double totalTaxable = 0;
    double totalCgst = 0;
    double totalSgst = 0;
    double totalIgst = 0;

    // ================= ADD NEW ITEMS =================
    for (InvoiceItemDTO itemDto : dto.getItems()) {

        InvoiceItem item = new InvoiceItem();

        Product product = productRepository.findById(itemDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        item.setProduct(product);
        item.setQuantity(itemDto.getQuantity());
        item.setRate(itemDto.getRate());

        double baseAmount = itemDto.getQuantity() * itemDto.getRate();
        item.setBaseAmount(baseAmount);

        // ===== Royalty Calculation =====
        item.setRoyalty(itemDto.getRoyalty());
        double royaltyAmount = baseAmount * itemDto.getRoyalty() / 100;
        item.setRoyaltyAmount(royaltyAmount);

        // ===== DMF Calculation =====
        item.setDmf(itemDto.getDmf());
        double dmfAmount = baseAmount * itemDto.getDmf() / 100;
        item.setDmfAmount(dmfAmount);

        // ===== NMET Calculation =====
        item.setNmet(itemDto.getNmet());
        double nmetAmount = baseAmount * itemDto.getNmet() / 100;
        item.setNmetAmount(nmetAmount);

        double taxableAmount = baseAmount;
        item.setTaxableAmount(taxableAmount);

        // ===== GST =====
        item.setCgstPercent(itemDto.getCgstPercent());
        item.setSgstPercent(itemDto.getSgstPercent());
        item.setIgstPercent(itemDto.getIgstPercent());

        double cgstAmount = taxableAmount * itemDto.getCgstPercent() / 100;
        double sgstAmount = taxableAmount * itemDto.getSgstPercent() / 100;
        double igstAmount = taxableAmount * itemDto.getIgstPercent() / 100;

        item.setCgstAmount(cgstAmount);
        item.setSgstAmount(sgstAmount);
        item.setIgstAmount(igstAmount);

        // ===== Add to totals =====
        totalTaxable += taxableAmount;
        totalCgst += cgstAmount;
        totalSgst += sgstAmount;
        totalIgst += igstAmount;

        // 🔥 VERY IMPORTANT
        item.setInvoice(invoice);

        invoice.getItems().add(item);
    }

    // ================= TOTAL CALCULATION =================
    invoice.setTotalTaxableAmount(totalTaxable);
    invoice.setTotalCgst(totalCgst);
    invoice.setTotalSgst(totalSgst);
    invoice.setTotalIgst(totalIgst);

    double grandTotal = totalTaxable + totalCgst + totalSgst + totalIgst + invoice.getRoundOff();
    invoice.setGrandTotal(grandTotal);

    // ================= SAVE =================
    invoiceRepository.save(invoice);

    return new MessageResponse(true, HttpStatus.OK, "Invoice updated successfully");
}


    @Override
    public DataResponse getInvoiceById(int invoiceId, Map<String, String> headers) {
        if (invoiceRepository.findById(invoiceId).isPresent())
            return new DataResponse(true, HttpStatus.OK, appUtils.invoiceToDto(invoiceRepository.findById(invoiceId).get()));
        else
            throw new ResourceNotFoundException("Invoice-Details with id '" + invoiceId + "' not Found!");
    }

    @Override
    public MessageResponse softDeleteInvoiceById(int invoiceId, Map<String, String> headers) {

        // Fetch user from DB
        InvoiceGenerator invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice with ID '" + invoiceId + "' not found!"));


        invoice.setIsDeleted(true);
        invoice.setDeletedAt(LocalDateTime.now());
        invoiceRepository.save(invoice);

        return new MessageResponse(true, HttpStatus.OK, "Invoice with ID '" + invoiceId + "' soft deleted.");
    }

    @Override
    public MessageResponse restoreInvoiceById(int invoiceId, Map<String, String> headers) {
        // Fetch user from DB
        InvoiceGenerator invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice with ID '" + invoiceId + "' not found!"));


        invoice.setIsDeleted(false);
        invoice.setDeletedAt(null);
        invoiceRepository.save(invoice);

        return new MessageResponse(true, HttpStatus.OK, "Invoice with ID '" + invoiceId + "' restored.");
    }

//    @Override
//    public HttpResponse getAllInvoiceReports(
//            String keyword,
//            Integer pageNumber,
//            Integer pageSize,
//            String sortBy,
//            String sortDirection,
//            Map<String, String> headers) {
//
//        Page<InvoiceGenerator> pageInvoice = null;
//        List<InvoiceReportDto> dtos = null;
//
//        try {
//
//            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//            CriteriaQuery<InvoiceGenerator> cq = cb.createQuery(InvoiceGenerator.class);
//            Root<InvoiceGenerator> root = cq.from(InvoiceGenerator.class);
//
//            Order order = sortDirection.equalsIgnoreCase("asc") ?
//                    cb.asc(root.get(sortBy)) :
//                    cb.desc(root.get(sortBy));
//
//            cq.orderBy(order);
//
//            Predicate predicate = cb.conjunction();
//
//            if (keyword != null && !keyword.isEmpty()) {
//                predicate = cb.or(
//                        cb.like(cb.lower(root.get("invoiceNumber")), "%" + keyword.toLowerCase() + "%"),
//                        cb.like(cb.lower(root.get("invoicePrefix")), "%" + keyword.toLowerCase() + "%")
//                );
//            }
//
//            cq.select(root).where(predicate);
//
//            Pageable pageable = PageRequest.of(pageNumber, pageSize);
//
//            List<InvoiceGenerator> resultList = entityManager.createQuery(cq)
//                    .setFirstResult((int) pageable.getOffset())
//                    .setMaxResults(pageable.getPageSize())
//                    .getResultList();
//
//            long totalCount = entityManager.createQuery(cq).getResultList().size();
//
//            pageInvoice = new PageImpl<>(resultList, pageable, totalCount);
//
//            // Convert to DTO
//            dtos = resultList.stream().flatMap(invoice ->
//                    invoice.getItems().stream().map(item ->
//                            InvoiceReportDto.builder()
//                                    .date(invoice.getInvoiceDate())
//                                    .particulars(invoice.getCustomer().getDisplayName())
//                                    .invoiceType(null)
//                                    .invoiceNumber(invoice.getInvoicePrefix() + invoice.getInvoiceNumber())
//                                    .quantity(item.getQuantity())
//                                    .material(item.getProduct().getProductName())
//
//                                    .value(formatAmount(item.getBaseAmount()))
//                                    .royaltyValue(formatAmount(item.getRoyaltyAmount()))
//                                    .dmf(formatAmount(item.getDmfAmount()))
//                                    .nmet(formatAmount(item.getNmetAmount()))
//                                    .totalTaxableValue(formatAmount(item.getTaxableAmount()))
//
//                                    .sgst(formatAmount(item.getSgstAmount()))
//                                    .cgst(formatAmount(item.getCgstAmount()))
//                                    .igst(formatAmount(item.getIgstAmount()))
//
//                                    .total(formatAmount(item.getTaxableAmount()))
//                                    .roundOff(formatAmount(invoice.getRoundOff()))
//                                    .grandTotal(formatAmount(invoice.getGrandTotal()))
//
//                                    .paymentReceived(formatAmount(0))
//                                    .closingBalance(formatAmount(invoice.getGrandTotal()))
//
//                                    .narration(invoice.getNarration())
//                                    .build()
//                    )
//            ).toList();
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return HttpResponse.builder()
//                .success(true)
//                .successCode(HttpStatus.OK)
//                .pageNumber(pageInvoice.getNumber())
//                .pageSize(pageInvoice.getSize())
//                .totalElements(pageInvoice.getTotalElements())
//                .totalPages(pageInvoice.getTotalPages())
//                .isLastPage(pageInvoice.isLast())
//                .data(dtos)
//                .build();
//    }
@Override
public HttpResponse getAllInvoiceReports(
        String keyword,
        Integer pageNumber,
        Integer pageSize,
        String sortBy,
        String sortDirection,
        Map<String, String> headers) {

    Page<InvoiceGenerator> pageInvoice = null;
    List<InvoiceReportDto> dtos = new ArrayList<>();

    try {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<InvoiceGenerator> cq = cb.createQuery(InvoiceGenerator.class);
        Root<InvoiceGenerator> root = cq.from(InvoiceGenerator.class);

        Order order = sortDirection.equalsIgnoreCase("asc") ?
                cb.asc(root.get(sortBy)) :
                cb.desc(root.get(sortBy));

        cq.orderBy(order);

        Predicate predicate = cb.conjunction();

        if (keyword != null && !keyword.isEmpty()) {
            predicate = cb.or(
                    cb.like(cb.lower(root.get("invoiceNumber")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("invoicePrefix")), "%" + keyword.toLowerCase() + "%")
            );
        }

        cq.select(root).where(predicate);

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<InvoiceGenerator> resultList = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        long totalCount = entityManager.createQuery(cq).getResultList().size();

        pageInvoice = new PageImpl<>(resultList, pageable, totalCount);

        // 🔢 TOTAL VARIABLES
        double totalTaxableSum = 0;
        double totalSum = 0;
        double grandTotalSum = 0;
        double paymentReceivedSum = 0;
        double closingBalanceSum = 0;

        double valueSum = 0;
        double royaltyValueSum = 0;
        double dmfSum = 0;
        double nmetSum = 0;
        double sgstSum = 0;
        double cgstSum = 0;
        double igstSum = 0;
        double roundOffSum = 0;

// Convert to DTO
        for (InvoiceGenerator invoice : resultList) {
            for (var item : invoice.getItems()) {

                double taxable = item.getTaxableAmount();
                double grand = invoice.getGrandTotal();
                double payment = invoice.getPaymentReceived() != null ? invoice.getPaymentReceived() : 0;
                double closing = invoice.getClosingBalance() != null ? invoice.getClosingBalance() : grand - payment;

                // extra fields
                double value = item.getBaseAmount();
                double royalty = item.getRoyaltyAmount();
                double dmf = item.getDmfAmount();
                double nmet = item.getNmetAmount();
                double sgst = item.getSgstAmount();
                double cgst = item.getCgstAmount();
                double igst = item.getIgstAmount();
                double roundOff = invoice.getRoundOff() != null ? invoice.getRoundOff() : 0;

                // accumulate totals
                totalTaxableSum += taxable;
                totalSum += taxable;
                grandTotalSum += grand;
                paymentReceivedSum += payment;
                closingBalanceSum += closing;

                valueSum += value;
                royaltyValueSum += royalty;
                dmfSum += dmf;
                nmetSum += nmet;
                sgstSum += sgst;
                cgstSum += cgst;
                igstSum += igst;
                roundOffSum += roundOff;

                dtos.add(
                        InvoiceReportDto.builder()
                                .date(invoice.getInvoiceDate())
                                .particulars(invoice.getCustomer().getDisplayName())
                                .invoiceType(null)
                                .invoiceNumber(invoice.getInvoicePrefix() + invoice.getInvoiceNumber())
                                .quantity(item.getQuantity())
                                .material(item.getProduct().getProductName())
                                .value(formatAmount(value))
                                .royaltyValue(formatAmount(royalty))
                                .dmf(formatAmount(dmf))
                                .nmet(formatAmount(nmet))
                                .totalTaxableValue(formatAmount(taxable))
                                .sgst(formatAmount(sgst))
                                .cgst(formatAmount(cgst))
                                .igst(formatAmount(igst))
                                .total(formatAmount(taxable))
                                .roundOff(formatAmount(roundOff))
                                .grandTotal(formatAmount(grand))
                                .paymentReceived(formatAmount(payment))
                                .closingBalance(formatAmount(closing))
                                .narration(invoice.getNarration())
                                .build()
                );
            }
        }

// ✅ LAST TOTAL ROW WITH ALL FIELDS
        dtos.add(
                InvoiceReportDto.builder()
                        .date("Total")
                        .value(formatAmount(valueSum))
                        .royaltyValue(formatAmount(royaltyValueSum))
                        .dmf(formatAmount(dmfSum))
                        .nmet(formatAmount(nmetSum))
                        .totalTaxableValue(formatAmount(totalTaxableSum))
                        .sgst(formatAmount(sgstSum))
                        .cgst(formatAmount(cgstSum))
                        .igst(formatAmount(igstSum))
                        .total(formatAmount(totalSum))
                        .roundOff(formatAmount(roundOffSum))
                        .grandTotal(formatAmount(grandTotalSum))
                        .paymentReceived(formatAmount(paymentReceivedSum))
                        .closingBalance(formatAmount(closingBalanceSum))
                        .particulars("")
                        .invoiceType("")
                        .invoiceNumber("")
                        .material("")
                        .quantity(null)
                        .narration("")
                        .build()
        );
    } catch (Exception e) {
        e.printStackTrace();
    }

    return HttpResponse.builder()
            .success(true)
            .successCode(HttpStatus.OK)
            .pageNumber(pageInvoice.getNumber())
            .pageSize(pageInvoice.getSize())
            .totalElements(pageInvoice.getTotalElements())
            .totalPages(pageInvoice.getTotalPages())
            .isLastPage(pageInvoice.isLast())
            .data(dtos)
            .build();
}

    private String formatAmount(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(amount);
    }
    @Override
    public void saveExcelData(MultipartFile file) {

        List<InvoiceGenerator> invoices = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                InvoiceGenerator invoice = new InvoiceGenerator();

                // --- Date ---
                invoice.setInvoiceDate(getDateValue(row.getCell(0)).toString());

                // --- Company ID ---
                Integer companyId = getDoubleValue(row.getCell(1)).intValue();
                if (companyId != null) {
                    CompanyRegistration company = companyRepository.findById(companyId)
                            .orElseThrow(() -> new RuntimeException("Invalid Company ID: " + companyId));
                    invoice.setCompanyRegistration(company);
                }

                // --- Invoice Number ---
                invoice.setInvoiceNumber(getStringValue(row.getCell(2)));
                invoice.setInvoicePrefix(getStringValue(row.getCell(2)).replaceAll("\\d", ""));

                // --- Customer lookup ---
                String customerName = getStringValue(row.getCell(3));
                Customer customer = customerRepository.findByDisplayName(customerName)
                        .orElseThrow(() -> new RuntimeException("Customer not found: " + customerName));
                invoice.setCustomer(customer);

                // --- Opposite Account / Company Name can be stored if needed ---
                // invoice.setOppositeAccount(getStringValue(row.getCell(4)));

                // --- Invoice Item ---
                InvoiceItem item = new InvoiceItem();
                String productName = getStringValue(row.getCell(5));
                Product product = productRepository.findByProductName(productName)
                        .orElseThrow(() -> new RuntimeException("Product not found: " + productName));
                item.setProduct(product);

                item.setQuantity(getDoubleValue(row.getCell(7)));
                item.setRate(getDoubleValue(row.getCell(8)));
                item.setBaseAmount(getDoubleValue(row.getCell(9)));
                item.setRoyaltyAmount(getDoubleValue(row.getCell(10)));
                item.setDmfAmount(getDoubleValue(row.getCell(11)));
                item.setNmetAmount(getDoubleValue(row.getCell(12)));
                item.setTaxableAmount(getDoubleValue(row.getCell(13)));
                item.setIgstAmount(getDoubleValue(row.getCell(14)));
                item.setCgstAmount(getDoubleValue(row.getCell(15)));
                item.setSgstAmount(getDoubleValue(row.getCell(16)));

                // --- RoundOff / Adjustment ---
                invoice.setRoundOff(getDoubleValue(row.getCell(17)));

                // --- Grand Total / Payment / Closing ---
                invoice.setGrandTotal(getDoubleValue(row.getCell(18)));
                invoice.setPaymentReceived(getDoubleValue(row.getCell(19)));
                invoice.setClosingBalance(getDoubleValue(row.getCell(20)));

                // --- Link item to invoice ---
                item.setInvoice(invoice);
                invoice.setItems(List.of(item));

                invoices.add(invoice);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading Excel file", e);
        }

        invoiceRepository.saveAll(invoices);
    }

    private String getStringValue(Cell cell) {

        if (cell == null) return null;

        switch (cell.getCellType()) {

            case STRING:
                return cell.getStringCellValue();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("yyyy-MM-dd")
                            .format(cell.getDateCellValue());
                }
                return String.valueOf(cell.getNumericCellValue());

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            default:
                return null;
        }
    }



    private Double getDoubleValue(Cell cell) {

        if (cell == null) return 0.0;

        try {

            if (cell.getCellType() == CellType.NUMERIC)
                return cell.getNumericCellValue();

            if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue();

                if (value == null || value.trim().isEmpty())
                    return 0.0;

                value = value.replace(",", "").trim();  // remove commas
                return Double.parseDouble(value);
            }

        } catch (Exception e) {
            return 0.0;
        }

        return 0.0;
    }

    private LocalDate getDateValue(Cell cell) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC &&
                DateUtil.isCellDateFormatted(cell)) {

            return cell.getDateCellValue()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        if (cell.getCellType() == CellType.STRING) {
            return LocalDate.parse(cell.getStringCellValue());
        }

        return null;
    }

    @Override
    public HttpResponse getAllSoftDeletedInvoices(
            Integer companyId,
            String keyword,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortDirection,
            Map<String, String> headers) {

        Page<InvoiceGenerator> pageInvoice = null;
        List<InvoiceDto> dtos = new ArrayList<>();
        Integer intKeyword = null;

        try {
            if (keyword != null) intKeyword = Integer.parseInt(keyword);
        } catch (Exception ignored) {}

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<InvoiceGenerator> cq = cb.createQuery(InvoiceGenerator.class);
            Root<InvoiceGenerator> root = cq.from(InvoiceGenerator.class);

            List<Predicate> predicates = new ArrayList<>();

            // ----------------------------
            // Company Filter (mandatory)
            // ----------------------------
            predicates.add(cb.equal(root.get("companyRegistration").get("companyId"), companyId));

            // ----------------------------
            // Soft Delete Filter
            // ----------------------------
            predicates.add(cb.equal(root.get("isDeleted"), true));

            // ----------------------------
            // Keyword Filter
            // ----------------------------
            if (keyword != null && !keyword.isEmpty()) {
                if (intKeyword != null) {
                    predicates.add(cb.equal(root.get("invoiceId"), intKeyword));
                } else {
                    predicates.add(cb.like(cb.lower(root.get("invoiceNumber")), "%" + keyword.toLowerCase() + "%"));
                }
            }

            // ----------------------------
            // Sorting
            // ----------------------------
            Order order = "asc".equalsIgnoreCase(sortDirection)
                    ? cb.asc(root.get(sortBy))
                    : cb.desc(root.get(sortBy));
            cq.orderBy(order);

            // ----------------------------
            // Apply Predicates
            // ----------------------------
            cq.where(cb.and(predicates.toArray(new Predicate[0])));

            Pageable pageable = PageRequest.of(pageNumber, pageSize);

            List<InvoiceGenerator> resultList = entityManager.createQuery(cq)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();

            long totalCount = entityManager.createQuery(cq).getResultList().size();

            pageInvoice = new PageImpl<>(resultList, pageable, totalCount);

            dtos = appUtils.invoicesToDtos(resultList);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .pageNumber(pageInvoice.getNumber())
                .pageSize(pageInvoice.getSize())
                .totalElements(pageInvoice.getTotalElements())
                .totalPages(pageInvoice.getTotalPages())
                .isLastPage(pageInvoice.isLast())
                .data(dtos)
                .build();
    }
}
