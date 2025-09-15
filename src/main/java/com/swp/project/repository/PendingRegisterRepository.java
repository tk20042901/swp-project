package com.swp.project.repository;

import com.swp.project.entity.PendingRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface PendingRegisterRepository extends JpaRepository<PendingRegister, String> {
    PendingRegister findByEmail(String email);
    void deleteByOtpExpiryTimeBefore(Instant otpExpiryTimeBefore);
}