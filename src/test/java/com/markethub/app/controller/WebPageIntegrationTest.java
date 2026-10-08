package com.markethub.app.controller;

import com.markethub.app.model.Product;
import com.markethub.app.model.Role;
import com.markethub.app.model.ShoppingCart;
import com.markethub.app.model.User;
import com.markethub.app.repository.ProductRepository;
import com.markethub.app.repository.RoleRepository;
import com.markethub.app.repository.ShoppingCartRepository;
import com.markethub.app.repository.UserRepository;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Server-rendered pages: the product detail page, the signed-in navbar, the error pages, and
 * routes that were removed because they pointed at templates that never existed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WebPageIntegrationTest {

    private static final String PRODUCTS = "/onlinemarket/secured/services/products";

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired ProductRepository products;
    @Autowired ShoppingCartRepository carts;

    User buyer, seller, admin;
    Product product;

    @BeforeEach
    void data() {
        String id = UUID.randomUUID().toString().substring(0, 8);
        buyer = account("buyer-" + id, "ROLE_BUYER");
        seller = account("seller-" + id, "ROLE_SELLER");
        admin = account("admin-" + id, "ROLE_ADMIN");
        Product p = new Product();
        p.setName("Walnut Desk " + id);
        p.setSku("DESK-" + id);
        p.setDescription("Solid walnut writing desk");
        p.setPrice(249.5);
        p.setQuantity(4);
        p.setSeller(seller);
        product = products.save(p);
    }

    private User account(String name, String role) {
        Role r = roles.findByRoleType(role).orElseGet(() -> {
            Role created = new Role();
            created.setRoleType(role);
            return roles.save(created);
        });
        User u = new User();
        u.setFirstName("Sam");
        u.setLastName("Tester");
        u.setUserName(name);
        u.setEmail(name + "@test.local");
        u.setPassword("x");
        u.setApprovedSeller(role.equals("ROLE_SELLER"));
        u.setRoles(new ArrayList<>(List.of(r)));
        if (role.equals("ROLE_BUYER")) {
            u.setShoppingCart(carts.save(new ShoppingCart()));
        }
        return users.save(u);
    }

    private static RequestPostProcessor as(User u) {
        return user(u.getUserName()).roles(u.getRoles().get(0).getRoleType().replace("ROLE_", ""));
    }

    // ---- product detail page ---------------------------------------------------------------

    @Test
    void productListLinksToProductDetailPage() throws Exception {
        mvc.perform(get(PRODUCTS + "/list").param("size", "1000").with(as(buyer)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(PRODUCTS + "/" + product.getProductId() + "\"")));
    }

    @Test
    void productDetailPageRendersReadOnlyDetailsForBuyer() throws Exception {
        mvc.perform(get(PRODUCTS + "/" + product.getProductId()).with(as(buyer)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(product.getName())))
                .andExpect(content().string(containsString("SKU: " + product.getSku())))
                .andExpect(content().string(containsString("$249.50")))
                .andExpect(content().string(containsString("Sam Tester")))
                .andExpect(content().string(containsString("Add to Cart")));
    }

    @Test
    void productDetailPageHasNoCartButtonForSeller() throws Exception {
        mvc.perform(get(PRODUCTS + "/" + product.getProductId()).with(as(seller)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(product.getName())))
                .andExpect(content().string(not(containsString("Add to Cart"))));
    }

    @Test
    void unknownProductIdIsNotFound() throws Exception {
        mvc.perform(get(PRODUCTS + "/999999999").with(as(buyer)))
                .andExpect(status().isNotFound());
    }

    // ---- navbar ----------------------------------------------------------------------------

    @Test
    void navbarShowsSignedInUsername() throws Exception {
        mvc.perform(get("/onlinemarket/secured/services").with(as(buyer)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<span>" + buyer.getUserName() + "</span>")));
    }

    // ---- removed routes (their templates never existed) ------------------------------------

    @Test
    void removedProductSearchPageIsNotFound() throws Exception {
        mvc.perform(get(PRODUCTS + "/search").param("searchString", "desk").with(as(buyer)))
                .andExpect(status().isNotFound());
    }

    @Test
    void removedAdminUserCrudPageIsNotFound() throws Exception {
        mvc.perform(get("/onlinemarket/secured/services/users/users").with(as(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void removedPublicStubPagesAreNotFound() throws Exception {
        mvc.perform(get("/onlinemarket/public/about")).andExpect(status().isNotFound());
        mvc.perform(get("/onlinemarket/public/virtualtour")).andExpect(status().isNotFound());
    }

    // ---- error pages -----------------------------------------------------------------------

    @Test
    void errorPageOffersLoginWhenAnonymous() throws Exception {
        mvc.perform(get("/error").accept(MediaType.TEXT_HTML)
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
                        .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/missing"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Go to Login")))
                .andExpect(content().string(not(containsString("Go to Dashboard"))));
    }

    @Test
    void errorPageOffersDashboardWhenSignedIn() throws Exception {
        mvc.perform(get("/error").accept(MediaType.TEXT_HTML).with(as(buyer))
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500)
                        .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/broken"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Go to Dashboard")))
                .andExpect(content().string(not(containsString("Go to Login"))));
    }
}
