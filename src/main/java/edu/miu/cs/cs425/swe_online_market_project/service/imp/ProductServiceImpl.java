package edu.miu.cs.cs425.swe_online_market_project.service.imp;

import edu.miu.cs.cs425.swe_online_market_project.model.Product;
import edu.miu.cs.cs425.swe_online_market_project.repository.ProductRepository;
import edu.miu.cs.cs425.swe_online_market_project.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Page<Product> getPagedProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size, Sort.by("name")));
    }

    @Override
    public Product getProductById(long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(long id, Product product) {
        product.setProductId(id);
        return productRepository.save(product);
    }

    @Override
    public List<Product> getAllProductsBySellerId(long id) {
        return productRepository.findBySellerId(id);
    }

    @Override
    public List<Product> getAllProductWhichAreNotOutOfStock() {
        return productRepository.findNotOutOfStockProducts();
    }

    @Override
    public void updateProductQuantity(List<Product> productList) {
        productRepository.saveAll(productList);
    }

    @Override
    public void deleteById(long id) {
        productRepository.deleteById(id);
    }
    @Override
    public List<Product> searchProducts(String searchString) {
        return productRepository.findAllByProductName(searchString);

    }

}
