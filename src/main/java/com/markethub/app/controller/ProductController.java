package com.markethub.app.controller;

import com.markethub.app.model.Product;
import com.markethub.app.service.ProductService;
import com.markethub.app.service.imp.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping({"onlinemarket/secured/services/products"})
public class ProductController {

    private final ProductService productService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    public ProductController(ProductService productService, UserDetailsServiceImpl userDetailsServiceImpl) {
        this.productService = productService;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    @GetMapping("/list")
    public String displayAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model) {
        Page<Product> productPage = productService.getPagedProducts(page, size);
        model.addAttribute("products", productPage);
        model.addAttribute("currentUser", userDetailsServiceImpl.getCurrentUser());
        return "secured/services/buyer/product/list";
    }

    @GetMapping("/{id}")
    public String displayProductById(@PathVariable("id") long id, Model model) {
        model.addAttribute("product", productService.getProductById(id));
        return "product/product-view";
    }

    @GetMapping("/my-products")
    public String displayMyProducts(Model model) {
        com.markethub.app.model.User cu = userDetailsServiceImpl.getCurrentUser();
        model.addAttribute("products", productService.getAllProductsBySellerId(cu.getUserId()));
        model.addAttribute("currentUser", cu);
        return "secured/services/seller/sellerPage";
    }

    @GetMapping("/my-products/{userId}")
    public String displaySellerProducts(@PathVariable("userId") long userId, Model model) {
        model.addAttribute("products", productService.getAllProductsBySellerId(userId));
        model.addAttribute("currentUser", userDetailsServiceImpl.getCurrentUser());
        return "secured/services/seller/sellerPage";
    }

    @GetMapping("/new-product")
    public ModelAndView displayNewProductForm() {
        Product product = new Product();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("product", product);
        modelAndView.addObject("errors", product);
        modelAndView.setViewName("secured/services/seller/product-form-new");
        return modelAndView;
    }

    @GetMapping("/update-product/{id}")
    public String displayUpdateProductForm(Model model, @PathVariable("id") long id) {
        model.addAttribute("product", productService.getProductById(id));
        return "secured/services/seller/product-form";
    }

    @PostMapping("/save-product")
    public String saveProduct(Model model, @Valid @ModelAttribute("product") Product product, BindingResult result) {
        if (result.hasErrors()) {
            model.addAttribute("errors", result.getAllErrors());
            return "secured/services/seller/product-form-new";
        }
        com.markethub.app.model.User cu = userDetailsServiceImpl.getCurrentUser();
        if (product.getSeller() == null) {
            product.setSeller(cu);
        }
        productService.saveProduct(product);
        return "redirect:/onlinemarket/secured/services/products/my-products/" + cu.getUserId();
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") long id) {
        com.markethub.app.model.User currentUser = userDetailsServiceImpl.getCurrentUser();
        com.markethub.app.model.Product product = productService.getProductById(id);
        if (product.getSeller() == null || product.getSeller().getUserId() != currentUser.getUserId()) {
            return "redirect:/onlinemarket/secured/services/products/my-products/" + currentUser.getUserId();
        }
        productService.deleteById(id);
        return "redirect:/onlinemarket/secured/services/products/my-products/" + currentUser.getUserId();
    }

    @GetMapping(value = "/search")
    public ModelAndView searchProducts(@RequestParam String searchString) {
        ModelAndView modelAndView = new ModelAndView();
        List<Product> products = productService.searchProducts(searchString);
        modelAndView.addObject("products", products);
        modelAndView.addObject("searchString", searchString);
        modelAndView.addObject("productCount", products.size());
        modelAndView.setViewName("product/list");
        return modelAndView;
    }
}
