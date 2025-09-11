package com.swp.project.service.user;

import com.swp.project.entity.Role;
import com.swp.project.repository.user.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public Role getAdminRole() {
        return roleRepository.findByName(RoleRepository.ADMIN_ROLE_NAME);
    }

    public Role getManagerRole() {
        return roleRepository.findByName(RoleRepository.MANAGER_ROLE_NAME);
    }

    public Role getCustomerRole() {
        return roleRepository.findByName(RoleRepository.CUSTOMER_ROLE_NAME);
    }

    @Transactional
    public void initializeDefaultRoles() {
        Set.of(
                RoleRepository.CUSTOMER_ROLE_NAME,
                RoleRepository.MANAGER_ROLE_NAME,
                RoleRepository.ADMIN_ROLE_NAME
        ).forEach(roleName -> {
                if(!roleRepository.existsByName(roleName))
                    roleRepository.save(Role.builder().name(roleName).build());
            }
        );
    }
}
