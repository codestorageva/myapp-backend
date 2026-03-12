package com.vaistra.repository;

import com.vaistra.entity.BankDetails;
import com.vaistra.entity.CompanyRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankRepository extends JpaRepository<BankDetails, Integer> {

    boolean existsByAccountNumberIgnoreCase(String accountNumber);

    List<BankDetails> findByCompanyRegistration(CompanyRegistration company);

}
