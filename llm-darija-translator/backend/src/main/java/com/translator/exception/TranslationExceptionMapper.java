package com.translator.exception;

import com.translator.dto.ErrorResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps TranslationException to a 502 Bad Gateway JSON response
 * (the LLM provider is the upstream dependency that failed).
 */
@Provider
public class TranslationExceptionMapper implements ExceptionMapper<TranslationException> {

    @Override
    public Response toResponse(TranslationException ex) {
        ErrorResponse error = new ErrorResponse(
                "Translation Error",
                ex.getMessage(),
                502
        );
        return Response.status(502)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
