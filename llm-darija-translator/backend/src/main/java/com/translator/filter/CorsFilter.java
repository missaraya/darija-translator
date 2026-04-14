package com.translator.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

/**
 * CORS filter — allows the Chrome extension, PHP client, Python client,
 * and React Native dev server to call this backend from different origins.
 *
 * For production you should restrict Access-Control-Allow-Origin to known origins.
 * For this local demo, we allow all origins (*).
 */
@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Handle pre-flight OPTIONS requests immediately
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            requestContext.abortWith(
                    Response.ok()
                            .header("Access-Control-Allow-Origin",  "*")
                            .header("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization")
                            .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                            .header("Access-Control-Max-Age",       "3600")
                            .build()
            );
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        // Add CORS headers to every response
        responseContext.getHeaders().add("Access-Control-Allow-Origin",  "*");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }
}
