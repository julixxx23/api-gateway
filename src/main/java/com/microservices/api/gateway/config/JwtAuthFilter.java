package com.microservices.api.gateway.config;

import com.microservices.api.gateway.config.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private static final List<String> PUBLIC_ROUTES = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Eliminar headers maliciosos del cliente
        HeaderMutatingRequest cleanRequest = new HeaderMutatingRequest(request);

        String path = cleanRequest.getRequestURI();

        if (PUBLIC_ROUTES.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(cleanRequest, response);
            return;
        }

        String authHeader = cleanRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.validateToken(token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        // 2. Extraer claims y guardar como attributes
        String username = jwtService.extractUsername(token);
        String role = jwtService.extractRole(token);

        cleanRequest.setAttribute("X-User-Name", username);
        cleanRequest.setAttribute("X-User-Role", role);

        filterChain.doFilter(cleanRequest, response);
    }
}