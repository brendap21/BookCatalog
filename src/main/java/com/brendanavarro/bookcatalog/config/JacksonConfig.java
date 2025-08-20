package com.brendanavarro.bookcatalog.config;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración centralizada de Jackson.
 * - SNAKE_CASE: convierte "download_count" -> "downloadCount" automáticamente.
 * - Ignora propiedades desconocidas.
 * - Acepta case-insensitive para nombres de propiedades.
 * - Registra módulos (JDK8, JavaTime).
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build()
                .findAndRegisterModules();
    }
}
