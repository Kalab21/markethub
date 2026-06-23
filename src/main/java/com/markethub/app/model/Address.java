package com.markethub.app.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "addresses")
public class Address implements Serializable{
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long Id;

        @NotBlank
        private String street;

        @NotBlank
        private String city;

        @NotBlank
        private String state;

        @NotNull
        @Size(min=5, max=5, message="zipcode should have size of five number.")
        private String zipCode;

        private AddressType addressType;

        @NotNull
        @Size(min=10, max=10, message="phone number have size of 10.")
        private long phoneNumber;

        @JsonIgnore
        @ManyToOne
        @JoinColumn(name = "user_Id")
        private User user;
    }

