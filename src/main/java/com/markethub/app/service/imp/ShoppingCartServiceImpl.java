package com.markethub.app.service.imp;

import com.markethub.app.exception.ResourceNotFoundException;
import com.markethub.app.model.Product;
import com.markethub.app.model.ShoppingCart;
import com.markethub.app.repository.ShoppingCartRepository;
import com.markethub.app.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;

    public ShoppingCartServiceImpl(ShoppingCartRepository shoppingCartRepository) {
        this.shoppingCartRepository = shoppingCartRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ShoppingCart getShoppingCartById(long id) {
        return shoppingCartRepository.getReferenceById(id);
    }

    @Override
    public void deleteShoppingCartById(long id) {
        shoppingCartRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ShoppingCart getShoppingCartByBuyer(Long id) {
        return shoppingCartRepository.findByBuyerUserId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for buyer: " + id));
    }

    @Override
    public ShoppingCart addProductToShoppingCart(Long cartId, Product product) {
        ShoppingCart cart = shoppingCartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found: " + cartId));
        cart.getProducts().add(product);
        log.info("Added product id={} to cart id={}", product.getProductId(), cartId);
        return shoppingCartRepository.save(cart);
    }

    @Override
    public void deleteProductFromCart(Long productId, Long cartId) {
        ShoppingCart cart = shoppingCartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found: " + cartId));
        List<Product> products = cart.getProducts();
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getProductId() == productId) {
                products.remove(i);
                break;
            }
        }
        cart.setProducts(products);
        shoppingCartRepository.save(cart);
    }

    @Override
    public void deleteAllProductsFromCart(Long cartId) {
        ShoppingCart cart = shoppingCartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found: " + cartId));
        cart.getProducts().clear();
        shoppingCartRepository.save(cart);
        log.info("Cleared cart id={}", cartId);
    }
}
