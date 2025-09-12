package com.swp.project.repository.user;

import com.swp.project.entity.user.PendingRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface PendingRegisterRepository extends JpaRepository<PendingRegister, Long> {
    PendingRegister findByEmail(String email);
    void deleteByOtpExpiryTimeBefore(Instant otpExpiryTimeBefore);
}