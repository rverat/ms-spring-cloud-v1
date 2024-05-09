package com.thedevlair.product;

import com.thedevlair.model.CategoryResponse;
import com.thedevlair.model.ProductResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final WebClient webClient;
    //private final ReactiveCircuitBreakerFactory cbFactory;
   // private final CircuitBreaker circuitBreaker;

    @Autowired
    public ProductController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("lb://category-service").build();
        //this.circuitBreaker = circuitBreaker;
        //this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("externalServiceCategory");
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @CircuitBreaker(name = "externalServiceCategory", fallbackMethod = "fallback")
    public Mono<ProductResponse> getProduct() {
        return webClient.get()
                .uri("/api/categories")
                .retrieve()
                .bodyToMono(CategoryResponse.class)
                .flatMap(categoryResponse -> Mono.just(
                        ProductResponse.builder()
                                .id(101)
                                .name("microfono")
                                .description("microfono fifani")
                                .categoryResponse(categoryResponse)
                                .build()));
    }



    public Mono<ProductResponse> fallback(Throwable throwable) {
        // Handle fallback logic here (e.g., log error, return default product)
        System.err.println("Error fetching categories: " + throwable.getMessage());
        return Mono.just(ProductResponse.builder()
                .id(0)
                .name("Default Product")
                .description("Fallback product description")
                .categoryResponse(CategoryResponse.builder().id(0).name("Default Category").build())
                .build());
    }

}

