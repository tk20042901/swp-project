package com.swp.project.service;

import com.swp.project.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class SettingService {

    private final SettingRepository settingRepository;

    public String getShopName() {
        return Objects.requireNonNull(settingRepository.findById("shop_name").orElse(null)).getValue();
    }

    public String getShopAddress() {
        return Objects.requireNonNull(settingRepository.findById("shop_address").orElse(null)).getValue();
    }

    public String getShopPhone() {
        return Objects.requireNonNull(settingRepository.findById("shop_phone").orElse(null)).getValue();
    }

    public String getShopEmail() {
        return Objects.requireNonNull(settingRepository.findById("shop_email").orElse(null)).getValue();
    }
}