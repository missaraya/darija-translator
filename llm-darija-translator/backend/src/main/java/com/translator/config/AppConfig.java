package com.translator.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Inject;

/**
 * Centralized application configuration.
 * Values are read from microprofile-config.properties or overridden
 * by environment variables (MicroProfile Config ordinal: env vars beat properties file).
 *
 * Environment variable mapping examples:
 *   GEMINI_API_KEY  ->  gemini.api.key
 *   AUTH_USERNAME   ->  auth.username
 *   AUTH_PASSWORD   ->  auth.password
 */
@ApplicationScoped
public class AppConfig {

    @Inject
    @ConfigProperty(name = "app.name", defaultValue = "Darija Translator")
    private String appName;

    @Inject
    @ConfigProperty(name = "app.version", defaultValue = "1.0.0")
    private String appVersion;

    @Inject
    @ConfigProperty(name = "auth.username", defaultValue = "student")
    private String authUsername;

    @Inject
    @ConfigProperty(name = "auth.password", defaultValue = "student123")
    private String authPassword;

    @Inject
    @ConfigProperty(name = "gemini.api.key", defaultValue = "not-configured")
    private String geminiApiKey;

    @Inject
    @ConfigProperty(name = "gemini.model", defaultValue = "gemini-1.5-flash")
    private String geminiModel;

    @Inject
    @ConfigProperty(name = "gemini.api.url",
            defaultValue = "https://generativelanguage.googleapis.com/v1beta/models")
    private String geminiApiUrl;

    @Inject
    @ConfigProperty(name = "translation.max.text.length", defaultValue = "5000")
    private int maxTextLength;

    public String getAppName()      { return appName; }
    public String getAppVersion()   { return appVersion; }
    public String getAuthUsername() { return authUsername; }
    public String getAuthPassword() { return authPassword; }
    public String getGeminiApiKey() { return geminiApiKey; }
    public String getGeminiModel()  { return geminiModel; }
    public String getGeminiApiUrl() { return geminiApiUrl; }
    public int    getMaxTextLength(){ return maxTextLength; }
}
