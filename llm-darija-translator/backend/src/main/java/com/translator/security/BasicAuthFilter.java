package com.translator.security;

import com.translator.config.AppConfig;
import com.translator.dto.ErrorResponse;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * Jakarta REST ContainerRequestFilter that enforces HTTP Basic Authentication
 * on all endpoints except /health and /info (which are public).
 *
 * How it works:
 *  1. Reads the Authorization header.
 *  2. Decodes the Base64 "username:password" credentials.
 *  3. Compares against credentials from AppConfig (read from microprofile-config.properties
 *     or environment variables AUTH_USERNAME / AUTH_PASSWORD).
 *  4. Returns 401 Unauthorized if missing or wrong — with WWW-Authenticate header
 *     so browsers and Postman know this resource requires Basic Auth.
 */
@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION)
public class BasicAuthFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(BasicAuthFilter.class.getName());

    @Inject
    private AppConfig config;

    // Endpoints that do NOT require authentication
    private static final String[] PUBLIC_PATHS = {"/health", "/info"};

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path   = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();

        // Let CORS pre-flight requests (OPTIONS) through unconditionally.
        // Browsers never send credentials with pre-flight, so blocking them causes
        // CORS errors in the Chrome extension, PHP client, and Streamlit app.
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return;
        }

        // Allow public endpoints through without authentication
        for (String publicPath : PUBLIC_PATHS) {
            if (path.endsWith(publicPath)) {
                return;
            }
        }

        // All other endpoints require Basic Auth
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            LOGGER.warning("Request missing Authorization header: " + path);
            abortWithUnauthorized(requestContext, "Authorization header required");
            return;
        }

        if (!isValidCredentials(authHeader)) {
            LOGGER.warning("Invalid credentials for path: " + path);
            abortWithUnauthorized(requestContext, "Invalid username or password");
        }
    }

    private boolean isValidCredentials(String authHeader) {
        try {
            // Strip "Basic " prefix and Base64-decode
            String base64Part = authHeader.substring("Basic ".length()).trim();
            String decoded = new String(Base64.getDecoder().decode(base64Part), StandardCharsets.UTF_8);

            // decoded format: "username:password"
            int colonIndex = decoded.indexOf(':');
            if (colonIndex < 0) {
                return false;
            }

            String username = decoded.substring(0, colonIndex);
            String password = decoded.substring(colonIndex + 1);

            return username.equals(config.getAuthUsername())
                    && password.equals(config.getAuthPassword());

        } catch (IllegalArgumentException e) {
            // Invalid Base64
            return false;
        }
    }

    private void abortWithUnauthorized(ContainerRequestContext ctx, String message) {
        ErrorResponse error = new ErrorResponse("Unauthorized", message, 401);
        ctx.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity(error)
                        .type(MediaType.APPLICATION_JSON)
                        // WWW-Authenticate header tells the client that Basic Auth is required
                        .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"DarijaTranslator\"")
                        .build()
        );
    }
}
