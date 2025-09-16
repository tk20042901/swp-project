package com.swp.project.service.user;

import com.swp.project.entity.user.Manager;
import com.swp.project.listener.event.UserDisabledEvent;
import com.swp.project.repository.user.ManagerRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ManagerService {
    private final ManagerRepository managerRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

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
        for (int i = 1; i <= 36; i++) {
            createManagerIfNotExists(Manager.builder()
                    .email("manager" + i + "@shop.com")
                    .password("manager")
                    .build());
        }
        createManagerIfNotExists(Manager.builder()
                .email("disabled-manager@shop.com")
                .password("manager")
                .enabled(false)
                .build());
    }

    private void createManagerIfNotExists(Manager manager) {
        if (!managerRepository.existsByEmail(manager.getEmail())) {
            manager.setPassword(passwordEncoder.encode(manager.getPassword()));
            managerRepository.save(manager);
        }
    }

    public void updateManager(Long id, Manager updatedManager) {
        Manager existingManager = getManagerById(id);
        if(existingManager == null) {
            throw new IllegalArgumentException("Manager not found.");
        }
        if (!existingManager.getEmail().equals(updatedManager.getEmail()) && managerRepository.existsByEmail(updatedManager.getEmail())) {
            throw new IllegalArgumentException("Email already in use.");
        }
        existingManager.setEmail(updatedManager.getEmail());
        if(updatedManager.getPassword() != null && !updatedManager.getPassword().isEmpty() && !existingManager.getPassword().equals(updatedManager.getPassword())) {
            existingManager.setPassword(passwordEncoder.encode(updatedManager.getPassword()));
        }
        managerRepository.save(existingManager);
    }

    public void createManager(Manager manager) {
        if (managerRepository.existsByEmail(manager.getEmail())) {
            throw new IllegalArgumentException("Email already in use.");
        }
        manager.setPassword(passwordEncoder.encode(manager.getPassword()));
        managerRepository.save(manager);
    }

    public Page<Manager> getAllManagers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return managerRepository.findAll(pageable);
    }
    
}
