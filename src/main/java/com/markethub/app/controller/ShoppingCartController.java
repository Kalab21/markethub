package com.markethub.app.controller;

import com.markethub.app.model.Order;
import com.markethub.app.model.Product;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

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

    @GetMapping("/{cartId}/addproduct/{productId}")
    public String addProductToCart(@PathVariable("cartId") Long cartId, @PathVariable("productId") Long productId){
        Product product = productService.getProductById(productId);
        shoppingCartService.addProductToShoppingCart(cartId, product);
        return "redirect:/onlinemarket/secured/services/products/list";
    }

    @GetMapping("/addproduct/{productId}")
    public String addProductToCartByUser(@PathVariable("productId") Long productId){
        long uid = userDetailsServiceImpl.getCurrentUser().getUserId();
        ShoppingCart cart = shoppingCartService.getShoppingCartByBuyer(uid);
        Product product = productService.getProductById(productId);
        shoppingCartService.addProductToShoppingCart(cart.getCartId(), product);
        return "redirect:/onlinemarket/secured/services/products/list";
    }


    @GetMapping("/me")
    public String loadMyCart(){
        long uid = userDetailsServiceImpl.getCurrentUser().getUserId();
        return "redirect:/onlinemarket/cart/" + uid;
    }

    @GetMapping("/{buyerId}")
    public String loadShoppingCartById(@PathVariable("buyerId") long buyerId, Model model){
        ShoppingCart cart = shoppingCartService.getShoppingCartByBuyer(buyerId);
        double total = cart.getProducts().stream().mapToDouble(p -> p.getPrice()).sum();
        model.addAttribute("shoppingCart", cart);
        model.addAttribute("cartTotal", total);
        model.addAttribute("currentUser", userDetailsServiceImpl.getCurrentUser());
        return "secured/services/buyer/cart/cartPage";
    }

    @GetMapping("/{cartId}/delete/{productId}")
    public String removeProductFromCart(@PathVariable("productId") Long productId, @PathVariable("cartId") Long cartId){
        shoppingCartService.deleteProductFromCart(productId, cartId);
        ShoppingCart cart= shoppingCartService.getShoppingCartById(cartId);
        String buyerId=cart.getBuyer().getUserId().toString();
        return "redirect:/onlinemarket/cart/"+buyerId;
    }

    @GetMapping("/{cartId}/checkout/{userId}")
    public String checkOutProductsFromCart(@PathVariable("cartId") Long cartId, @PathVariable("userId") Long userId){

        ShoppingCart cart= shoppingCartService.getShoppingCartById(cartId);
        List<Product> cartProducts= cart.getProducts();
        double cartPrice= 0;
        for (Product product: cartProducts){
            cartPrice= cartPrice+ product.getPrice();
        }
        User buyer= userService.getUserById(userId);
        Order order= new Order("Pending", LocalDate.now(), cartPrice, buyer);
        orderService.saveOrder(order);

        shoppingCartService.deleteAllProductsFromCart(cartId);
        String buyerId=cart.getBuyer().getUserId().toString();
        return "redirect:/onlinemarket/cart/"+buyerId;

    }


}
