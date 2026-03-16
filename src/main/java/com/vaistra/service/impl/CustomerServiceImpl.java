package com.vaistra.service.impl;


import com.vaistra.dto.ContactPersonDTO;
import com.vaistra.dto.CustomerDto;
import com.vaistra.dto.response.DataResponse;
import com.vaistra.dto.response.HttpResponse;
import com.vaistra.dto.response.MessageResponse;
import com.vaistra.dto.update.CustomerUpdateDto;
import com.vaistra.entity.*;
import com.vaistra.exception.DuplicateEntryException;
import com.vaistra.exception.ResourceNotFoundException;
import com.vaistra.repository.CityRepository;
import com.vaistra.repository.CustomerRepository;
import com.vaistra.repository.CompanyRepository;
import com.vaistra.repository.StateRepository;
import com.vaistra.service.CustomerService;
import com.vaistra.util.AppUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {


    @PersistenceContext
    private EntityManager entityManager;
    private final CustomerRepository customerRepository;
    private final CompanyRepository companyRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;
    private final AppUtils appUtils;
    private final HttpServletRequest request;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, CompanyRepository companyRepository, StateRepository stateRepository, CityRepository cityRepository, AppUtils appUtils, HttpServletRequest request) {
        this.customerRepository = customerRepository;
        this.companyRepository = companyRepository;
        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
        this.appUtils = appUtils;
        this.request = request;
    }


    @Override
    public MessageResponse addCustomer(CustomerDto customerDto, Map<String, String> headers) {

        if (customerDto.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID is required.");
        }

        CompanyRegistration company = companyRepository.findById(customerDto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + customerDto.getCompanyId()));

        // Check for duplicate email
        if (customerRepository.existsByEmailIgnoreCase(customerDto.getEmail())) {
            throw new DuplicateEntryException("Customer with email '" + customerDto.getEmail() + "' already exists!");
        }

        // Fetch Billing City and State
        City billingCity = cityRepository.findById(customerDto.getBillingCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found with ID: " + customerDto.getBillingCityId()));
        State billingState = stateRepository.findById(customerDto.getBillingStateId())
                .orElseThrow(() -> new ResourceNotFoundException("State not found with ID: " + customerDto.getBillingStateId()));

        State placeOfSupply = stateRepository.findById(customerDto.getPlaceOfSupplyStateId())
                .orElseThrow(() -> new ResourceNotFoundException("Place of Supply State not found with ID: " + customerDto.getPlaceOfSupplyStateId()));

        // Fetch Shipping City and State
        City shippingCity = null;
        State shippingState = null;

        if (!customerDto.isSameAsBillingAddress()) {
            shippingCity = cityRepository.findById(customerDto.getShippingCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("City not found with ID: " + customerDto.getShippingCityId()));
            shippingState = stateRepository.findById(customerDto.getShippingStateId())
                    .orElseThrow(() -> new ResourceNotFoundException("State not found with ID: " + customerDto.getShippingStateId()));
        }

        // Set State Names in DTO
        customerDto.setBillingStateName(billingState.getStateName()); // Set billing state name
        if (!customerDto.isSameAsBillingAddress() && shippingState != null) {
            customerDto.setShippingStateName(shippingState.getStateName()); // Set shipping state name
        }

        // Build Customer entity
        Customer customer = new Customer();
        customer.setCustomerType(customerDto.getCustomerType());
        customer.setVid(customerDto.getVid());
        customer.setSalutation(customerDto.getSalutation());
        customer.setFirstName(customerDto.getFirstName());
        customer.setLastName(customerDto.getLastName());
        customer.setDisplayName(customerDto.getDisplayName());
        customer.setEmail(customerDto.getEmail());
        customer.setWorkPhone(customerDto.getWorkPhone());
        customer.setMobileNumber(customerDto.getMobileNumber());
        customer.setGstNumber(customerDto.getGstNumber());
        customer.setPan(customerDto.getPan());
        customer.setTerms(customerDto.getTerms());
        customer.setCompanyRegistration(company);
        customer.setCustomerCompanyName(customerDto.getCustomerCompanyName());
        customer.setBillingCity(billingCity);
        customer.setBillingState(billingState);
        customer.setShippingCity(shippingCity);
        customer.setShippingState(shippingState);
        customer.setSameAsBillingAddress(customerDto.isSameAsBillingAddress());
        customer.setPlaceOfSupply(placeOfSupply);
        customerDto.setPlaceOfSupplyStateName(placeOfSupply.getStateName());

        customer.setStatus(customerDto.getStatus() != null ? customerDto.getStatus() : true);
        customer.setIsDeleted(false);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setDeletedAt(null);

        // Set Billing Address
        Address billingAddress = new Address();
        billingAddress.setAttention(customerDto.getBillingAttention());
        billingAddress.setAddressLine1(customerDto.getBillingAddressLine1());
        billingAddress.setAddressLine2(customerDto.getBillingAddressLine2());
        billingAddress.setPincode(customerDto.getBillingPincode());
        customer.setBillingAddress(billingAddress);

        // Set Shipping Address
        if (customerDto.isSameAsBillingAddress()) {
            // Clone billing address for shipping
            Address shipping = new Address();
            shipping.setAttention(customerDto.getBillingAttention());
            shipping.setAddressLine1(customerDto.getBillingAddressLine1());
            shipping.setAddressLine2(customerDto.getBillingAddressLine2());
            shipping.setPincode(customerDto.getBillingPincode());
            customer.setShippingAddress(shipping);
        } else {
            Address shipping = new Address();
            shipping.setAttention(customerDto.getShippingAttention());
            shipping.setAddressLine1(customerDto.getShippingAddressLine1());
            shipping.setAddressLine2(customerDto.getShippingAddressLine2());
            shipping.setPincode(customerDto.getShippingPincode());
            customer.setShippingAddress(shipping);
        }

        // Set Contact Persons
        if (customerDto.getContactPersons() != null) {
            List<ContactPerson> contactPersons = appUtils.toEntityContactPersons(customerDto.getContactPersons(), customer);
            customer.setContactPersons(contactPersons);
        }

        // Save to DB
        customerRepository.save(customer);

        return new MessageResponse(true, HttpStatus.OK, "Customer created successfully.");
    }


    @Override
    public HttpResponse getAllCustomers(Integer companyId, String keyword, Integer pageNumber, Integer pageSize,
                                        String sortBy, String sortDirection, String isDeleted, String status,
                                        Map<String, String> headers) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Customer> cq = cb.createQuery(Customer.class);
        Root<Customer> root = cq.from(Customer.class);

        List<Predicate> predicates = new ArrayList<>();

        // --- Soft delete ---
        if (isDeleted != null) {
            Boolean softDeleted = isDeleted.equalsIgnoreCase("true");
            predicates.add(cb.equal(root.get("isDeleted"), softDeleted));
        }

        // --- Company filter ---
        if (companyId != null) {
            predicates.add(cb.equal(root.get("companyRegistration").get("companyId"), companyId));
        }

        // --- Keyword search ---
        if (keyword != null && !keyword.isEmpty()) {
            Predicate keywordPredicate = cb.or(
                    cb.like(cb.lower(root.get("firstName")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("lastName")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("customerCompanyName")), "%" + keyword.toLowerCase() + "%")
            );
            predicates.add(keywordPredicate);
        }

        cq.where(cb.and(predicates.toArray(new Predicate[0])));

        // --- Sorting ---
        cq.orderBy(sortDirection.equalsIgnoreCase("asc") ? cb.asc(root.get(sortBy)) : cb.desc(root.get(sortBy)));

        // --- Pagination ---
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<Customer> resultList = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        long totalCount = entityManager.createQuery(cq).getResultList().size();
        Page<Customer> pageCustomer = new PageImpl<>(resultList, pageable, totalCount);

        // --- Mapping to DTOs ---
        List<CustomerDto> dtos = appUtils.customersToDtos(resultList);

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .pageNumber(pageCustomer.getNumber())
                .pageSize(pageCustomer.getSize())
                .totalElements(pageCustomer.getTotalElements())
                .totalPages(pageCustomer.getTotalPages())
                .isLastPage(pageCustomer.isLast())
                .data(dtos)
                .build();
    }

    @Override
    public MessageResponse updateCustomer(CustomerUpdateDto customerDto, int customerId, Map<String, String> headers) {
        // Fetch existing customer
        Customer existingCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        // Update customer fields
        existingCustomer.setVid(customerDto.getVid());
        existingCustomer.setSalutation(customerDto.getSalutation());
        existingCustomer.setFirstName(customerDto.getFirstName());
        existingCustomer.setLastName(customerDto.getLastName());
        existingCustomer.setDisplayName(customerDto.getDisplayName());
        existingCustomer.setCustomerCompanyName(customerDto.getCustomerCompanyName());

        // Set company name from company id
        if (customerDto.getCompanyId() != null) {
            CompanyRegistration company = companyRepository.findById(customerDto.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + customerDto.getCompanyId()));

            existingCustomer.setCompanyRegistration(company);
        }

        if (customerDto.getPlaceOfSupplyStateId() != null) {
            State state = stateRepository.findById(customerDto.getPlaceOfSupplyStateId())
                    .orElseThrow(() -> new ResourceNotFoundException("State not found with ID: " + customerDto.getPlaceOfSupplyStateId()));
            existingCustomer.setPlaceOfSupply(state);
        }


        existingCustomer.setEmail(customerDto.getEmail());
        existingCustomer.setWorkPhone(customerDto.getWorkPhone());
        existingCustomer.setMobileNumber(customerDto.getMobileNumber());
        existingCustomer.setGstNumber(customerDto.getGstNumber());
        existingCustomer.setPan(customerDto.getPan());
        existingCustomer.setTerms(customerDto.getTerms());
        existingCustomer.setUpdatedAt(LocalDateTime.now());


        // Update Billing Address
        Address billingAddress = new Address();
        billingAddress.setAttention(customerDto.getBillingAttention());
        billingAddress.setAddressLine1(customerDto.getBillingAddressLine1());
        billingAddress.setAddressLine2(customerDto.getBillingAddressLine2());
        billingAddress.setPincode(customerDto.getBillingPincode());

        // Fetch City and State for Billing Address (if provided)

        existingCustomer.setBillingAddress(billingAddress);

        // Update Shipping Address (same logic as Billing Address)
        Address shippingAddress = new Address();
        shippingAddress.setAttention(customerDto.getShippingAttention());
        shippingAddress.setAddressLine1(customerDto.getShippingAddressLine1());
        shippingAddress.setAddressLine2(customerDto.getShippingAddressLine2());
        shippingAddress.setPincode(customerDto.getShippingPincode());


        // If same as billing address, set shipping address to billing address
        if (customerDto.isSameAsBillingAddress()) {
            shippingAddress = billingAddress;
        }

        existingCustomer.setShippingAddress(shippingAddress);

        // Update Contact Persons
        // Update Contact Persons
        if (customerDto.getContactPersons() != null) {
            List<ContactPerson> updatedContactPersons = new ArrayList<>();

            for (ContactPersonDTO cpDto : customerDto.getContactPersons()) {
                ContactPerson existingCp = null;

                // Try to find existing contact person by email (or use ID if available)
                if (cpDto.getEmail() != null) {
                    existingCp = existingCustomer.getContactPersons().stream()
                            .filter(cp -> cpDto.getEmail().equals(cp.getEmail()))
                            .findFirst()
                            .orElse(null);
                }

                if (existingCp == null) {
                    // New contact person
                    existingCp = new ContactPerson();
                    existingCp.setCustomer(existingCustomer);
                }

                // Set / update fields
                existingCp.setSalutation(cpDto.getSalutation());
                existingCp.setFirstName(cpDto.getFirstName());
                existingCp.setLastName(cpDto.getLastName());
                existingCp.setEmail(cpDto.getEmail());
                existingCp.setWorkPhone(cpDto.getWorkPhone());
                existingCp.setMobileNumber(cpDto.getMobileNumber());

                updatedContactPersons.add(existingCp);
            }

            existingCustomer.setContactPersons(updatedContactPersons);
        }

        customerRepository.save(existingCustomer);

        return new MessageResponse(true, HttpStatus.OK, "Customer updated successfully.");
    }



    @Override
    public DataResponse getCustomerById(int customerId, Map<String, String> headers) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        if (customerOptional.isPresent()) {
            CustomerDto customerDto = appUtils.customerToDto(customerOptional.get());
            return new DataResponse(true, HttpStatus.OK, customerDto);
        } else {
            throw new ResourceNotFoundException("Customer details with ID '" + customerId + "' not found.");
        }
    }

    @Override
    public MessageResponse softDeleteCustomerById(int customerId, Map<String, String> headers) {
        // Fetch customer from DB
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer with ID '" + customerId + "' not found!"));

        customer.setIsDeleted(true);
        customer.setDeletedAt(LocalDateTime.now());
        customerRepository.save(customer);

        return new MessageResponse(true, HttpStatus.OK, "Customer with ID '" + customerId + "' soft deleted.");
    }

    @Override
    public MessageResponse restoreCustomerById(int customerId, Map<String, String> headers) {
        // Fetch customer from DB
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer with ID '" + customerId + "' not found!"));

        customer.setIsDeleted(false);
        customer.setDeletedAt(null);
        customerRepository.save(customer);

        return new MessageResponse(true, HttpStatus.OK, "Customer with ID '" + customerId + "' restored.");
    }
}
