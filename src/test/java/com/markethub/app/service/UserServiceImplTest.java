package com.markethub.app.service;

import com.markethub.app.DTO.SignUp;
import com.markethub.app.model.Role;
import com.markethub.app.model.ShoppingCart;
import com.markethub.app.model.User;
import com.markethub.app.repository.RoleRepository;
import com.markethub.app.repository.ShoppingCartRepository;
import com.markethub.app.repository.UserRepository;
import com.markethub.app.service.imp.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ShoppingCartRepository shoppingCartRepository;
    @InjectMocks private UserServiceImpl userService;

    @Test
    void registerUser_success() {
        SignUp signUp = new SignUp("John", "Doe", "johndoe", "john@test.com", "password123", "BUYER");

        when(userRepository.existsByUserName("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(roleRepository.findByRoleType("ROLE_BUYER")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(new Role(1L, "ROLE_BUYER", null));
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenReturn(new ShoppingCart());

        User saved = new User();
        saved.setUserId(1L);
        saved.setUserName("johndoe");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = userService.registerUser(signUp);

        assertNotNull(result);
        assertEquals("johndoe", result.getUserName());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_duplicateUsername_throwsException() {
        SignUp signUp = new SignUp("John", "Doe", "johndoe", "john@test.com", "password123", "BUYER");
        when(userRepository.existsByUserName("johndoe")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.registerUser(signUp));
        assertTrue(ex.getMessage().contains("johndoe"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_duplicateEmail_throwsException() {
        SignUp signUp = new SignUp("John", "Doe", "johndoe", "john@test.com", "password123", "BUYER");
        when(userRepository.existsByUserName("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.registerUser(signUp));
        assertTrue(ex.getMessage().contains("john@test.com"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_existingRole_reusesRole() {
        SignUp signUp = new SignUp("Jane", "Smith", "janesmith", "jane@test.com", "pass123", "SELLER");
        Role existingRole = new Role(2L, "ROLE_SELLER", null);

        when(userRepository.existsByUserName("janesmith")).thenReturn(false);
        when(userRepository.existsByEmail("jane@test.com")).thenReturn(false);
        when(roleRepository.findByRoleType("ROLE_SELLER")).thenReturn(Optional.of(existingRole));
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenReturn(new ShoppingCart());
        when(userRepository.save(any(User.class))).thenReturn(new User());

        userService.registerUser(signUp);

        verify(roleRepository, never()).save(any());
    }

    @Test
    void approveSeller_success() {
        User seller = new User();
        seller.setUserId(1L);
        seller.setApprovedSeller(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(userRepository.save(seller)).thenReturn(seller);

        User result = userService.approveSeller(1L);

        assertTrue(result.isApprovedSeller());
        verify(userRepository).save(seller);
    }

    @Test
    void approveSeller_notFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.approveSeller(99L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getAllUsers_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));

        List<User> users = userService.getAllUsers();

        assertEquals(2, users.size());
    }
}
