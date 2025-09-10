package com.swp.project.initializer;

import com.swp.project.service.RoleService;
import com.swp.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DataInitializer implements CommandLineRunner {
    private final UserService userService;
    private final RoleService roleService;

    @Override
    public void run(String... args) {
        roleService.initializeDefaultRoles();
        userService.initDefaultUser();
    }
}
