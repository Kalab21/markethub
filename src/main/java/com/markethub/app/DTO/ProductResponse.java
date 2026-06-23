package com.markethub.app.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.markethub.app.model.Product;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product details")
public record ProductResponse(
        @Schema(description = "Unique product ID") @JsonProperty("product_id") long productId,
        @Schema(description = "Product name") String name,
        @Schema(description = "Price in USD", example = "49.99") double price,
        @Schema(description = "Product description") String description,
        @Schema(description = "Units in stock") int quantity,
        @Schema(description = "Stock-keeping unit", example = "SKU-001") String sku,
        @Schema(description = "Seller user ID") @JsonProperty("seller_id") Long sellerId
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getProductId(),
                p.getName(),
                p.getPrice(),
                p.getDescription(),
                p.getQuantity(),
                p.getSku(),
                p.getSeller() != null ? p.getSeller().getUserId() : null
        );
    }
}
