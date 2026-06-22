package edu.miu.cs.cs425.swe_online_market_project.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Online Market System API")
                        .description("REST API for a multi-role e-commerce platform supporting Admin, Seller, and Buyer workflows.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Kalabe Kebede")
                                .email("Kalabe.Kebede@revature.net"))
                        .license(new License()
                                .name("MIT")));
    }
}
