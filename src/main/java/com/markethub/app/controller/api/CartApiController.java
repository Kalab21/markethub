package com.markethub.app.controller.api;

import com.markethub.app.DTO.CartResponse;
import com.markethub.app.model.Product;
import com.markethub.app.model.ShoppingCart;
import com.markethub.app.service.ProductService;
import com.markethub.app.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/cart")
@Tag(name = "Shopping Cart", description = "Cart management API")
public class CartApiController {

    private final ShoppingCartService cartService;
    private final ProductService productService;

    public CartApiController(ShoppingCartService cartService, ProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    @GetMapping("/buyer/{userId}")
    @Operation(summary = "Get cart by buyer user ID")
    public ResponseEntity<CartResponse> getCartByBuyer(@PathVariable Long userId) {
        return ResponseEntity.ok(CartResponse.from(cartService.getShoppingCartByBuyer(userId)));
    }

    @PostMapping("/{cartId}/products/{productId}")
    @Operation(summary = "Add product to cart")
    public ResponseEntity<CartResponse> addProduct(@PathVariable Long cartId, @PathVariable long productId) {
        Product product = productService.getProductById(productId);
        return ResponseEntity.ok(CartResponse.from(cartService.addProductToShoppingCart(cartId, product)));
    }

    @DeleteMapping("/{cartId}/products/{productId}")
    @Operation(summary = "Remove product from cart")
    public ResponseEntity<Void> removeProduct(@PathVariable Long cartId, @PathVariable Long productId) {
        cartService.deleteProductFromCart(productId, cartId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{cartId}/products")
    @Operation(summary = "Clear all products from cart")
    public ResponseEntity<Void> clearCart(@PathVariable Long cartId) {
        cartService.deleteAllProductsFromCart(cartId);
        return ResponseEntity.noContent().build();
    }
}
