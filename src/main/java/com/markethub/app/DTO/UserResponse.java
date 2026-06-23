package com.markethub.app.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.markethub.app.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "User details")
public record UserResponse(
        @Schema(description = "Unique user ID") @JsonProperty("user_id") Long userId,
        @Schema(description = "First name") @JsonProperty("first_name") String firstName,
        @Schema(description = "Last name") @JsonProperty("last_name") String lastName,
        @Schema(description = "Username") @JsonProperty("user_name") String userName,
        @Schema(description = "Email address") String email,
        @Schema(description = "Whether the seller account is approved") @JsonProperty("approved_seller") boolean approvedSeller,
        @Schema(description = "Assigned roles") List<String> roles
) {
    public static UserResponse from(User u) {
        List<String> roleNames = u.getRoles() != null
                ? u.getRoles().stream().map(r -> r.getRoleType()).toList()
                : List.of();
        return new UserResponse(
                u.getUserId(),
                u.getFirstName(),
                u.getLastName(),
                u.getUserName(),
                u.getEmail(),
                u.isApprovedSeller(),
                roleNames
        );
    }
}
