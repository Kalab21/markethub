package com.markethub.app.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @NotNull
    @NotBlank
    @Column(nullable = false)
    private String firstName;
    @NotNull
    @NotBlank
    @Column(nullable = false)
    private String lastName;
    @NotNull
    @NotBlank
    @Column(nullable = false)
    private String userName;
    @Email
    private String email;
    @JsonIgnore
    @NotNull
    @NotBlank
    @Column(nullable = false)
    private String password;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "users_roles",
            joinColumns = {@JoinColumn(name = "USER_ID",referencedColumnName = "userId")},
            inverseJoinColumns = {@JoinColumn(name = "ROLE_ID",referencedColumnName = "roleId")})
    private List<Role> roles;

    private boolean approvedSeller;

    @OneToMany(mappedBy = "user")
    private List<Address> address;

    @OneToMany(mappedBy = "user")
    List<Payment> payment;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "shopping_cart_Id")
    private ShoppingCart shoppingCart;

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
