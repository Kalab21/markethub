package edu.miu.cs.cs425.swe_online_market_project.service;

import edu.miu.cs.cs425.swe_online_market_project.model.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();
    Page<Product> getPagedProducts(int page, int size);

    Product getProductById(long id);

    Product saveProduct(Product product);

    Product updateProduct(long id, Product product);

    List<Product> getAllProductsBySellerId(long id);

    List<Product> getAllProductWhichAreNotOutOfStock();

    void updateProductQuantity(List<Product> productList);
    List<Product> searchProducts(String searchString);
    void deleteById(long id);


     //  void updateReview(Review review, String productId);

//    List<Product> getPendingReviewProducts();

//    void saveAll(List<Product> productList);

//    void updateProductReviewStatus(ProductComment productComment);
}
