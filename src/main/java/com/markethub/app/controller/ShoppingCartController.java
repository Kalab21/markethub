package com.markethub.app.controller;

import com.markethub.app.model.Order;
import com.markethub.app.model.Product;
import com.markethub.app.security.AccessGuard;
import com.markethub.app.model.ShoppingCart;
import com.markethub.app.model.User;
import com.markethub.app.service.OrderService;
import com.markethub.app.service.ProductService;
import com.markethub.app.service.ShoppingCartService;
import com.markethub.app.service.UserService;
import com.markethub.app.service.imp.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequestMapping(value = "/onlinemarket/cart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private UserService userService;

    @Autowired
    private AccessGuard accessGuard;

    /** The signed-in buyer's cart, created here (inside a mutation) if the account has none yet. */
    private ShoppingCart currentCart() {
        return userService.ensureCart(userDetailsServiceImpl.getCurrentUser().getUserId());
    }

    @PostMapping("/products/{productId}/add")
    public String addProductToCart(@PathVariable("productId") Long productId) {
        ShoppingCart cart = currentCart();
        Product product = productService.getProductById(productId);
        shoppingCartService.addProductToShoppingCart(cart.getCartId(), product);
        return "redirect:/onlinemarket/secured/services/products/list";
    }

    @GetMapping("/me")
    public String loadMyCart() {
        long uid = userDetailsServiceImpl.getCurrentUser().getUserId();
        return "redirect:/onlinemarket/cart/" + uid;
    }

    /** Read only: a buyer without a cart yet sees an empty one, and nothing is saved. */
    @GetMapping("/{buyerId:\\d+}")
    public String loadShoppingCartById(@PathVariable("buyerId") long buyerId, Model model) {
        accessGuard.requireSelfOrAdmin(buyerId);
        ShoppingCart cart = userService.getUserById(buyerId).getShoppingCart();
        if (cart == null) {
            cart = new ShoppingCart();
        }
        double total = cart.getProducts().stream().mapToDouble(p -> p.getPrice()).sum();
        model.addAttribute("shoppingCart", cart);
        model.addAttribute("cartTotal", total);
        model.addAttribute("currentUser", userDetailsServiceImpl.getCurrentUser());
        return "secured/services/buyer/cart/cartPage";
    }

    @PostMapping("/products/{productId}/remove")
    public String removeProductFromCart(@PathVariable("productId") Long productId) {
        ShoppingCart cart = currentCart();
        shoppingCartService.deleteProductFromCart(productId, cart.getCartId());
        return "redirect:/onlinemarket/cart/" + userDetailsServiceImpl.getCurrentUser().getUserId();
    }

    @PostMapping("/checkout")
    public String checkOutProductsFromCart() {
        User buyer = userDetailsServiceImpl.getCurrentUser();
        ShoppingCart cart = currentCart();
        if (cart.getProducts().isEmpty()) {
            return "redirect:/onlinemarket/cart/" + buyer.getUserId();
        }
        double cartPrice = cart.getProducts().stream().mapToDouble(Product::getPrice).sum();
        orderService.saveOrder(new Order("Pending", LocalDate.now(), cartPrice, buyer));
        shoppingCartService.deleteAllProductsFromCart(cart.getCartId());
        return "redirect:/orders/user/" + buyer.getUserId() + "?ordered";
    }
}
