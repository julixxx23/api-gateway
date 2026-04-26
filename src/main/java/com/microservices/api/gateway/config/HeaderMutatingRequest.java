package com.microservices.api.gateway.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class HeaderMutatingRequest extends HttpServletRequestWrapper {

    private static final List<String> BLOCKED_HEADERS = List.of(
            "X-User-Name", "X-User-Role", "X-User-Id"
    );

    public HeaderMutatingRequest(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getHeader(String name) {
        if (BLOCKED_HEADERS.stream()
                .anyMatch(h -> h.equalsIgnoreCase(name))) {
            return null;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (BLOCKED_HEADERS.stream()
                .anyMatch(h -> h.equalsIgnoreCase(name))) {
            return Collections.emptyEnumeration();
        }
        return super.getHeaders(name);
    }
}