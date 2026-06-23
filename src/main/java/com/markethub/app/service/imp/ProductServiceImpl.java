package com.markethub.app.service.imp;

import com.markethub.app.exception.ResourceNotFoundException;
import com.markethub.app.model.Product;
import com.markethub.app.repository.ProductRepository;
import com.markethub.app.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("products")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getPagedProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size, Sort.by("name")));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product", key = "#id")
    public Product getProductById(long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "product", "sellerProducts"}, allEntries = true)
    public Product saveProduct(Product product) {
        log.info("Saving product sku={}", product.getSku());
        return productRepository.save(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "product", "sellerProducts"}, allEntries = true)
    public Product updateProduct(long id, Product product) {
        product.setProductId(id);
        log.info("Updating product id={}", id);
        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "sellerProducts", key = "#id")
    public List<Product> getAllProductsBySellerId(long id) {
        return productRepository.findBySellerId(id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("inStockProducts")
    public List<Product> getAllProductWhichAreNotOutOfStock() {
        return productRepository.findNotOutOfStockProducts();
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "product", "sellerProducts", "inStockProducts"}, allEntries = true)
    public void updateProductQuantity(List<Product> productList) {
        productRepository.saveAll(productList);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "product", "sellerProducts", "inStockProducts"}, allEntries = true)
    public void deleteById(long id) {
        log.info("Deleting product id={}", id);
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String searchString) {
        return productRepository.findAllByProductName(searchString);
    }
}
