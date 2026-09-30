package com.markethub.app.service.imp;

import com.markethub.app.DTO.SignUp;
import com.markethub.app.exception.DuplicateResourceException;
import com.markethub.app.exception.ResourceNotFoundException;
import com.markethub.app.model.Role;
import com.markethub.app.model.ShoppingCart;
import com.markethub.app.model.User;
import com.markethub.app.repository.RoleRepository;
import com.markethub.app.repository.ShoppingCartRepository;
import com.markethub.app.repository.UserRepository;
import com.markethub.app.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final Set<String> SELF_REGISTRATION_ROLES = Set.of("BUYER", "SELLER");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ShoppingCartRepository shoppingCartRepository;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder, ShoppingCartRepository shoppingCartRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.shoppingCartRepository = shoppingCartRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getUsersPage(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size, Sort.by("lastName")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getSellers() {
        return userRepository.findUsersBySellerRole();
    }

    @Override
    @Transactional
    public User addUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        log.info("Deleting user id={}", id);
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public User registerUser(SignUp signUp) {
        if (userRepository.existsByUserName(signUp.getUserName())) {
            throw new DuplicateResourceException("Username already taken: " + signUp.getUserName());
        }
        if (userRepository.existsByEmail(signUp.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + signUp.getEmail());
        }
        String requestedRole = signUp.getRole() == null ? "" : signUp.getRole().trim().toUpperCase();
        if (!SELF_REGISTRATION_ROLES.contains(requestedRole)) {
            throw new IllegalArgumentException("Account type must be BUYER or SELLER");
        }
        String roleType = "ROLE_" + requestedRole;
        Role role = roleRepository.findByRoleType(roleType).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setRoleType(roleType);
            return roleRepository.save(newRole);
        });
        User user = new User();
        user.setFirstName(signUp.getFirstName());
        user.setLastName(signUp.getLastName());
        user.setUserName(signUp.getUserName());
        user.setEmail(signUp.getEmail());
        user.setPassword(passwordEncoder.encode(signUp.getPassword()));
        user.setApprovedSeller(false);
        user.setRoles(List.of(role));
        user.setShoppingCart(shoppingCartRepository.save(new ShoppingCart()));
        log.info("Registered new user username={} role={}", signUp.getUserName(), roleType);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User approveSeller(long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found: " + sellerId));
        seller.setApprovedSeller(true);
        log.info("Approved seller id={}", sellerId);
        return userRepository.save(seller);
    }

    @Override
    @Transactional
    public ShoppingCart ensureCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (user.getShoppingCart() != null) {
            return user.getShoppingCart();
        }
        ShoppingCart cart = shoppingCartRepository.save(new ShoppingCart());
        user.setShoppingCart(cart);
        userRepository.save(user);
        log.info("Created cart for user id={}", userId);
        return cart;
    }

    @Override
    @Transactional
    public User updateUser(User user, Long id) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        existing.setFirstName(user.getFirstName());
        existing.setLastName(user.getLastName());
        existing.setUserName(user.getUserName());
        existing.setEmail(user.getEmail());
        existing.setRoles(user.getRoles());
        existing.setApprovedSeller(user.isApprovedSeller());
        existing.setAddress(user.getAddress());
        return userRepository.save(existing);
    }
}
