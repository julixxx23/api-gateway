package com.microservices.api.gateway.config;

import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

@Configuration
public class GatewayConfig{

    //Funcion de limpia registros y monitoreo del servicio Users
    private RouterFunction<ServerResponse> buildRoute(
            String routeId, String path, String serviceName) {

        return GatewayRouterFunctions.route(routeId)
                .route(GatewayRequestPredicates.path(path),
                        HandlerFunctions.http())
                .filter(LoadBalancerFilterFunctions.lb(serviceName))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        serviceName + "-cb",
                        URI.create("forward:/fallback/" + serviceName)
                ))
                .filter((request, next) -> {
                    String username = (String) request.servletRequest()
                            .getAttribute("X-User-Name");
                    String role = (String) request.servletRequest()
                            .getAttribute("X-User-Role");

                    ServerRequest mutated = ServerRequest.from(request)
                            .header("X-User-Name", username != null ? username : "")
                            .header("X-User-Role", role != null ? role : "")
                            .build();

                    return next.handle(mutated);
                })
                .build();
    }
    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return buildRoute("user-service", "/api/v1/users/**", "user-service");
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceAuthRoute() {
        return buildRoute("user-service-auth", "/api/v1/auth/**", "user-service");
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceRoute() {
        return buildRoute("order-service", "/api/v1/orders/**", "order-service");
    }
}