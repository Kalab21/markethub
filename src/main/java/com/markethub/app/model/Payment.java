package com.markethub.app.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.markethub.app.constant.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="payments")
public class Payment implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long paymentId;

    @NotNull
    @NotBlank(message = "Required")
    @Column(nullable = false)
    private String cardName;

    @NotNull(message = "Required")
    @Column(nullable = false)
    private PaymentType type;

    @NotNull
    @NotBlank(message = "Required")
    @Column(nullable = false, unique = true)
    private String cardNumber;

    @NotNull(message = "Required")
    @Column(nullable = false)
    private int csv;

    @Column(name = "is_default")
    private boolean isDefault;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
   @JsonBackReference
    private User user;

}
