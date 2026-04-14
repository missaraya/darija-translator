package com.translator;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS application entry point.
 * All REST resources are available under /api/...
 */
@ApplicationPath("/api")
public class TranslatorApplication extends Application {
    // No configuration needed — JAX-RS scans for @Path annotated classes automatically.
}
