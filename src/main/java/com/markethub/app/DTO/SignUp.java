package com.markethub.app.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "User registration request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUp {

    @Schema(description = "First name", example = "Jane")
    @JsonProperty("first_name")
    @NotEmpty
    @Size(min = 2)
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    @JsonProperty("last_name")
    @NotEmpty
    @Size(min = 2)
    private String lastName;

    @Schema(description = "Unique username", example = "janedoe")
    @JsonProperty("user_name")
    @NotEmpty
    @Size(min = 3)
    private String userName;

    @Schema(description = "Email address", example = "jane@example.com")
    @NotEmpty
    @Email
    private String email;

    @Schema(description = "Password (min 8 chars)", example = "s3cur3Pass!")
    @NotEmpty
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Schema(description = "Requested role: BUYER or SELLER", example = "BUYER")
    @NotEmpty
    private String role;
}

