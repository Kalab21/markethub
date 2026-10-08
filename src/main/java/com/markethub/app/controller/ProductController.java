package com.markethub.app.controller;

import com.markethub.app.model.Product;
import com.markethub.app.security.AccessGuard;
import com.markethub.app.service.ProductService;
import com.markethub.app.service.imp.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping({"onlinemarket/secured/services/products"})
public class ProductController {

    private final ProductService productService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final AccessGuard accessGuard;

    public ProductController(ProductService productService, UserDetailsServiceImpl userDetailsServiceImpl,
                             AccessGuard accessGuard) {
        this.productService = productService;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.accessGuard = accessGuard;
    }

    /** The owner and the reviews are never taken from the submitted form. */
    @InitBinder("product")
    void restrictBinding(WebDataBinder binder) {
        binder.setDisallowedFields("seller", "seller.*", "reviews", "reviews.*");
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

    /** Read-only product detail page, linked from the product list. */
    @GetMapping("/{id:\\d+}")
    public String displayProductById(@PathVariable("id") long id, Model model) {
        model.addAttribute("product", productService.getProductById(id));
        return "secured/services/buyer/product/product-view";
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
        accessGuard.requireSelfOrAdmin(userId);
        model.addAttribute("products", productService.getAllProductsBySellerId(userId));
        model.addAttribute("currentUser", userDetailsServiceImpl.getCurrentUser());
        return "secured/services/seller/sellerPage";
    }

    @GetMapping("/new-product")
    public ModelAndView displayNewProductForm() {
        accessGuard.requireApprovedSellerOrAdmin();
        Product product = new Product();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("product", product);
        modelAndView.addObject("errors", product);
        modelAndView.setViewName("secured/services/seller/product-form-new");
        return modelAndView;
    }

    @GetMapping("/update-product/{id}")
    public String displayUpdateProductForm(Model model, @PathVariable("id") long id) {
        accessGuard.requireApprovedSellerOrAdmin();
        Product existing = productService.getProductById(id);
        accessGuard.requireOwnerOrAdmin(existing.getSeller());
        model.addAttribute("product", existing);
        return "secured/services/seller/product-form";
    }

    @PostMapping("/save-product")
    public String saveProduct(Model model, @Valid @ModelAttribute("product") Product product, BindingResult result) {
        accessGuard.requireApprovedSellerOrAdmin();
        com.markethub.app.model.User cu = userDetailsServiceImpl.getCurrentUser();
        if (product.getProductId() == 0) {
            product.setSeller(cu);
        } else {
            // An update: only the owner (or an admin) may change a product, and it keeps its owner.
            Product existing = productService.getProductById(product.getProductId());
            accessGuard.requireOwnerOrAdmin(existing.getSeller());
            product.setSeller(existing.getSeller());
            product.setReviews(existing.getReviews());
        }
        if (result.hasErrors()) {
            model.addAttribute("errors", result.getAllErrors());
            return "secured/services/seller/product-form-new";
        }
        productService.saveProduct(product);
        return "redirect:/onlinemarket/secured/services/products/my-products/" + cu.getUserId();
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable("id") long id) {
        accessGuard.requireApprovedSellerOrAdmin();
        com.markethub.app.model.User currentUser = userDetailsServiceImpl.getCurrentUser();
        com.markethub.app.model.Product product = productService.getProductById(id);
        accessGuard.requireOwnerOrAdmin(product.getSeller());
        productService.deleteById(id);
        return "redirect:/onlinemarket/secured/services/products/my-products/" + currentUser.getUserId();
    }
}
