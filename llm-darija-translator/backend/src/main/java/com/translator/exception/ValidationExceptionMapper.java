package com.translator.exception;

import com.translator.dto.ErrorResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps ValidationException to a 400 Bad Request JSON response.
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    @Override
    public Response toResponse(ValidationException ex) {
        ErrorResponse error = new ErrorResponse(
                "Validation Error",
                ex.getMessage(),
                Response.Status.BAD_REQUEST.getStatusCode()
        );
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
