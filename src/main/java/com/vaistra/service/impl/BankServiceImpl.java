package com.vaistra.service.impl;

import com.vaistra.dto.BankDetailsDto;
import com.vaistra.dto.response.DataResponse;
import com.vaistra.dto.response.HttpResponse;
import com.vaistra.dto.response.MessageResponse;
import com.vaistra.dto.update.BankUpdateDto;
import com.vaistra.entity.BankDetails;
import com.vaistra.entity.CompanyRegistration;
import com.vaistra.exception.DuplicateEntryException;
import com.vaistra.exception.ResourceNotFoundException;
import com.vaistra.repository.BankRepository;
import com.vaistra.repository.CompanyRepository;
import com.vaistra.service.BankService;
import com.vaistra.util.AppUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class BankServiceImpl implements BankService {

    @PersistenceContext
    private EntityManager entityManager;
    private final BankRepository bankRepository;
    private final CompanyRepository companyRepository;
    private final AppUtils appUtils;
    private final HttpServletRequest request;

    @Autowired
    private RestTemplate restTemplate;

    public BankServiceImpl(BankRepository bankRepository, CompanyRepository companyRepository, AppUtils appUtils, HttpServletRequest request) {
        this.bankRepository = bankRepository;
        this.companyRepository = companyRepository;
        this.appUtils = appUtils;
        this.request = request;
    }


    @Override
    public MessageResponse addBankDetails(BankDetailsDto b, Map<String, String> headers) {

        if (bankRepository.existsByAccountNumberIgnoreCase(b.getAccountNumber()))
            throw new DuplicateEntryException("Bank with AccountNumber '" + b.getAccountNumber() + "' already exists!");

        if (b.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID is required.");
        }

        CompanyRegistration company = companyRepository.findById(b.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + b.getCompanyId()));

        // Populate BankDetails entity
        BankDetails bank = new BankDetails();
        bank.setBankName(b.getBankName().trim());
        bank.setIfscCode(b.getIfscCode());
        bank.setBranch(b.getBranch());
        bank.setAccountNumber(b.getAccountNumber());
        bank.setAccHolderName(b.getAccHolderName());
        bank.setBankAddress(b.getBankAddress());
        bank.setCompanyRegistration(company); // Set the company entity

        bank.setStatus(b.getStatus() != null ? b.getStatus() : true);
        bank.setIsDeleted(false);
        bank.setCreatedAt(LocalDateTime.now());
        bank.setUpdatedAt(LocalDateTime.now());
        bank.setDeletedAt(null);

        bankRepository.save(bank);

        return new MessageResponse(true, HttpStatus.OK, "Bank-Details Entered successfully !!.");
    }

    @Override
    public HttpResponse getAllBankDetails(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, String isDeleted, String status, Map<String, String> headers) {
        Page<BankDetails> pageBank = null;
        List<BankDetailsDto> dtos = null;

        Integer intKeyword = null;
        Boolean softDeleted = null;
        Boolean isActoveStatus = null;


        if (isDeleted.equalsIgnoreCase("true")) {
            softDeleted = Boolean.TRUE;
        } else if (isDeleted.equalsIgnoreCase("false")) {
            softDeleted = Boolean.FALSE;
        }

        if (status.equalsIgnoreCase("true")) {
            isActoveStatus = Boolean.TRUE;
        } else if (status.equalsIgnoreCase("false")) {
            isActoveStatus = Boolean.FALSE;
        }

        try {
            intKeyword = Integer.parseInt(keyword);
        } catch (Exception ignored) {
        }

        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<BankDetails> criteriaQuery = criteriaBuilder.createQuery(BankDetails.class);
            Root<BankDetails> root = criteriaQuery.from(BankDetails.class);

            Order order = sortDirection.equalsIgnoreCase("asc") ?
                    criteriaBuilder.asc(root.get(sortBy)) :
                    criteriaBuilder.desc(root.get(sortBy));
            criteriaQuery.orderBy(order);

            Predicate statusPredicate = criteriaBuilder.equal(root.get("status"), isActoveStatus);
            Predicate deletedPredicate = criteriaBuilder.equal(root.get("isDeleted"), softDeleted);
            Predicate userIdPredicate = criteriaBuilder.equal(root.get("bankId"), intKeyword);
            Predicate userPredicate = null;

            if (keyword != null) {
                userPredicate = criteriaBuilder.or(

                        criteriaBuilder.like(criteriaBuilder.lower(root.get("ifscCode").as(String.class)), "%" + keyword.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("accountNumber").as(String.class)), "%" + keyword.toLowerCase() + "%")

                );
            }

            Predicate combinedPredicate = null;

            if (isActoveStatus != null) {
                combinedPredicate = statusPredicate;

                if (softDeleted != null) {
                    if (intKeyword != null) {
                        combinedPredicate = criteriaBuilder.and(combinedPredicate, userIdPredicate, deletedPredicate);
                    } else if (keyword != null) {
                        combinedPredicate = criteriaBuilder.and(combinedPredicate, userPredicate, deletedPredicate);
                    } else {
                        combinedPredicate = criteriaBuilder.and(combinedPredicate, deletedPredicate);
                    }
                }
            }

            // Create the query to retrieve a page of results
            criteriaQuery.select(root)
                    .where(criteriaBuilder.and(combinedPredicate));

            Pageable pageable = PageRequest.of(pageNumber, pageSize);

            // Fetch results for the current page
            List<BankDetails> resultList = entityManager.createQuery(criteriaQuery)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();

            long totalCount = entityManager.createQuery(criteriaQuery).getResultList().size();

            pageBank = new PageImpl<>(resultList, pageable, totalCount);

            dtos = appUtils.banksToDtos(resultList);

        } catch (NoResultException ignored) {

        }
        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .pageNumber(pageBank.getNumber())
                .pageSize(pageBank.getSize())
                .totalElements(pageBank.getTotalElements())
                .totalPages(pageBank.getTotalPages())
                .isLastPage(pageBank.isLast())
                .data(dtos)
                .build();
    }

    @Override
    public DataResponse getBankDetailsById(int bankId, Map<String, String> headers) {

        if (bankRepository.findById(bankId).isPresent())
            return new DataResponse(true, HttpStatus.OK, appUtils.bankToDto(bankRepository.findById(bankId).get()));
        else
            throw new ResourceNotFoundException("Bank-Details with id '" + bankId + "' not Found!");
    }

    @Override
    public MessageResponse updateBankDetails(BankUpdateDto bankDto, int bankId, Map<String, String> headers) {

        BankDetails bank = bankRepository.findById(bankId)
                .orElseThrow(() -> new RuntimeException("Bank-Details not found"));

        if (bankDto.getBankName() != null)
            bank.setBankName(bankDto.getBankName());

        if (bankDto.getAccountNumber() != null)
            bank.setAccountNumber(bankDto.getAccountNumber());

        if (bankDto.getIfscCode() != null)
            bank.setIfscCode(bankDto.getIfscCode());

        if (bankDto.getBranch() != null)
            bank.setBranch(bankDto.getBranch());

        if (bankDto.getAccHolderName()!=null)
            bank.setAccHolderName(bankDto.getAccHolderName());

        if (bankDto.getBankAddress()!=null)
            bank.setBankAddress(bankDto.getBankAddress());

        if (bankDto.getStatus() != null)
            bank.setStatus(bankDto.getStatus());

        if (bankDto.getCompanyId() != null) {
            CompanyRegistration company = companyRepository.findById(bankDto.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company with ID '" + bankDto.getCompanyId() + "' not found!"));

            bank.setCompanyRegistration(company);
        }

        bank.setUpdatedAt(LocalDateTime.now());

        bankRepository.save(bank);
        return new MessageResponse(true, HttpStatus.OK, "Bank-Details Updated successfully !!.");
    }

    @Override
    public MessageResponse softDeleteBankDetailsById(int bankId, Map<String, String> headers) {

        // Fetch user from DB
        BankDetails bank = bankRepository.findById(bankId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank with ID '" + bankId + "' not found!"));


        bank.setIsDeleted(true);
        bank.setDeletedAt(LocalDateTime.now());
        bankRepository.save(bank);

        return new MessageResponse(true, HttpStatus.OK, "Bank-Details with ID '" + bankId + "' soft deleted.");
    }

    @Override
    public MessageResponse restoreBankDetailsById(int bankId, Map<String, String> headers) {

        // Fetch user from DB
        BankDetails bank = bankRepository.findById(bankId).
                orElseThrow(() -> new ResourceNotFoundException("Bank-Details with id '" + bankId + "' not found!"));


        bank.setIsDeleted(false);
        bank.setDeletedAt(null);
        bankRepository.save(bank);

        return new MessageResponse(true, HttpStatus.OK, "Bank-Details with ID '" + bankId + "' restored.");
    }

}
