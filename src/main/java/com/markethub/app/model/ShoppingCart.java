package com.markethub.app.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ShoppingCart implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    @OneToOne(mappedBy = "shoppingCart")
    private User buyer;

    @OneToMany(fetch = javax.persistence.FetchType.EAGER)
    @JoinColumn(name= "carts_products")
    private List<Product> products;

}
