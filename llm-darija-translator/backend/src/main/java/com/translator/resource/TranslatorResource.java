package com.translator.resource;

import com.translator.config.AppConfig;
import com.translator.dto.TranslationRequest;
import com.translator.dto.TranslationResponse;
import com.translator.service.TranslationService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * JAX-RS REST resource for the Darija Translator service.
 *
 * Base path: /api/translator
 *
 * Endpoints:
 *   GET  /api/translator/health    — public, no auth required
 *   GET  /api/translator/info      — public, service metadata
 *   POST /api/translator/translate — PROTECTED (Basic Auth required)
 *
 * Security is handled by BasicAuthFilter (a @PreMatching ContainerRequestFilter),
 * not by annotations here — keeping the resource clean and readable.
 */
@RequestScoped
@Path("/translator")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TranslatorResource {

    private static final Logger LOGGER = Logger.getLogger(TranslatorResource.class.getName());

    @Inject
    private TranslationService translationService;

    @Inject
    private AppConfig config;

    /**
     * Health check — used by monitoring tools and the Chrome extension
     * to verify the backend is reachable before translating.
     *
     * Not protected. Returns 200 OK.
     */
    @GET
    @Path("/health")
    public Response health() {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("status",  "UP");
        body.put("service", config.getAppName());
        body.put("version", config.getAppVersion());
        return Response.ok(body).build();
    }

    /**
     * Service info — returns metadata about the translator service.
     * Useful for client apps to show which provider is active.
     *
     * Not protected.
     */
    @GET
    @Path("/info")
    public Response info() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service",         config.getAppName());
        body.put("version",         config.getAppVersion());
        body.put("provider",        "Gemini");
        body.put("model",           config.getGeminiModel());
        body.put("supportedTargets", new String[]{"Moroccan Darija", "French", "Spanish", "Arabic"});
        body.put("maxTextLength",   config.getMaxTextLength());
        return Response.ok(body).build();
    }

    /**
     * Translation endpoint — the core of this service.
     *
     * Requires Basic Authentication (student / student123 by default).
     * Protected by BasicAuthFilter — returns 401 if credentials are missing or wrong.
     *
     * @param request JSON body with text, sourceLanguage, targetLanguage.
     * @return JSON with originalText, translatedText, sourceLanguage, targetLanguage, provider, status.
     */
    @POST
    @Path("/translate")
    public Response translate(TranslationRequest request) {
        LOGGER.info("Received translate request");

        TranslationResponse result = translationService.translate(request);

        LOGGER.info("Translation completed successfully");
        return Response.ok(result).build();
    }
}
