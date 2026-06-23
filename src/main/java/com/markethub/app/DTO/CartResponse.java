package com.markethub.app.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.markethub.app.model.ShoppingCart;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Shopping cart contents")
public record CartResponse(
        @Schema(description = "Unique cart ID") @JsonProperty("cart_id") Long cartId,
        @Schema(description = "ID of the buyer who owns this cart") @JsonProperty("buyer_user_id") Long buyerUserId,
        @Schema(description = "Products currently in the cart") List<ProductResponse> products
) {
    public static CartResponse from(ShoppingCart c) {
        List<ProductResponse> products = c.getProducts() != null
                ? c.getProducts().stream().map(ProductResponse::from).toList()
                : List.of();
        return new CartResponse(
                c.getCartId(),
                c.getBuyer() != null ? c.getBuyer().getUserId() : null,
                products
        );
    }
}
