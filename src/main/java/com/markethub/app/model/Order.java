package com.markethub.app.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name= "orders")
public class Order implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    @NotBlank
    private String orderStatus;
    @NotNull
    @DateTimeFormat(pattern= "yyyy-MM-dd")
    private LocalDate createdAt;
    @NotNull
    @DecimalMin("0.0")
    @Column(nullable= false)
    private double price;

    @ManyToOne
    private User owner;

    public Order(String orderStatus, LocalDate createdAt, double price, User owner) {
        this.orderStatus = orderStatus;
        this.createdAt = createdAt;
        this.price = price;
        this.owner = owner;
    }


}
