package com.vaistra.repository;

import com.vaistra.entity.CompanyRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyRegistration, Integer> {

    boolean existsByCompanyNameIgnoreCase(String companyName);

    CompanyRegistration findByCompanyNameIgnoreCase(String companyName);
}
