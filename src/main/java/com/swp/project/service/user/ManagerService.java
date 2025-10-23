package com.swp.project.service.user;

import com.swp.project.dto.EditManagerDto;
import com.swp.project.dto.ManagerRegisterDto;
import com.swp.project.dto.ViewManagerDto;
import com.swp.project.entity.address.CommuneWard;
import com.swp.project.entity.user.Manager;
import com.swp.project.listener.event.UserDisabledEvent;
import com.swp.project.repository.address.CommuneWardRepository;
import com.swp.project.repository.user.ManagerRepository;
import com.swp.project.repository.user.SellerRepository;
import com.swp.project.repository.user.ShipperRepository;
import com.swp.project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@RequiredArgsConstructor
@Service
public class ManagerService {
    private final ManagerRepository managerRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CommuneWardRepository communeWardRepository;
    private final SellerRepository sellerRepository;
    private final ShipperRepository shipperRepository;
    public Manager getManagerById(Long id) {
        return managerRepository.findById(id).orElse(null);
    }

    @Transactional
    public void initManager() {
        List<CommuneWard> wards = communeWardRepository.findAll();
        CommuneWard ward = wards.isEmpty() ? null : wards.get(0);
        
        for (int i = 1; i <= 4; i++) {
            createManagerIfNotExists(Manager.builder()
                    .fullname("Manager " + i)
                    .email("manager" + i + "@shop.com")
                    .password("manager")
                    .communeWard(ward)
                    .specificAddress("123 Main St, City " + i)
                    .birthDate(LocalDate.of(2000, 1, i))
                    .cid("ID" + i)
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
        Manager existingManager = managerRepository.findById(id).orElseThrow(
            () -> new IllegalArgumentException("Không tìm thấy quản lý")
        );
        CommuneWard ward = communeWardRepository.findById(updatedManager.getCommuneWardCode())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy xã"));
        boolean isEnabled = Boolean.TRUE.equals(updatedManager.getStatus());
        if (!existingManager.getEmail().equals(updatedManager.getEmail()) && userRepository.existsByEmail(updatedManager.getEmail())) {
            throw new IllegalArgumentException("Mail đã được sử dụng");
        }
        if(!existingManager.getCid().equals(updatedManager.getCId()) 
            && (sellerRepository.findByCid(updatedManager.getCId()) != null ||
                shipperRepository.findByCid(updatedManager.getCId()) != null ||
                managerRepository.findByCid(updatedManager.getCId()) != null)) {
            throw new IllegalArgumentException("Căn cước công dân đã được sử dụng");
        }
        existingManager.setEmail(updatedManager.getEmail());
        existingManager.setFullname(updatedManager.getFullname());
        existingManager.setBirthDate(updatedManager.getBirthDate());
        existingManager.setCid(updatedManager.getCId());
        existingManager.setSpecificAddress(updatedManager.getSpecificAddress());
        existingManager.setCommuneWard(ward);
        existingManager.setEnabled(isEnabled);

        if(!isEnabled) eventPublisher.publishEvent(new UserDisabledEvent(existingManager.getEmail()));

        managerRepository.save(existingManager);
    }

    public void createManager(ManagerRegisterDto registerDto, BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            FieldError fieldError = bindingResult.getFieldErrors().get(0);
            String message = fieldError.getField() + ": " + fieldError.getDefaultMessage();
            throw new RuntimeException(message);
        }
        CommuneWard ward = communeWardRepository.findById(registerDto.getCommuneWardCode())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy xã"));
        if (!registerDto.getConfirmPassword().equals(registerDto.getPassword())) {
            throw new RuntimeException("Mật khẩu và xác nhận mật khẩu không khớp");
        }
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new IllegalArgumentException("Mail đã được sử dụng");
        }
        if(sellerRepository.findByCid(registerDto.getCId()) != null ||
           shipperRepository.findByCid(registerDto.getCId()) != null ||
           managerRepository.findByCid(registerDto.getCId()) != null) {
            throw new IllegalArgumentException("Căn cước công dân đã được sử dụng");
        }
        Manager manager = Manager.builder()
            .email(registerDto.getEmail())
            .password(passwordEncoder.encode(registerDto.getPassword()))
            .fullname(registerDto.getFullname())
            .birthDate(registerDto.getBirthDate())
            .cid(registerDto.getCId())
            .communeWard(ward)
            .specificAddress(registerDto.getSpecificAddress())
            .build();
        managerRepository.save(manager);
    }

    public List<ViewManagerDto> getAllViewManager(){
        return managerRepository.findAll().stream()
            .map(m -> new ViewManagerDto(m.getId(), m.getEmail(),m.isEnabled()))
            .toList();
    }

   
    
}
