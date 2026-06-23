package com.markethub.app.model;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "products")

public class Product implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long productId;

    @NotNull
    @NotBlank
    @Column(length = 50, nullable = false)
    private String name;

    @Positive
    @NotNull
    @Column(nullable = false)
    private double price;

    @NotNull
    @NotBlank
    @Column(nullable = false)
    private String description;

    @PositiveOrZero
    @NotNull
    @Column(nullable = false)
    private int quantity;

    @NotNull
    @Column(nullable = false, unique = true)
    private String sku;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Review> reviews;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;



}
