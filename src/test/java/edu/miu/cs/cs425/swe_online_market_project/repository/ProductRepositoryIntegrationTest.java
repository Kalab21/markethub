package edu.miu.cs.cs425.swe_online_market_project.repository;

import edu.miu.cs.cs425.swe_online_market_project.model.Product;
import edu.miu.cs.cs425.swe_online_market_project.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductRepositoryIntegrationTest {

    @Autowired ProductRepository productRepository;
    @Autowired UserRepository userRepository;

    private User seller;

    @BeforeEach
    void setUp() {
        User u = new User();
        u.setFirstName("Test");
        u.setLastName("Seller");
        u.setUserName("testseller");
        u.setPassword("hashed");
        seller = userRepository.save(u);
    }

    private Product saved(String name, String sku, int qty) {
        Product p = new Product();
        p.setName(name);
        p.setSku(sku);
        p.setPrice(9.99);
        p.setDescription("description");
        p.setQuantity(qty);
        p.setSeller(seller);
        return productRepository.save(p);
    }

    @Test
    void findBySellerId_returnsOnlySellerProducts() {
        saved("Widget", "SKU-001", 5);
        saved("Gadget", "SKU-002", 3);

        User other = new User();
        other.setFirstName("Other");
        other.setLastName("Seller");
        other.setUserName("otherseller");
        other.setPassword("hashed");
        other = userRepository.save(other);

        Product p = new Product();
        p.setName("Foreign");
        p.setSku("SKU-003");
        p.setPrice(1.0);
        p.setDescription("desc");
        p.setQuantity(1);
        p.setSeller(other);
        productRepository.save(p);

        List<Product> results = productRepository.findBySellerId(seller.getUserId());

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(r -> r.getSeller().getUserId().equals(seller.getUserId())));
    }

    @Test
    void findNotOutOfStockProducts_excludesZeroQuantity() {
        saved("InStock", "SKU-004", 5);
        saved("OutOfStock", "SKU-005", 0);

        List<Product> results = productRepository.findNotOutOfStockProducts();

        assertTrue(results.stream().allMatch(p -> p.getQuantity() > 0));
        assertTrue(results.stream().anyMatch(p -> p.getName().equals("InStock")));
        assertTrue(results.stream().noneMatch(p -> p.getName().equals("OutOfStock")));
    }

    @Test
    void findAllByProductName_returnsExactMatch() {
        saved("Laptop", "SKU-006", 2);
        saved("Laptop Stand", "SKU-007", 4);

        List<Product> results = productRepository.findAllByProductName("Laptop");

        assertEquals(1, results.size());
        assertEquals("Laptop", results.get(0).getName());
    }

    @Test
    void findAllByProductName_noMatch_returnsEmpty() {
        saved("Keyboard", "SKU-008", 3);

        List<Product> results = productRepository.findAllByProductName("Monitor");

        assertTrue(results.isEmpty());
    }

    @Test
    void findAllPaged_returnsCorrectPageAndTotal() {
        saved("Alpha", "SKU-009", 1);
        saved("Beta", "SKU-010", 1);
        saved("Gamma", "SKU-011", 1);
        saved("Delta", "SKU-012", 1);
        saved("Epsilon", "SKU-013", 1);

        Page<Product> firstPage = productRepository.findAll(PageRequest.of(0, 2, Sort.by("name")));

        assertEquals(2, firstPage.getContent().size());
        assertEquals(5, firstPage.getTotalElements());
        assertEquals(3, firstPage.getTotalPages());
        assertEquals("Alpha", firstPage.getContent().get(0).getName());
        assertEquals("Beta", firstPage.getContent().get(1).getName());
    }

    @Test
    void findAllPaged_lastPage_returnsRemainder() {
        saved("A", "SKU-014", 1);
        saved("B", "SKU-015", 1);
        saved("C", "SKU-016", 1);

        Page<Product> lastPage = productRepository.findAll(PageRequest.of(1, 2, Sort.by("name")));

        assertEquals(1, lastPage.getContent().size());
        assertEquals("C", lastPage.getContent().get(0).getName());
    }
}
