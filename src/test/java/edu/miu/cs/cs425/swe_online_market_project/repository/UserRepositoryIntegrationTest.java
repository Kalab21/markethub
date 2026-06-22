package edu.miu.cs.cs425.swe_online_market_project.repository;

import edu.miu.cs.cs425.swe_online_market_project.model.Role;
import edu.miu.cs.cs425.swe_online_market_project.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryIntegrationTest {

    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;

    private User savedUser(String username, String email) {
        User u = new User();
        u.setFirstName("Test");
        u.setLastName("User");
        u.setUserName(username);
        u.setEmail(email);
        u.setPassword("hashed");
        return userRepository.save(u);
    }

    @Test
    void findByUserName_returnsUser() {
        savedUser("alice", "alice@test.com");

        Optional<User> result = userRepository.findByUserName("alice");

        assertTrue(result.isPresent());
        assertEquals("alice", result.get().getUserName());
        assertEquals("alice@test.com", result.get().getEmail());
    }

    @Test
    void findByUserName_notFound_returnsEmpty() {
        Optional<User> result = userRepository.findByUserName("ghost");

        assertTrue(result.isEmpty());
    }

    @Test
    void existsByUserName_trueForExisting() {
        savedUser("bob", "bob@test.com");

        assertTrue(userRepository.existsByUserName("bob"));
    }

    @Test
    void existsByUserName_falseForMissing() {
        assertFalse(userRepository.existsByUserName("nobody"));
    }

    @Test
    void existsByEmail_trueForExisting() {
        savedUser("carol", "carol@test.com");

        assertTrue(userRepository.existsByEmail("carol@test.com"));
    }

    @Test
    void existsByEmail_falseForMissing() {
        assertFalse(userRepository.existsByEmail("missing@test.com"));
    }

    @Test
    void findUsersBySellerRole_returnsSellersOnly() {
        Role sellerRole = new Role(null, "ROLE_SELLER", null);
        sellerRole = roleRepository.save(sellerRole);

        User seller = savedUser("seller1", "seller1@test.com");
        seller.setRoles(new ArrayList<>(List.of(sellerRole)));
        userRepository.save(seller);

        savedUser("buyer1", "buyer1@test.com");

        List<User> sellers = userRepository.findUsersBySellerRole();

        assertEquals(1, sellers.size());
        assertEquals("seller1", sellers.get(0).getUserName());
    }

    @Test
    void findUsersBySellerRole_noSellers_returnsEmpty() {
        savedUser("buyer2", "buyer2@test.com");

        List<User> sellers = userRepository.findUsersBySellerRole();

        assertTrue(sellers.isEmpty());
    }
}
