package com.swp.project.repository.auth;

import com.swp.project.entity.PendingRegister;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface PendingRegisterRepository extends JpaRepository<PendingRegister, Long> {
    PendingRegister findByEmail(String email);
    void deleteByOtpExpiryTimeBefore(Instant otpExpiryTimeBefore);
}