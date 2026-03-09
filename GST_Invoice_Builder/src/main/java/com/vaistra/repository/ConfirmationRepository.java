package com.vaistra.repository;

import com.vaistra.entity.Confirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfirmationRepository extends JpaRepository<Confirmation,Integer> {

    Confirmation findByOtp(String otp);

    Confirmation findByEmailIgnoreCase(String email);

    List<Confirmation> findAllByEmailIgnoreCase(String email);

    Confirmation findByToken(String token);

    boolean existsByOtp(String otp);

    void deleteByEmailIgnoreCase(String email); // Delete OTP entry by email
}
