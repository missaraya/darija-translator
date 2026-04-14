package com.translator.exception;

import com.translator.dto.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Logger;

/**
 * Catch-all exception mapper for any Exception not handled by a more specific mapper.
 *
 * Important: WebApplicationException (JAX-RS 404, 405, 415, etc.) carries its own
 * pre-built Response. We return that directly so the framework's standard status codes
 * are preserved. All other exceptions become 500 Internal Server Error with a JSON body.
 */
@Provider
public class GeneralExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = Logger.getLogger(GeneralExceptionMapper.class.getName());

    @Override
    public Response toResponse(Exception ex) {

        // Let JAX-RS WebApplicationExceptions (404, 405, 415, etc.) pass through
        // with their own response — do not override them with 500.
        if (ex instanceof WebApplicationException wae) {
            return wae.getResponse();
        }

        LOGGER.severe("Unhandled exception: " + ex.getClass().getName() + " — " + ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                500
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
