package com.markethub.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markethub.app.controller.api.ProductApiController;
import com.markethub.app.exception.ResourceNotFoundException;
import com.markethub.app.model.Product;
import com.markethub.app.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductApiController.class)
class ProductApiControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ProductService productService;

    @Test
    @WithMockUser
    void getProductById_found_returns200() throws Exception {
        Product product = new Product();
        product.setProductId(1L);
        product.setName("Widget");
        product.setPrice(9.99);
        product.setSku("SKU-001");
        product.setDescription("A widget");
        product.setQuantity(10);

        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Widget"))
                .andExpect(jsonPath("$.sku").value("SKU-001"));
    }

    @Test
    @WithMockUser
    void getProductById_notFound_returns404() throws Exception {
        when(productService.getProductById(99L))
                .thenThrow(new ResourceNotFoundException("Product not found: 99"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found: 99"));
    }

    @Test
    @WithMockUser
    void searchProducts_returnsList() throws Exception {
        Product p = new Product();
        p.setName("Widget");
        when(productService.searchProducts("Widget")).thenReturn(List.of(p));

        mockMvc.perform(get("/api/products/search").param("q", "Widget"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Widget"));
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void createProduct_valid_returns201() throws Exception {
        Product product = new Product();
        product.setName("Gadget");
        product.setPrice(19.99);
        product.setSku("SKU-002");
        product.setDescription("A gadget");
        product.setQuantity(5);

        when(productService.saveProduct(any(Product.class))).thenReturn(product);

        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Gadget"));
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void deleteProduct_callsService_returns204() throws Exception {
        mockMvc.perform(delete("/api/products/1").with(csrf()))
                .andExpect(status().isNoContent());
    }
}
