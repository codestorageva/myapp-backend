package com.vaistra.service.impl;


import com.vaistra.dto.ProductDto;
import com.vaistra.dto.response.DataResponse;
import com.vaistra.dto.response.HttpResponse;
import com.vaistra.dto.response.MessageResponse;
import com.vaistra.dto.update.ProductUpdateDto;
import com.vaistra.entity.CompanyRegistration;
import com.vaistra.entity.InvoiceGenerator;
import com.vaistra.entity.Product;
import com.vaistra.exception.ResourceNotFoundException;
import com.vaistra.repository.CompanyRepository;
import com.vaistra.repository.InvoiceRepository;
import com.vaistra.repository.ProductRepository;
import com.vaistra.service.ProductService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {

    @PersistenceContext
    private EntityManager entityManager;
    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final AppUtils appUtils;
    private final HttpServletRequest request;

    @Autowired
    private RestTemplate restTemplate;

    public ProductServiceImpl(InvoiceRepository invoiceRepository, ProductRepository productRepository, CompanyRepository companyRepository, AppUtils appUtils, HttpServletRequest request) {
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
        this.companyRepository = companyRepository;
        this.appUtils = appUtils;
        this.request = request;
    }

    @Override
    public MessageResponse addProduct(ProductDto productDto,Map<String, String> headers) {


        // Validate company ID
        if (productDto.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID is required.");
        }

        CompanyRegistration company = companyRepository.findById(productDto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + productDto.getCompanyId()));


        // Validate type
        String type = productDto.getType();
        if (type == null || (!type.equalsIgnoreCase("Goods") && !type.equalsIgnoreCase("Service"))) {
            throw new ResourceNotFoundException("Product type must be either 'Goods' or 'Service'.");
        }

        if (type.equalsIgnoreCase("Goods") && (productDto.getHsnCode() == null || productDto.getHsnCode().isBlank())) {
            throw new ResourceNotFoundException("HSN code is required for Goods.");
        }
        if (type.equalsIgnoreCase("Service") && (productDto.getSacCode() == null || productDto.getSacCode().isBlank())) {
            throw new ResourceNotFoundException("SAC code is required for Services.");
        }

        // Fetch the invoice entity
//        InvoiceGenerator invoice = invoiceRepository.findById(productDto.getInvoiceId())
//                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + productDto.getInvoiceId()));

        // Map fields to entity
        Product product = new Product();
        product.setProductName(productDto.getProductName());
        product.setType(type);
        product.setHsnCode(type.equalsIgnoreCase("Goods") ? productDto.getHsnCode() : null);
        product.setSacCode(type.equalsIgnoreCase("Service") ? productDto.getSacCode() : null);
        product.setUnit(productDto.getUnit());
        product.setTaxPreference(productDto.getTaxPreference());
        product.setSellingPrice(productDto.getSellingPrice());
        product.setQuantity(productDto.getQuantity());
        product.setRate(productDto.getRate());
        product.setTaxValue(productDto.getTaxValue());
        product.setGstPercent(productDto.getGstPercent());
        product.setCgstAmount(productDto.getCgstAmount());
        product.setSgstAmount(productDto.getSgstAmount());
        product.setIgstAmount(productDto.getIgstAmount());
        product.setNetAmount(productDto.getNetAmount());
        product.setDescription(productDto.getDescription());


        product.setCompanyRegistration(company); // Set the company entity


        product.setStatus(productDto.getStatus() != null ? productDto.getStatus() : true);
        product.setIsDeleted(false);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setDeletedAt(null);

        product.setMiningProduct(productDto.getMiningProduct());

        if (Boolean.TRUE.equals(productDto.getMiningProduct())) {
            product.setRoyalty(productDto.getRoyalty());
            product.setDmf(productDto.getDmf());
            product.setNmet(productDto.getNmet());
        } else {
            product.setRoyalty(null);
            product.setDmf(null);
            product.setNmet(null);
        }


        // Link product to the invoice
//        product.setInvoice(invoice);

        // Save product
        productRepository.save(product);

        return new MessageResponse(true, HttpStatus.OK, "Product added successfully.");
    }



    @Override
    public HttpResponse getAllProducts(
            Integer companyId,
            String keyword,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortDirection,
            String isDeleted,
            String status,
            Map<String, String> headers) {

        Page<Product> pageProduct = null;
        List<ProductDto> dtos = null;

        Integer intKeyword = null;
        Boolean softDeleted = null;
        Boolean isActiveStatus = null;

        // Convert isDeleted
        if ("true".equalsIgnoreCase(isDeleted)) {
            softDeleted = true;
        } else if ("false".equalsIgnoreCase(isDeleted)) {
            softDeleted = false;
        }

        // Convert status
        if ("true".equalsIgnoreCase(status)) {
            isActiveStatus = true;
        } else if ("false".equalsIgnoreCase(status)) {
            isActiveStatus = false;
        }

        // Try convert keyword to Integer
        try {
            intKeyword = Integer.parseInt(keyword);
        } catch (Exception ignored) {}

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Product> cq = cb.createQuery(Product.class);
            Root<Product> root = cq.from(Product.class);

            // Sorting
            Order order = "asc".equalsIgnoreCase(sortDirection)
                    ? cb.asc(root.get(sortBy))
                    : cb.desc(root.get(sortBy));
            cq.orderBy(order);

            List<Predicate> predicates = new ArrayList<>();

            //  Company filter (MAIN FIX)
            if (companyId != null) {
                predicates.add(
                        cb.equal(
                                root.get("companyRegistration").get("companyId"),
                                companyId
                        )
                );
            }

            // Status filter
            if (isActiveStatus != null) {
                predicates.add(cb.equal(root.get("status"), isActiveStatus));
            }

            // Soft delete filter
            if (softDeleted != null) {
                predicates.add(cb.equal(root.get("isDeleted"), softDeleted));
            }

            // Keyword filter
            if (intKeyword != null) {
                predicates.add(cb.equal(root.get("productId"), intKeyword));
            } else if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("productName")),
                                "%" + keyword.toLowerCase() + "%"
                        )
                );
            }

            cq.select(root)
                    .where(cb.and(predicates.toArray(new Predicate[0])));

            Pageable pageable = PageRequest.of(pageNumber, pageSize);

            List<Product> resultList = entityManager.createQuery(cq)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();

            long totalCount = entityManager.createQuery(cq)
                    .getResultList()
                    .size();

            pageProduct = new PageImpl<>(resultList, pageable, totalCount);

            dtos = appUtils.productsToDtos(resultList);

        } catch (Exception e) {

            return HttpResponse.builder()
                    .success(false)
                    .successCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .pageNumber(0)
                    .pageSize(0)
                    .totalElements(0L)
                    .totalPages(0)
                    .isLastPage(true)
                    .data(new ArrayList<>())
                    .build();
        }

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .pageNumber(pageProduct.getNumber())
                .pageSize(pageProduct.getSize())
                .totalElements(pageProduct.getTotalElements())
                .totalPages(pageProduct.getTotalPages())
                .isLastPage(pageProduct.isLast())
                .data(dtos)
                .build();
    }



    @Override
    public DataResponse getProductById(int productId, Map<String, String> headers) {
        if (productRepository.findById(productId).isPresent())
            return new DataResponse(true, HttpStatus.OK, appUtils.productToDto(productRepository.findById(productId).get()));
        else
            throw new ResourceNotFoundException("Product with id '" + productId + "' not Found!");
    }

    @Override
    public MessageResponse updateProduct(ProductUpdateDto updateDto, int productId, Map<String, String> headers) {

        // Fetch the existing product
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        // Update basic fields
        if (updateDto.getProductName() != null)
            existingProduct.setProductName(updateDto.getProductName());

        existingProduct.setUnit(updateDto.getUnit());
        existingProduct.setTaxPreference(updateDto.getTaxPreference());
        existingProduct.setSellingPrice(updateDto.getSellingPrice());
        existingProduct.setQuantity(updateDto.getQuantity());
        existingProduct.setRate(updateDto.getRate());
        existingProduct.setTaxValue(updateDto.getTaxValue());
        existingProduct.setGstPercent(updateDto.getGstPercent());
        existingProduct.setCgstAmount(updateDto.getCgstAmount());
        existingProduct.setSgstAmount(updateDto.getSgstAmount());
        existingProduct.setIgstAmount(updateDto.getIgstAmount());
        existingProduct.setNetAmount(updateDto.getNetAmount());
        existingProduct.setDescription(updateDto.getDescription());

        // Update type and type-specific validation
        if (updateDto.getType() != null) {
            String type = updateDto.getType();

            if (!type.equalsIgnoreCase("Goods") && !type.equalsIgnoreCase("Service")) {
                throw new ResourceNotFoundException("Product type must be either 'Goods' or 'Service'.");
            }

            existingProduct.setType(type);

            if (type.equalsIgnoreCase("Goods")) {
                if (updateDto.getHsnCode() == null || updateDto.getHsnCode().isBlank()) {
                    throw new ResourceNotFoundException("HSN code is required for Goods.");
                }
                existingProduct.setHsnCode(updateDto.getHsnCode());
                existingProduct.setSacCode(null); // clear SAC if switching from Service
            } else if (type.equalsIgnoreCase("Service")) {
                if (updateDto.getSacCode() == null || updateDto.getSacCode().isBlank()) {
                    throw new ResourceNotFoundException("SAC code is required for Service.");
                }
                existingProduct.setSacCode(updateDto.getSacCode());
                existingProduct.setHsnCode(null); // clear HSN if switching from Goods
            }
        }

        // Update company if provided
        if (updateDto.getCompanyId() != null) {
            CompanyRegistration company = companyRepository.findById(updateDto.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company with ID '" + updateDto.getCompanyId() + "' not found!"));
            existingProduct.setCompanyRegistration(company);
        }

        // Update mining product fields
        if (updateDto.getMiningProduct() != null) {
            existingProduct.setMiningProduct(updateDto.getMiningProduct());

            if (Boolean.TRUE.equals(updateDto.getMiningProduct())) {
                existingProduct.setRoyalty(updateDto.getRoyalty());
                existingProduct.setDmf(updateDto.getDmf());
                existingProduct.setNmet(updateDto.getNmet());
            } else {
                existingProduct.setRoyalty(null);
                existingProduct.setDmf(null);
                existingProduct.setNmet(null);
            }
        }

        // Update status and deletion flag
        existingProduct.setStatus(updateDto.getStatus() != null ? updateDto.getStatus() : existingProduct.getStatus());
        existingProduct.setIsDeleted(updateDto.getIsDeleted() != null ? updateDto.getIsDeleted() : existingProduct.getIsDeleted());

        // Set audit timestamps
        existingProduct.setUpdatedAt(LocalDateTime.now());

        // Optional: update invoice association if invoiceId is provided
        if (updateDto.getInvoiceId() != null) {
            InvoiceGenerator invoice = invoiceRepository.findById(updateDto.getInvoiceId())
                    .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + updateDto.getInvoiceId()));
            // existingProduct.setInvoice(invoice); // uncomment if you want to link invoice
        }

        // Save the updated product
        productRepository.save(existingProduct);

        return new MessageResponse(true, HttpStatus.OK, "Product updated successfully.");
    }



    @Override
    public MessageResponse softDeleteProductById(int productId, Map<String, String> headers) {
        // Fetch user from DB
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID '" + productId + "' not found!"));


        product.setIsDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);

        return new MessageResponse(true, HttpStatus.OK, "Product with ID '" + productId + "' soft deleted.");
    }

    @Override
    public MessageResponse restoreProductById(int productId, Map<String, String> headers) {
        // Fetch user from DB
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID '" + productId + "' not found!"));


        product.setIsDeleted(false);
        product.setDeletedAt(null);
        productRepository.save(product);

        return new MessageResponse(true, HttpStatus.OK, "Product with ID '" + productId + "' restored.");
    }
}
