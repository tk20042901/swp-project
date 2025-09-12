package com.swp.project.service.user;

import com.swp.project.entity.user.Manager;
import com.swp.project.listener.event.UserDisabledEvent;
import com.swp.project.repository.user.ManagerRepository;
import lombok.RequiredArgsConstructor;
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

    public Manager getManagerById(Long id) {
        return managerRepository.findById(id).orElse(null);
    }

    @Transactional
    public void setManagerStatus(Long id, boolean status) {
        Manager manager = getManagerById(id);
        manager.setStatus(status);

        if (!status) {
            eventPublisher.publishEvent(new UserDisabledEvent(manager.getEmail()));
        }

        managerRepository.save(manager);
    }

    @Transactional
    public void initManager() {
        String[] managers = {"manager1", "manager2", "manager3"};
        for (String manager : managers) {
            createManagerIfNotExists(Manager.builder()
                    .email(manager + "@manager.com")
                    .password(manager)
                    .build());
        }
        createManagerIfNotExists(Manager.builder()
                .email("disabled@manager.com")
                .password("disabled")
                .status(false)
                .build());
    }


    private void createManagerIfNotExists(Manager manager) {
        if (!managerRepository.existsByEmail(manager.getEmail())) {
            manager.setPassword(passwordEncoder.encode(manager.getPassword()));
            managerRepository.save(manager);
        }
    }
}
