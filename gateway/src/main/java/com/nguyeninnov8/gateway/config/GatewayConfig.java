package com.nguyeninnov8.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Services Routes
                .route("auth-service", r -> r.path("api/auth/**")
                        .filters(f -> f.rewritePath("api/auth/(?<segment>.*)", "/${segment}"))
                        .uri("lb://auth-service"))
                // User Services Routes
                .route("user-service", r -> r.path("api/users/**")
                        .filters(f -> f.rewritePath("api/users/(?<segment>.*)", "/${segment}"))
                        .uri("lb://user-service"))
                // Other service routes would be added here
                .build();
    }
}
