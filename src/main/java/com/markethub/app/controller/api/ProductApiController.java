package com.markethub.app.controller.api;

import com.markethub.app.DTO.ProductResponse;
import com.markethub.app.model.Product;
import com.markethub.app.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product catalog API")
public class ProductApiController {

    private final ProductService productService;

    public ProductApiController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "List all products (paged)")
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(productService.getPagedProducts(page, size).map(ProductResponse::from));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable long id) {
        return ResponseEntity.ok(ProductResponse.from(productService.getProductById(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam @NotBlank String q) {
        return ResponseEntity.ok(productService.searchProducts(q).stream().map(ProductResponse::from).toList());
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Get products by seller ID")
    public ResponseEntity<List<ProductResponse>> getProductsBySeller(@PathVariable long sellerId) {
        return ResponseEntity.ok(productService.getAllProductsBySellerId(sellerId).stream().map(ProductResponse::from).toList());
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(productService.saveProduct(product)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product by ID")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable long id, @Valid @RequestBody Product product) {
        return ResponseEntity.ok(ProductResponse.from(productService.updateProduct(id, product)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product by ID")
    public ResponseEntity<Void> deleteProduct(@PathVariable long id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
