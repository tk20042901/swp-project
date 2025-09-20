package com.swp.project.service.user;

import com.swp.project.dto.EditManagerDto;
import com.swp.project.dto.ManagerRegisterDto;
import com.swp.project.dto.ViewManagerDto;
import com.swp.project.entity.user.Manager;
import com.swp.project.listener.event.UserDisabledEvent;
import com.swp.project.repository.user.ManagerRepository;

import com.swp.project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ManagerService {
    private final ManagerRepository managerRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public Manager getManagerById(Long id) {
        return managerRepository.findById(id).orElse(null);
    }

    @Transactional
    public void setManagerStatus(Long id, boolean status) {
        Manager manager = getManagerById(id);
        manager.setEnabled(status);

        if (!status) {
            eventPublisher.publishEvent(new UserDisabledEvent(manager.getEmail()));
        }

        managerRepository.save(manager);
    }

    @Transactional
    public void initManager() {
        for (int i = 1; i <= 4; i++) {
            createManagerIfNotExists(Manager.builder()
                    .fullname("Manager " + i)
                    .email("manager" + i + "@shop.com")
                    .password("manager")
                    .provinceCityCode("01")
                    .communeWardCode("001")
                    .specificAddress("123 Main St, City " + i)
                    .birthDate(LocalDate.of(2000, 1, i))
                    .cId("ID" + i)  
                    .build());
        }
    }

    private void createManagerIfNotExists(Manager manager) {
        if (!userRepository.existsByEmail(manager.getEmail())) {
            manager.setPassword(passwordEncoder.encode(manager.getPassword()));
            managerRepository.save(manager);
        }
    }

    public void updateManager(Long id, EditManagerDto updatedManager) {
        Manager existingManager = getManagerById(id);
        if(existingManager == null) {
            throw new IllegalArgumentException("Manager not found.");
        }
        if (!existingManager.getEmail().equals(updatedManager.getEmail()) && userRepository.existsByEmail(updatedManager.getEmail())) {
            throw new IllegalArgumentException("Email already in use.");
        }
        existingManager.setEmail(updatedManager.getEmail());
        existingManager.setFullname(updatedManager.getFullname());
        existingManager.setBirthDate(updatedManager.getBirthDate());
        existingManager.setCId(updatedManager.getCId());
        existingManager.setProvinceCityCode(updatedManager.getProvinceCityCode());
        existingManager.setCommuneWardCode(updatedManager.getCommuneWardCode());
        existingManager.setSpecificAddress(updatedManager.getSpecificAddress());
        managerRepository.save(existingManager);
    }

    public void createManager(ManagerRegisterDto registerDto) {
        if (!registerDto.getConfirmPassword().equals(registerDto.getPassword())) {
            throw new RuntimeException("Mật khẩu và xác nhận mật khẩu không khớp");
        }
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new IllegalArgumentException("Email already in use.");
        }
        Manager manager = Manager.builder()
            .email(registerDto.getEmail())
            .password(passwordEncoder.encode(registerDto.getPassword()))
            .fullname(registerDto.getFullname())
            .build();
        if(!registerDto.getBirthDate().toString().isBlank()){
            manager.setBirthDate(registerDto.getBirthDate());
        }
        if(!registerDto.getCId().isBlank()){
            manager.setCId(registerDto.getCId());
        }
        if(!registerDto.getProvinceCityCode().isBlank()){
            manager.setProvinceCityCode(registerDto.getProvinceCityCode());
            System.out.println("Province/City Code: " + registerDto.getProvinceCityCode());
        }
        if(!registerDto.getCommuneWardCode().isBlank()){
            manager.setCommuneWardCode(registerDto.getCommuneWardCode());
            System.out.println("Commune/Ward Code: " + registerDto.getCommuneWardCode());
        }
        if(!registerDto.getSpecificAddress().isBlank()){
            manager.setSpecificAddress(registerDto.getSpecificAddress());
        }
        managerRepository.save(manager);
    }

    public List<ViewManagerDto> getAllViewManager(){
        return managerRepository.findAll().stream()
            .map(m -> new ViewManagerDto(m.getId(), m.getEmail(),m.isEnabled()))
            .toList();
    }
    
}
