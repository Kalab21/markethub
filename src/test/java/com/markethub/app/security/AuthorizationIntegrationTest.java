package com.markethub.app.security;

import com.markethub.app.model.Order;
import com.markethub.app.model.Product;
import com.markethub.app.model.Role;
import com.markethub.app.model.ShoppingCart;
import com.markethub.app.model.User;
import com.markethub.app.repository.OrderRepository;
import com.markethub.app.repository.ProductRepository;
import com.markethub.app.repository.RoleRepository;
import com.markethub.app.repository.ShoppingCartRepository;
import com.markethub.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Role gates, seller approval and record ownership, exercised through the real security filter chain
 * against the in-memory database. Every "negative" test also asserts that nothing was changed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.transaction.annotation.Transactional
class AuthorizationIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired ProductRepository products;
    @Autowired OrderRepository orders;
    @Autowired ShoppingCartRepository carts;
    @Autowired PasswordEncoder encoder;

    User admin, approvedSeller, otherSeller, pendingSeller, buyer, otherBuyer;
    Product ownedProduct;
    Order buyerOrder;

    @BeforeEach
    void data() {
        String id = UUID.randomUUID().toString().substring(0, 8);
        admin = account("adm-" + id, "ROLE_ADMIN", false, false);
        approvedSeller = account("sel-" + id, "ROLE_SELLER", true, false);
        otherSeller = account("sel2-" + id, "ROLE_SELLER", true, false);
        pendingSeller = account("pend-" + id, "ROLE_SELLER", false, false);
        buyer = account("buy-" + id, "ROLE_BUYER", false, true);
        otherBuyer = account("buy2-" + id, "ROLE_BUYER", false, true);
        ownedProduct = product("SKU-" + id, approvedSeller);
        buyerOrder = orders.save(new Order("Pending", LocalDate.now(), 10.0, buyer));
    }

    // ---- helpers ---------------------------------------------------------------------------

    private User account(String name, String role, boolean approved, boolean cart) {
        Role r = roles.findByRoleType(role).orElseGet(() -> {
            Role created = new Role();
            created.setRoleType(role);
            return roles.save(created);
        });
        User u = new User();
        u.setFirstName("T");
        u.setLastName("User");
        u.setUserName(name);
        u.setEmail(name + "@test.local");
        u.setPassword(encoder.encode("password"));
        u.setApprovedSeller(approved);
        u.setRoles(new java.util.ArrayList<>(List.of(r)));
        if (cart) {
            u.setShoppingCart(carts.save(new ShoppingCart()));
        }
        return users.save(u);
    }

    private Product product(String sku, User seller) {
        Product p = new Product();
        p.setName("Item " + sku);
        p.setPrice(5.0);
        p.setDescription("desc");
        p.setQuantity(3);
        p.setSku(sku);
        p.setSeller(seller);
        return products.save(p);
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor as(User u) {
        String role = u.getRoles().get(0).getRoleType().replace("ROLE_", "");
        return user(u.getUserName()).roles(role);
    }

    private String productJson(String sku) {
        return "{\"name\":\"New\",\"price\":4.5,\"description\":\"d\",\"quantity\":2,\"sku\":\"" + sku + "\"}";
    }

    // ---- unauthenticated and role gates ----------------------------------------------------

    @Test
    void anonymousApiCallIsNotServed() throws Exception {
        mvc.perform(get("/api/orders/" + buyerOrder.getOrderId()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void buyerCannotCreateProductsThroughApi() throws Exception {
        long before = products.count();
        mvc.perform(post("/api/products").with(as(buyer)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(productJson("B-" + UUID.randomUUID())))
                .andExpect(status().isForbidden());
        assertEquals(before, products.count());
    }

    @Test
    void sellerCannotReachAdminUserManagement() throws Exception {
        mvc.perform(get("/onlinemarket/secured/services/users/users").with(as(approvedSeller)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/onlinemarket/secured/services/users/sellers").with(as(buyer)))
                .andExpect(status().isForbidden());
    }

    @Test
    void sellerCannotApproveThemselves() throws Exception {
        mvc.perform(post("/onlinemarket/secured/services/users/" + pendingSeller.getUserId() + "/approve")
                        .with(as(pendingSeller)).with(csrf()))
                .andExpect(status().isForbidden());
        assertTrue(!users.findById(pendingSeller.getUserId()).orElseThrow().isApprovedSeller());
    }

    @Test
    void adminCanApproveSeller() throws Exception {
        mvc.perform(post("/onlinemarket/secured/services/users/" + pendingSeller.getUserId() + "/approve")
                        .with(as(admin)).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertTrue(users.findById(pendingSeller.getUserId()).orElseThrow().isApprovedSeller());
    }

    @Test
    void buyerCannotUseAdminOnlyAreas() throws Exception {
        mvc.perform(get("/addresses/addresses").with(as(buyer))).andExpect(status().isForbidden());
        mvc.perform(get("/api/orders").with(as(buyer))).andExpect(status().isForbidden());
    }

    // ---- seller approval -------------------------------------------------------------------

    @Test
    void unapprovedSellerCannotCreateProductThroughApi() throws Exception {
        long before = products.count();
        mvc.perform(post("/api/products").with(as(pendingSeller)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(productJson("P-" + UUID.randomUUID())))
                .andExpect(status().isForbidden());
        assertEquals(before, products.count());
    }

    @Test
    void unapprovedSellerCannotUseProductForms() throws Exception {
        mvc.perform(get("/onlinemarket/secured/services/products/new-product").with(as(pendingSeller)))
                .andExpect(status().isForbidden());
    }

    @Test
    void approvedSellerCreatesProductOwnedByThemselves() throws Exception {
        String sku = "OK-" + UUID.randomUUID();
        // The body tries to assign the product to another seller; the server ignores that.
        String body = "{\"name\":\"New\",\"price\":4.5,\"description\":\"d\",\"quantity\":2,\"sku\":\"" + sku
                + "\",\"seller\":{\"userId\":" + otherSeller.getUserId() + "}}";
        mvc.perform(post("/api/products").with(as(approvedSeller)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        Product saved = products.findAll().stream().filter(p -> sku.equals(p.getSku())).findFirst().orElseThrow();
        assertEquals(approvedSeller.getUserId(), saved.getSeller().getUserId());
    }

    // ---- product ownership -----------------------------------------------------------------

    @Test
    void sellerCannotUpdateAnotherSellersProduct() throws Exception {
        mvc.perform(put("/api/products/" + ownedProduct.getProductId()).with(as(otherSeller)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(productJson(ownedProduct.getSku())))
                .andExpect(status().isForbidden());
        assertEquals("Item " + ownedProduct.getSku(),
                products.findById(ownedProduct.getProductId()).orElseThrow().getName());
    }

    @Test
    void sellerCannotDeleteAnotherSellersProduct() throws Exception {
        mvc.perform(delete("/api/products/" + ownedProduct.getProductId()).with(as(otherSeller)).with(csrf()))
                .andExpect(status().isForbidden());
        assertTrue(products.existsById(ownedProduct.getProductId()));
    }

    @Test
    void sellerCannotOpenAnotherSellersEditForm() throws Exception {
        mvc.perform(get("/onlinemarket/secured/services/products/update-product/" + ownedProduct.getProductId())
                        .with(as(otherSeller)))
                .andExpect(status().isForbidden());
    }

    @Test
    void sellerCannotOverwriteAnotherSellersProductThroughTheForm() throws Exception {
        mvc.perform(post("/onlinemarket/secured/services/products/save-product").with(as(otherSeller)).with(csrf())
                        .param("productId", String.valueOf(ownedProduct.getProductId()))
                        .param("name", "Hijacked").param("price", "1").param("description", "x")
                        .param("quantity", "1").param("sku", ownedProduct.getSku()))
                .andExpect(status().isForbidden());
        assertEquals("Item " + ownedProduct.getSku(),
                products.findById(ownedProduct.getProductId()).orElseThrow().getName());
    }

    @Test
    void sellerCannotListAnotherSellersProducts() throws Exception {
        mvc.perform(get("/onlinemarket/secured/services/products/my-products/" + approvedSeller.getUserId())
                        .with(as(otherSeller)))
                .andExpect(status().isForbidden());
    }

    @Test
    void ownerAndAdminMayUpdateAndDeleteProduct() throws Exception {
        mvc.perform(put("/api/products/" + ownedProduct.getProductId()).with(as(approvedSeller)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(productJson(ownedProduct.getSku())))
                .andExpect(status().isOk());
        Product after = products.findById(ownedProduct.getProductId()).orElseThrow();
        assertEquals("New", after.getName());
        assertEquals(approvedSeller.getUserId(), after.getSeller().getUserId());

        mvc.perform(delete("/api/products/" + ownedProduct.getProductId()).with(as(admin)).with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(!products.existsById(ownedProduct.getProductId()));
    }

    // ---- orders ----------------------------------------------------------------------------

    @Test
    void buyerCannotReadAnotherBuyersOrder() throws Exception {
        mvc.perform(get("/api/orders/" + buyerOrder.getOrderId()).with(as(otherBuyer)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/orders/user/" + buyer.getUserId()).with(as(otherBuyer)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/orders/user/" + buyer.getUserId()).with(as(otherBuyer)))
                .andExpect(status().isForbidden());
    }

    @Test
    void buyerCannotCancelOrDeleteAnotherBuyersOrder() throws Exception {
        mvc.perform(post("/orders/" + buyerOrder.getOrderId() + "/cancel").with(as(otherBuyer)).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/orders/" + buyerOrder.getOrderId() + "/delete").with(as(otherBuyer)).with(csrf()))
                .andExpect(status().isForbidden());
        Order unchanged = orders.findById(buyerOrder.getOrderId()).orElseThrow();
        assertEquals("Pending", unchanged.getOrderStatus());
    }

    @Test
    void ownerAndAdminCanReadOrder() throws Exception {
        mvc.perform(get("/api/orders/" + buyerOrder.getOrderId()).with(as(buyer))).andExpect(status().isOk());
        mvc.perform(get("/api/orders/" + buyerOrder.getOrderId()).with(as(admin))).andExpect(status().isOk());
    }

    @Test
    void ownerCanCancelOwnOrder() throws Exception {
        mvc.perform(post("/orders/" + buyerOrder.getOrderId() + "/cancel").with(as(buyer)).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertEquals("Cancelled", orders.findById(buyerOrder.getOrderId()).orElseThrow().getOrderStatus());
    }

    @Test
    void orderCreatedThroughApiBelongsToTheCaller() throws Exception {
        String body = "{\"orderStatus\":\"Pending\",\"createdAt\":\"2026-01-01\",\"price\":3.0,"
                + "\"owner\":{\"userId\":" + otherBuyer.getUserId() + "}}";
        mvc.perform(post("/api/orders").with(as(buyer)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        long forBuyer = orders.findAll().stream()
                .filter(o -> o.getOwner() != null && o.getOwner().getUserId().equals(buyer.getUserId())).count();
        long forVictim = orders.findAll().stream()
                .filter(o -> o.getOwner() != null && o.getOwner().getUserId().equals(otherBuyer.getUserId())).count();
        assertEquals(2, forBuyer);
        assertEquals(0, forVictim);
    }

    // ---- carts -----------------------------------------------------------------------------

    @Test
    void buyerCannotTouchAnotherBuyersCart() throws Exception {
        Long victimCart = otherBuyer.getShoppingCart().getCartId();
        mvc.perform(get("/api/cart/buyer/" + otherBuyer.getUserId()).with(as(buyer)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/cart/" + victimCart + "/products/" + ownedProduct.getProductId())
                        .with(as(buyer)).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/cart/" + victimCart + "/products").with(as(buyer)).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/onlinemarket/cart/" + otherBuyer.getUserId()).with(as(buyer)))
                .andExpect(status().isForbidden());
    }

    @Test
    void buyerCanReadOwnCart() throws Exception {
        mvc.perform(get("/api/cart/buyer/" + buyer.getUserId()).with(as(buyer)))
                .andExpect(status().isOk());
    }

    @Test
    void sellerCannotUseCarts() throws Exception {
        mvc.perform(get("/api/cart/buyer/" + buyer.getUserId()).with(as(approvedSeller)))
                .andExpect(status().isForbidden());
    }

    // ---- HTTP methods and CSRF ---------------------------------------------------------------

    private int cartSize(User u) {
        return carts.findById(u.getShoppingCart().getCartId()).orElseThrow().getProducts().size();
    }

    private void putInCart(User u, Product p) {
        ShoppingCart c = carts.findById(u.getShoppingCart().getCartId()).orElseThrow();
        c.getProducts().add(p);
        carts.save(c);
    }

    private long ordersOf(User u) {
        return orders.findAll().stream().filter(o -> o.getOwner().getUserId().equals(u.getUserId())).count();
    }

    @Test
    void formerGetMutationUrlsNoLongerChangeState() throws Exception {
        putInCart(buyer, ownedProduct);
        Long cartId = buyer.getShoppingCart().getCartId();
        long ordersBefore = orders.count();
        long productsBefore = products.count();
        String[] urls = {
                "/orders/cancel/" + buyerOrder.getOrderId() + "/user/" + buyer.getUserId(),
                "/orders/delete/" + buyerOrder.getOrderId() + "/user/" + buyer.getUserId(),
                "/orders/" + buyerOrder.getOrderId() + "/cancel",
                "/orders/" + buyerOrder.getOrderId() + "/delete",
                "/onlinemarket/cart/addproduct/" + ownedProduct.getProductId(),
                "/onlinemarket/cart/" + cartId + "/addproduct/" + ownedProduct.getProductId(),
                "/onlinemarket/cart/" + cartId + "/delete/" + ownedProduct.getProductId(),
                "/onlinemarket/cart/" + cartId + "/checkout/" + buyer.getUserId(),
                "/onlinemarket/cart/checkout",
                "/onlinemarket/cart/products/" + ownedProduct.getProductId() + "/remove",
        };
        for (String url : urls) {
            mvc.perform(get(url).with(as(buyer))).andExpect(status().is4xxClientError());
        }
        String[] sellerUrls = {
                "/onlinemarket/secured/services/products/delete/" + ownedProduct.getProductId(),
                "/onlinemarket/secured/services/products/" + ownedProduct.getProductId() + "/delete",
        };
        for (String url : sellerUrls) {
            mvc.perform(get(url).with(as(approvedSeller))).andExpect(status().is4xxClientError());
        }
        String[] adminUrls = {
                "/onlinemarket/secured/services/users/update/" + pendingSeller.getUserId(),
                "/onlinemarket/secured/services/users/" + pendingSeller.getUserId() + "/approve",
        };
        for (String url : adminUrls) {
            mvc.perform(get(url).with(as(admin))).andExpect(status().is4xxClientError());
        }
        assertEquals("Pending", orders.findById(buyerOrder.getOrderId()).orElseThrow().getOrderStatus());
        assertEquals(ordersBefore, orders.count());
        assertEquals(productsBefore, products.count());
        assertEquals(1, cartSize(buyer));
        assertTrue(!users.findById(pendingSeller.getUserId()).orElseThrow().isApprovedSeller());
    }

    @Test
    void getLogoutDoesNotEndTheSession() throws Exception {
        mvc.perform(get("/onlinemarket/public/logout").with(as(buyer))).andExpect(status().is4xxClientError());
    }

    @Test
    void mutationsWithoutCsrfTokenAreRejected() throws Exception {
        putInCart(buyer, ownedProduct);
        String pid = String.valueOf(ownedProduct.getProductId());
        mvc.perform(post("/orders/" + buyerOrder.getOrderId() + "/cancel").with(as(buyer)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/orders/" + buyerOrder.getOrderId() + "/delete").with(as(buyer)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/onlinemarket/cart/products/" + pid + "/add").with(as(buyer)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/onlinemarket/cart/products/" + pid + "/remove").with(as(buyer)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/onlinemarket/cart/checkout").with(as(buyer)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/onlinemarket/secured/services/products/" + pid + "/delete").with(as(approvedSeller)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/onlinemarket/secured/services/users/" + pendingSeller.getUserId() + "/approve")
                        .with(as(admin)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/onlinemarket/public/logout").with(as(buyer)))
                .andExpect(status().isForbidden());
        assertEquals("Pending", orders.findById(buyerOrder.getOrderId()).orElseThrow().getOrderStatus());
        assertEquals(1, cartSize(buyer));
        assertTrue(products.existsById(ownedProduct.getProductId()));
        assertTrue(!users.findById(pendingSeller.getUserId()).orElseThrow().isApprovedSeller());
    }

    @Test
    void buyerAddsAndRemovesOnlyInOwnCart() throws Exception {
        String pid = String.valueOf(ownedProduct.getProductId());
        mvc.perform(post("/onlinemarket/cart/products/" + pid + "/add").with(as(buyer)).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertEquals(1, cartSize(buyer));
        assertEquals(0, cartSize(otherBuyer));
        mvc.perform(post("/onlinemarket/cart/products/" + pid + "/remove").with(as(buyer)).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertEquals(0, cartSize(buyer));
    }

    @Test
    void sellerCannotUseBuyerCartActions() throws Exception {
        String pid = String.valueOf(ownedProduct.getProductId());
        mvc.perform(post("/onlinemarket/cart/products/" + pid + "/add").with(as(approvedSeller)).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/onlinemarket/cart/checkout").with(as(approvedSeller)).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void checkoutOrdersBelongToTheCallerAndEmptyTheirCart() throws Exception {
        putInCart(buyer, ownedProduct);
        putInCart(otherBuyer, ownedProduct);
        long victimBefore = ordersOf(otherBuyer);
        mvc.perform(post("/onlinemarket/cart/checkout").with(as(buyer)).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertEquals(0, cartSize(buyer));
        assertEquals(1, cartSize(otherBuyer));
        assertEquals(2, ordersOf(buyer));
        assertEquals(victimBefore, ordersOf(otherBuyer));
    }

    @Test
    void checkoutIgnoresIdentityInTheRequest() throws Exception {
        putInCart(buyer, ownedProduct);
        mvc.perform(post("/onlinemarket/cart/checkout").with(as(buyer)).with(csrf())
                        .param("userId", String.valueOf(otherBuyer.getUserId()))
                        .param("cartId", String.valueOf(otherBuyer.getShoppingCart().getCartId())))
                .andExpect(status().is3xxRedirection());
        assertEquals(0, ordersOf(otherBuyer));
        assertEquals(2, ordersOf(buyer));
    }

    @Test
    void ownerCanDeleteOwnOrderWithCsrf() throws Exception {
        mvc.perform(post("/orders/" + buyerOrder.getOrderId() + "/delete").with(as(buyer)).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertTrue(!orders.existsById(buyerOrder.getOrderId()));
    }

    @Test
    void sellerDeleteFormRespectsApprovalAndOwnership() throws Exception {
        String url = "/onlinemarket/secured/services/products/" + ownedProduct.getProductId() + "/delete";
        mvc.perform(post(url).with(as(otherSeller)).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post(url).with(as(pendingSeller)).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post(url).with(as(buyer)).with(csrf())).andExpect(status().isForbidden());
        assertTrue(products.existsById(ownedProduct.getProductId()));
        mvc.perform(post(url).with(as(approvedSeller)).with(csrf())).andExpect(status().is3xxRedirection());
        assertTrue(!products.existsById(ownedProduct.getProductId()));
    }

    @Test
    void adminDeletesAnySellersProductThroughTheApi() throws Exception {
        mvc.perform(delete("/api/products/" + ownedProduct.getProductId()).with(as(admin)))
                .andExpect(status().isForbidden()); // no CSRF token
        assertTrue(products.existsById(ownedProduct.getProductId()));
        mvc.perform(delete("/api/products/" + ownedProduct.getProductId()).with(as(admin)).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void approvingASellerNeedsAdminAndCsrf() throws Exception {
        String url = "/onlinemarket/secured/services/users/" + pendingSeller.getUserId() + "/approve";
        mvc.perform(post(url).with(as(buyer)).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post(url).with(as(approvedSeller)).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post(url).with(as(admin))).andExpect(status().isForbidden());
        assertTrue(!users.findById(pendingSeller.getUserId()).orElseThrow().isApprovedSeller());
    }

    @Test
    void logoutWorksWithPostAndCsrf() throws Exception {
        mvc.perform(post("/onlinemarket/public/logout").with(as(buyer)).with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    // ---- GET stays read-only -----------------------------------------------------------------

    @Test
    void viewingACartDoesNotCreateOne() throws Exception {
        User cartless = account("nocart-" + UUID.randomUUID().toString().substring(0, 6), "ROLE_BUYER", false, false);
        long cartsBefore = carts.count();
        mvc.perform(get("/onlinemarket/cart/" + cartless.getUserId()).with(as(cartless)))
                .andExpect(status().isOk());
        assertEquals(cartsBefore, carts.count());
        assertTrue(users.findById(cartless.getUserId()).orElseThrow().getShoppingCart() == null);
    }

    @Test
    void addingToCartCreatesMissingCartForTheCaller() throws Exception {
        User cartless = account("nocart2-" + UUID.randomUUID().toString().substring(0, 6), "ROLE_BUYER", false, false);
        mvc.perform(post("/onlinemarket/cart/products/" + ownedProduct.getProductId() + "/add")
                        .with(as(cartless)).with(csrf()))
                .andExpect(status().is3xxRedirection());
        User reloaded = users.findById(cartless.getUserId()).orElseThrow();
        assertEquals(1, reloaded.getShoppingCart().getProducts().size());
    }

    @Test
    void readPagesStillRenderForTheirRoles() throws Exception {
        mvc.perform(get("/onlinemarket/secured/services/products/list").with(as(buyer))).andExpect(status().isOk());
        mvc.perform(get("/orders/user/" + buyer.getUserId()).with(as(buyer))).andExpect(status().isOk());
        mvc.perform(get("/onlinemarket/cart/" + buyer.getUserId()).with(as(buyer))).andExpect(status().isOk());
        mvc.perform(get("/onlinemarket/secured/services/products/my-products/" + approvedSeller.getUserId())
                        .with(as(approvedSeller))).andExpect(status().isOk());
        mvc.perform(get("/onlinemarket/secured/services/users/sellers").with(as(admin))).andExpect(status().isOk());
    }
}
