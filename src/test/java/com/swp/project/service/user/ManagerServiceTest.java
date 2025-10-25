package com.swp.project.service.user;

import com.swp.project.dto.ManagerRegisterDto;
import com.swp.project.entity.address.CommuneWard;
import com.swp.project.entity.user.Manager;
import com.swp.project.entity.user.Seller;
import com.swp.project.entity.user.Shipper;
import com.swp.project.repository.address.CommuneWardRepository;
import com.swp.project.repository.user.ManagerRepository;
import com.swp.project.repository.user.SellerRepository;
import com.swp.project.repository.user.ShipperRepository;
import com.swp.project.repository.user.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private CommuneWardRepository communeWardRepository;
    
    @Mock
    private SellerRepository sellerRepository;
    
    @Mock
    private ShipperRepository shipperRepository;
    
    @InjectMocks
    private ManagerService managerService;

    private ManagerRegisterDto validRegisterDto;
    private CommuneWard mockCommuneWard;

    @BeforeEach
    void setUp() {
        // Setup valid register DTO
        validRegisterDto = new ManagerRegisterDto();
        validRegisterDto.setEmail("manager@test.com");
        validRegisterDto.setPassword("password123");
        validRegisterDto.setConfirmPassword("password123");
        validRegisterDto.setFullname("Test Manager");
        validRegisterDto.setBirthDate(LocalDate.of(1990, 1, 1));
        validRegisterDto.setCId("123456789");
        validRegisterDto.setCommuneWardCode("WARD001");
        validRegisterDto.setSpecificAddress("123 Test Street");
        
        // Setup mock commune ward
        mockCommuneWard = new CommuneWard();
        mockCommuneWard.setCode("WARD001");
        mockCommuneWard.setName("Test Ward");
    }

    @Test
    @DisplayName("Happy case: Tạo manager thành công với dữ liệu hợp lệ")
    @Order(1)
    void createManager_Success_ValidData() {
        // Arrange
        when(communeWardRepository.findById(validRegisterDto.getCommuneWardCode()))
            .thenReturn(Optional.of(mockCommuneWard));
        when(userRepository.existsByEmail(validRegisterDto.getEmail())).thenReturn(false);
        when(sellerRepository.findByCid(validRegisterDto.getCId())).thenReturn(null);
        when(shipperRepository.findByCid(validRegisterDto.getCId())).thenReturn(null);
        when(managerRepository.findByCid(validRegisterDto.getCId())).thenReturn(null);
        when(passwordEncoder.encode(validRegisterDto.getPassword())).thenReturn("encodedPassword");
        
        // Act
        assertDoesNotThrow(() -> managerService.createManager(validRegisterDto));
        
        // Assert
        ArgumentCaptor<Manager> managerCaptor = ArgumentCaptor.forClass(Manager.class);
        verify(managerRepository).save(managerCaptor.capture());
        
        Manager savedManager = managerCaptor.getValue();
        assertEquals(validRegisterDto.getEmail(), savedManager.getEmail());
        assertEquals("encodedPassword", savedManager.getPassword());
        assertEquals(validRegisterDto.getFullname(), savedManager.getFullname());
        assertEquals(validRegisterDto.getBirthDate(), savedManager.getBirthDate());
        assertEquals(validRegisterDto.getCId(), savedManager.getCid());
        assertEquals(mockCommuneWard, savedManager.getCommuneWard());
        assertEquals(validRegisterDto.getSpecificAddress(), savedManager.getSpecificAddress());
        
        verify(passwordEncoder).encode(validRegisterDto.getPassword());
        verify(communeWardRepository).findById(validRegisterDto.getCommuneWardCode());
        verify(userRepository).existsByEmail(validRegisterDto.getEmail());
        verify(sellerRepository).findByCid(validRegisterDto.getCId());
        verify(shipperRepository).findByCid(validRegisterDto.getCId());
        verify(managerRepository).findByCid(validRegisterDto.getCId());
    }

    @Test
    @DisplayName("validateCreateManager: Thành công khi dữ liệu hợp lệ")
    @Order(2)
    void validateCreateManager_Success_ValidData() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        when(userRepository.existsByEmail(validRegisterDto.getEmail())).thenReturn(false);
        when(bindingResult.hasErrors()).thenReturn(false);
        
        // Act & Assert
        assertDoesNotThrow(() -> managerService.validateCreateManager(validRegisterDto, bindingResult));
        
        verify(userRepository).existsByEmail(validRegisterDto.getEmail());
        verify(bindingResult, never()).rejectValue(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("validateCreateManager: Thất bại khi mật khẩu và xác nhận mật khẩu không khớp")
    @Order(3)
    void validateCreateManager_Fail_PasswordMismatch() {
        // Arrange
        validRegisterDto.setConfirmPassword("differentPassword");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(
            java.util.Collections.singletonList(
                new org.springframework.validation.FieldError(
                    "managerRegisterDto", 
                    "confirmPassword", 
                    "Mật khẩu và xác nhận mật khẩu không khớp"
                )
            )
        );
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> managerService.validateCreateManager(validRegisterDto, bindingResult));
        
        assertEquals("confirmPassword: Mật khẩu và xác nhận mật khẩu không khớp", exception.getMessage());
        verify(bindingResult).rejectValue("confirmPassword", "error.confirmPassword", "Mật khẩu và xác nhận mật khẩu không khớp");
    }

    @Test
    @DisplayName("validateCreateManager: Thất bại khi email đã tồn tại")
    @Order(4)
    void validateCreateManager_Fail_EmailAlreadyExists() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        when(userRepository.existsByEmail(validRegisterDto.getEmail())).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(
            java.util.Collections.singletonList(
                new org.springframework.validation.FieldError(
                    "managerRegisterDto",
                    "email",
                    "Mail đã được sử dụng"
                )
            )
        );
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> managerService.validateCreateManager(validRegisterDto, bindingResult));
        
        assertEquals("email: Mail đã được sử dụng", exception.getMessage());
        verify(bindingResult).rejectValue("email", "error.email", "Mail đã được sử dụng");
    }

    @Test
    @DisplayName("createManager: Thất bại khi không tìm thấy xã")
    @Order(5)
    void createManager_Fail_CommuneWardNotFound() {
        // Arrange
        when(communeWardRepository.findById(validRegisterDto.getCommuneWardCode()))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> managerService.createManager(validRegisterDto));
        
        assertEquals("Không tìm thấy xã", exception.getMessage());
        verify(managerRepository, never()).save(any(Manager.class));
    }

    @Test
    @DisplayName("createManager: Thất bại khi mật khẩu và xác nhận mật khẩu không khớp")
    @Order(6)
    void createManager_Fail_PasswordMismatch() {
        // Arrange
        validRegisterDto.setConfirmPassword("differentPassword");
        when(communeWardRepository.findById(validRegisterDto.getCommuneWardCode()))
            .thenReturn(Optional.of(mockCommuneWard));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> managerService.createManager(validRegisterDto));
        
        assertEquals("Mật khẩu và xác nhận mật khẩu không khớp", exception.getMessage());
        verify(managerRepository, never()).save(any(Manager.class));
    }

    @Test
    @DisplayName("createManager: Thất bại khi email đã tồn tại")
    @Order(7)
    void createManager_Fail_EmailAlreadyExists() {
        // Arrange
        when(communeWardRepository.findById(validRegisterDto.getCommuneWardCode()))
            .thenReturn(Optional.of(mockCommuneWard));
        when(userRepository.existsByEmail(validRegisterDto.getEmail())).thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> managerService.createManager(validRegisterDto));
        
        assertEquals("Mail đã được sử dụng", exception.getMessage());
        verify(managerRepository, never()).save(any(Manager.class));
    }
}
