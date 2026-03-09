package com.vaistra.repository;

import com.vaistra.entity.InvoiceGenerator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceGenerator, Integer> {

    boolean existsByInvoiceNumberIgnoreCase(String invoiceNumber);

    Optional<InvoiceGenerator> findTopByOrderByInvoiceNumberDesc();

    Optional<InvoiceGenerator> findTopByInvoiceNumberStartingWithOrderByInvoiceNumberDesc(String prefix);

    long countByInvoicePrefix(String prefix);

    @Query("SELECT i FROM InvoiceGenerator i " +
            "LEFT JOIN FETCH i.items it " +
            "LEFT JOIN FETCH it.product " +
            "LEFT JOIN FETCH i.customer " +
            "LEFT JOIN FETCH i.companyRegistration")
    List<InvoiceGenerator> findAllWithDetails();

    List<InvoiceGenerator> findAllByIsDeletedTrue();
}
