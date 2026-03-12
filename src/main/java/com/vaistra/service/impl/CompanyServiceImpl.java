package com.vaistra.service.impl;

import com.vaistra.dto.CompanyDto;
import com.vaistra.dto.response.DataResponse;
import com.vaistra.dto.response.HttpResponse;
import com.vaistra.dto.response.MessageResponse;
import com.vaistra.entity.BankDetails;
import com.vaistra.entity.City;
import com.vaistra.entity.CompanyRegistration;
import com.vaistra.entity.State;
import com.vaistra.exception.DuplicateEntryException;
import com.vaistra.exception.ResourceNotFoundException;
import com.vaistra.exception.UserUnauthorizedException;
import com.vaistra.repository.BankRepository;
import com.vaistra.repository.CityRepository;
import com.vaistra.repository.CompanyRepository;
import com.vaistra.repository.StateRepository;
import com.vaistra.service.CloudinaryImageService;
import com.vaistra.service.CompanyService;
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
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class CompanyServiceImpl implements CompanyService {

    @PersistenceContext
    private EntityManager entityManager;
    private final CompanyRepository companyRepository;
    private final BankRepository bankRepository;
    private final AppUtils appUtils;
    private final HttpServletRequest request;
    private final CityRepository cityRepository;
    private final StateRepository stateRepository;

//    private final AmazonS3 s3Client;
//    private final AwsService awsService;

    @Autowired
    private RestTemplate restTemplate;

    public CompanyServiceImpl(CompanyRepository companyRepository, BankRepository bankRepository, AppUtils appUtils, HttpServletRequest request, CityRepository cityRepository, StateRepository stateRepository) {
        this.companyRepository = companyRepository;
        this.bankRepository = bankRepository;
        this.appUtils = appUtils;
        this.request = request;

        this.cityRepository = cityRepository;
        this.stateRepository = stateRepository;
    }

    @Autowired
    private CloudinaryImageService cloudinaryImageService;


    @Override
    public MessageResponse addCompany(
            String companyName,
            String ownerName,
            String password,
            MultipartFile logoFile,

            String email,
            String cinNumber,
            String lrNumber,
            String transport,
            String commissionerate,
            String range,
            String division,
            String mobileNumber,
            String alternateMobileNumber,

            String billingAddress1,
            String billingAddress2,
            String billingAddress3,
            Integer billingStateId,
            Integer billingCityId,
            String billingPincode,

            String panNumber,
            String gstNumber,
            String serviceDescription,
            String industry,
            Boolean status,
            Boolean isDeleted,

            List<String> bankName,
            List<String> ifscCode,
            List<String> branch,
            List<String> accountNumber,
            List<String> accHolderName,
            List<String> bankAddress,
            List<Boolean> bankStatus,
            List<Boolean> bankIsDeleted,

            Map<String, String> headers
    ) {

        if (companyRepository.existsByCompanyNameIgnoreCase(companyName)) {
            throw new DuplicateEntryException(
                    "Company with name '" + companyName + "' already exists!"
            );
        }

        CompanyRegistration company = new CompanyRegistration();
        company.setCompanyName(companyName.trim());
        company.setOwnerName(ownerName);
        company.setPassword(password);

        company.setEmail(email);
        company.setMobileNumber(mobileNumber);
        company.setAlternateMobileNumber(alternateMobileNumber);
        company.setCinNumber(cinNumber);

        company.setBillingAddress1(billingAddress1);
        company.setBillingAddress2(billingAddress2);
        company.setBillingAddress3(billingAddress3);
        company.setBillingPincode(billingPincode);

        State billingState = stateRepository.findById(billingStateId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing State not found"));
        City billingCity = cityRepository.findById(billingCityId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing City not found"));

        company.setBillingState(billingState);
        company.setBillingCity(billingCity);

        company.setPanNumber(panNumber);
        company.setGstNumber(gstNumber);
        company.setIndustry(industry);

        company.setStatus(status != null ? status : true);
        company.setIsDeleted(isDeleted != null ? isDeleted : false);
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());

        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                Map<String, Object> uploadData =
                        cloudinaryImageService.upload(logoFile, headers);
                company.setLogo((String) uploadData.get("url"));
            } catch (Exception e) {
                throw new RuntimeException("Logo upload failed", e);
            }
        }

        companyRepository.save(company);

        if (bankName != null) {
            for (int i = 0; i < bankName.size(); i++) {
                BankDetails bank = new BankDetails();
                bank.setBankName(bankName.get(i));
                bank.setIfscCode(ifscCode != null && ifscCode.size() > i ? ifscCode.get(i) : null);
                bank.setBranch(branch != null && branch.size() > i ? branch.get(i) : null);
                bank.setAccountNumber(accountNumber != null && accountNumber.size() > i ? accountNumber.get(i) : null);
                bank.setAccHolderName(accHolderName != null && accHolderName.size() > i ? accHolderName.get(i) : null);
                bank.setBankAddress(bankAddress != null && bankAddress.size() > i ? bankAddress.get(i) : null);
                bank.setStatus(bankStatus != null && bankStatus.size() > i ? bankStatus.get(i) : true);
                bank.setIsDeleted(bankIsDeleted != null && bankIsDeleted.size() > i ? bankIsDeleted.get(i) : false);
                bank.setCreatedAt(LocalDateTime.now());
                bank.setUpdatedAt(LocalDateTime.now());
                bank.setCompanyRegistration(company);

                bankRepository.save(bank);
            }
        }

        return new MessageResponse(true, HttpStatus.OK,
                "Company Registered successfully !!");
    }


    @Override
    public HttpResponse getAllCompanies(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, String isDeleted, String status, Map<String, String> headers) {
        Page<CompanyRegistration> pageCompany = null;
        List<CompanyDto> dtos = null;

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
            CriteriaQuery<CompanyRegistration> criteriaQuery = criteriaBuilder.createQuery(CompanyRegistration.class);
            Root<CompanyRegistration> root = criteriaQuery.from(CompanyRegistration.class);

            Order order = sortDirection.equalsIgnoreCase("asc") ?
                    criteriaBuilder.asc(root.get(sortBy)) :
                    criteriaBuilder.desc(root.get(sortBy));
            criteriaQuery.orderBy(order);

            Predicate statusPredicate = criteriaBuilder.equal(root.get("status"), isActoveStatus);
            Predicate deletedPredicate = criteriaBuilder.equal(root.get("isDeleted"), softDeleted);
            Predicate userIdPredicate = criteriaBuilder.equal(root.get("companyId"), intKeyword);
            Predicate userPredicate = null;

//            if (keyword != null) {
//                userPredicate = criteriaBuilder.or(
//
//                        criteriaBuilder.like(criteriaBuilder.lower(root.get("companyName").as(String.class)), "%" + keyword.toLowerCase() + "%"),
//                        criteriaBuilder.like(criteriaBuilder.lower(root.get("ownerName").as(String.class)), "%" + keyword.toLowerCase() + "%")
//
//                );
//            }
            if (keyword != null) {
                userPredicate = criteriaBuilder.or(

                        criteriaBuilder.like(criteriaBuilder.lower(root.get("companyName")), "%" + keyword.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("ownerName")), "%" + keyword.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + keyword.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("mobileNumber")), "%" + keyword.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("alternateMobileNumber")), "%" + keyword.toLowerCase() + "%")

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
            List<CompanyRegistration> resultList = entityManager.createQuery(criteriaQuery)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();

            long totalCount = entityManager.createQuery(criteriaQuery).getResultList().size();

            pageCompany = new PageImpl<>(resultList, pageable, totalCount);

            dtos = appUtils.companiesToDtos(resultList);

        } catch (NoResultException ignored) {

        }
        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .pageNumber(pageCompany.getNumber())
                .pageSize(pageCompany.getSize())
                .totalElements(pageCompany.getTotalElements())
                .totalPages(pageCompany.getTotalPages())
                .isLastPage(pageCompany.isLast())
                .data(dtos)
                .build();
    }

    @Override
    public DataResponse getCompanyById(int companyId, Map<String, String> headers) {
        if (companyRepository.findById(companyId).isPresent())
            return new DataResponse(true, HttpStatus.OK, appUtils.companyToDto(companyRepository.findById(companyId).get()));
        else
            throw new ResourceNotFoundException("Company with id '" + companyId + "' not Found!");
    }

    @Override
    public MessageResponse updateCompany(
            String companyName,
            String ownerName,
            String password,
            MultipartFile logoFile,

            String email,
            String cinNumber,
            String lrNumber,
            String transport,
            String commissionerate,
            String range,
            String division,

            String mobileNumber,
            String alternateMobileNumber,

            String billingAddress1,
            String billingAddress2,
            String billingAddress3,
            Integer billingStateId,
            Integer billingCityId,
            String billingPincode,

            String panNumber,
            String gstNumber,
            String serviceDescription,
            String industry,
            Boolean status,
            Boolean isDeleted,

            List<String> bankName,
            List<String> ifscCode,
            List<String> branch,
            List<String> accountNumber,
            List<String> accHolderName,
            List<String> bankAddress,
            List<Boolean> bankStatus,
            List<Boolean> bankIsDeleted,

            int companyId,
            Map<String, String> headers
    ) {

        CompanyRegistration company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // Basic fields
        company.setCompanyName(companyName);
        company.setOwnerName(ownerName);
        company.setPassword(password);
        company.setEmail(email);
        company.setMobileNumber(mobileNumber);
        company.setAlternateMobileNumber(alternateMobileNumber);
        company.setCinNumber(cinNumber);

        // Billing
        company.setBillingAddress1(billingAddress1);
        company.setBillingAddress2(billingAddress2);
        company.setBillingAddress3(billingAddress3);
        company.setBillingPincode(billingPincode);

        State billingState = stateRepository.findById(billingStateId)
                .orElseThrow(() -> new RuntimeException("Billing state not found"));
        company.setBillingState(billingState);

//        City billingCity = cityRepository.findById(billingCityId)
//                .orElseThrow(() -> new RuntimeException("Billing city not found"));
//        company.setBillingCity(billingCity);
        if (billingStateId != null) {
            State billingState1 = stateRepository.findById(billingStateId)
                    .orElseThrow(() -> new RuntimeException("Billing state not found"));
            company.setBillingState(billingState1);
        }


        company.setPanNumber(panNumber);
        company.setGstNumber(gstNumber);
        company.setIndustry(industry);
        if (status != null) {
            company.setStatus(status);
        }

        if (isDeleted != null) {
            company.setIsDeleted(isDeleted);
        }


        // Logo update
        if (logoFile != null && !logoFile.isEmpty()) {
            Map<String, Object> uploadData = cloudinaryImageService.upload(logoFile, headers);
            company.setLogo((String) uploadData.get("url"));
        }

        // Bank Update (Simple Replace Logic)
//        if (bankName != null && !bankName.isEmpty()) {
//
//            List<BankDetails> existingBanks = bankRepository.findByCompanyRegistration(company);
//            existingBanks.forEach(bank -> bank.setIsDeleted(true));
//
//            for (int i = 0; i < bankName.size(); i++) {
//
//                BankDetails bank = new BankDetails();
//                bank.setBankName(bankName.get(i));
//                bank.setIfscCode(ifscCode.get(i));
//                bank.setBranch(branch.get(i));
//                bank.setAccountNumber(accountNumber.get(i));
//                bank.setAccHolderName(accHolderName.get(i));
//                bank.setBankAddress(bankAddress.get(i));
//                bank.setStatus(bankStatus.get(i));
//                bank.setIsDeleted(false);
//                bank.setCreatedAt(LocalDateTime.now());
//                bank.setUpdatedAt(LocalDateTime.now());
//                bank.setCompanyRegistration(company);
//
//                bankRepository.save(bank);
//            }
//        }
        if (bankName != null && !bankName.isEmpty()) {

            List<BankDetails> existingBanks = bankRepository.findByCompanyRegistration(company);
            existingBanks.forEach(bank -> bank.setIsDeleted(true));

            for (int i = 0; i < bankName.size(); i++) {

                BankDetails bank = new BankDetails();
                bank.setBankName(bankName.get(i));

                if (ifscCode != null && ifscCode.size() > i)
                    bank.setIfscCode(ifscCode.get(i));

                if (branch != null && branch.size() > i)
                    bank.setBranch(branch.get(i));

                if (accountNumber != null && accountNumber.size() > i)
                    bank.setAccountNumber(accountNumber.get(i));

                if (accHolderName != null && accHolderName.size() > i)
                    bank.setAccHolderName(accHolderName.get(i));

                if (bankAddress != null && bankAddress.size() > i)
                    bank.setBankAddress(bankAddress.get(i));

                // ✅ Status Safe Handling
                if (bankStatus != null && bankStatus.size() > i) {
                    bank.setStatus(bankStatus.get(i));
                } else {
                    bank.setStatus(true); // default value
                }

                bank.setIsDeleted(false);
                bank.setCreatedAt(LocalDateTime.now());
                bank.setUpdatedAt(LocalDateTime.now());
                bank.setCompanyRegistration(company);

                bankRepository.save(bank);
            }
        }


        company.setUpdatedAt(LocalDateTime.now());
        companyRepository.save(company);

        return new MessageResponse(true, HttpStatus.OK, "Company Updated Successfully");
    }

    @Override
    public DataResponse getCompanyPassword(int companyId, Map<String, String> headers) {
        CompanyRegistration company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company with ID '" + companyId + "' not found!"));

        // You can wrap the password in a map to keep it clean and structured
        Map<String, String> passwordMap = new HashMap<>();
        passwordMap.put("password", company.getPassword());

        return new DataResponse(true, HttpStatus.OK, passwordMap);
    }


    @Override
    public MessageResponse softDeleteCompanyById(int companyId, Map<String, String> headers) {
        // Retrieve Authorization header from request
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || authHeader.isEmpty()) {
            throw new UserUnauthorizedException("Authorization header is missing!");
        }

        // Fetch user from DB
        CompanyRegistration company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company with ID '" + companyId + "' not found!"));


        company.setIsDeleted(true);
        company.setDeletedAt(LocalDateTime.now());
        companyRepository.save(company);

        return new MessageResponse(true, HttpStatus.OK, "Company with ID '" + companyId + "' soft deleted.");
    }

    @Override
    public MessageResponse restoreCompanyById(int companyId, Map<String, String> headers) {
        // Retrieve Authorization header from request
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || authHeader.isEmpty()) {
            throw new UserUnauthorizedException("Authorization header is missing!");
        }

        // Fetch user from DB
        CompanyRegistration company = companyRepository.findById(companyId).
                orElseThrow(() -> new ResourceNotFoundException("Company with id '" + companyId + "' not found!"));


        company.setIsDeleted(false);
        company.setDeletedAt(null);
        companyRepository.save(company);

        return new MessageResponse(true, HttpStatus.OK, "Company with ID '" + companyId + "' restored.");
    }

    @Override
    public MessageResponse uploadLogoPicture(MultipartFile file, int companyId, Map<String, String> headers) throws IOException {
        Optional<CompanyRegistration> companyOptional = companyRepository.findById(companyId);

        if (companyOptional.isEmpty()) {
            throw new ResourceNotFoundException("Company not found with ID: " + companyId);
        }

        CompanyRegistration company = companyOptional.get();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }

        byte[] logoBytes = file.getBytes();
        company.setLogo(Arrays.toString(logoBytes));

        companyRepository.save(company);

        return new MessageResponse(true, HttpStatus.OK, "Logo uploaded successfully.");
    }


//    @Override
//    public MessageResponse uploadProfilePicture(MultipartFile file, Map<String,String> headers) throws IOException {
//        DataSize dataSize = DataSize.ofMegabytes(5);
//        if(file == null)
//            throw new ResourceNotFoundException("File is Empty.");
//        if(file.getSize() > dataSize.toBytes())
//            throw new ResourceNotFoundException("File size exceeds the limit (5MB)");
//        return awsService.uploadFile(file, headers);
//    }


}









