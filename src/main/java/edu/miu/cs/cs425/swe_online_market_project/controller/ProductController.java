package edu.miu.cs.cs425.swe_online_market_project.controller;

import edu.miu.cs.cs425.swe_online_market_project.model.Product;
import edu.miu.cs.cs425.swe_online_market_project.service.ProductService;
import edu.miu.cs.cs425.swe_online_market_project.service.imp.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping({"onlinemarket/secured/services/products"})
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    // Displays
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
    public String displayProductById(@PathVariable("id") long id, Model model){
        model.addAttribute("product", productService.getProductById(id));
        return "product/product-view";
    }

    @GetMapping("/my-products/{userId}")
//    @PreAuthorize("hasRole('" + Role.SELLER + "')")
    public String displaySellerProducts(@PathVariable("userId") long userId, Model model){
        model.addAttribute("products", productService.getAllProductsBySellerId(userId));
        return "secured/services/seller/sellerPage";
    }

    @GetMapping("/new-product")
    public ModelAndView displayNewProductForm(){
        Product product1= new Product();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("product", product1);

        modelAndView.addObject("errors", product1);
        modelAndView.setViewName("secured/services/seller/product-form-new");
        return modelAndView;
    }

    @GetMapping("/update-product/{id}")
    public String displayUpdateProductForm(Model model, @PathVariable("id") long id){
        model.addAttribute("product", productService.getProductById(id));
        return "secured/services/seller/product-form";
    }

    // Save and Update

    @PostMapping("/save-product")
    public String saveProduct(Model model, @Valid @ModelAttribute("product") Product product, BindingResult result){
        if (result.hasErrors()){
            model.addAttribute("errors", result.getAllErrors());
            return "secured/services/seller/product-form-new";
        }
        productService.saveProduct(product);
        return "redirect:/onlinemarket/secured/services/products/my-products";
//        return "secured/services/seller/sellerPage";
    }


    // Delete
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") long id){
        productService.deleteById(id);
        return "redirect:/onlinemarket/secured/services/products/my-products";
    }
    @GetMapping(value = "/search")
    public ModelAndView searchBooks(@RequestParam String searchString) {
        ModelAndView modelAndView = new ModelAndView();
        List<Product> products= productService.searchProducts(searchString);
        modelAndView.addObject("products", products);
        modelAndView.addObject("searchString", searchString);
        modelAndView.addObject("studentCount", products.size());
        modelAndView.setViewName("product/list");
        return modelAndView;
    }


}
