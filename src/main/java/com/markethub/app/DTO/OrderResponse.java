package com.markethub.app.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.markethub.app.model.Order;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Order details")
public record OrderResponse(
        @Schema(description = "Unique order ID") @JsonProperty("order_id") Long orderId,
        @Schema(description = "Order status, e.g. PENDING, SHIPPED, DELIVERED") @JsonProperty("order_status") String orderStatus,
        @Schema(description = "Date order was created") @JsonProperty("created_at") LocalDate createdAt,
        @Schema(description = "Total order value in USD", example = "99.99") double price,
        @Schema(description = "ID of the buyer who placed the order") @JsonProperty("owner_user_id") Long ownerUserId
) {
    public static OrderResponse from(Order o) {
        return new OrderResponse(
                o.getOrderId(),
                o.getOrderStatus(),
                o.getCreatedAt(),
                o.getPrice(),
                o.getOwner() != null ? o.getOwner().getUserId() : null
        );
    }
}
