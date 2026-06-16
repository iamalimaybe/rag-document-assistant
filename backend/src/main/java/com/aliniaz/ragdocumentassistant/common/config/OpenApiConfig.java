package com.aliniaz.ragdocumentassistant.common.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ragDocumentAssistantOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("RAG Document Assistant API")
                        .version("v0.1")
                        .description("""
                                Backend API for a grounded RAG document assistant.

                                Supports document upload, text extraction, chunking, embeddings,
                                retrieval testing, grounded question answering, citations,
                                missing-info handling, and QA run history.
                                """))
                .externalDocs(new ExternalDocumentation()
                        .description("Project repository")
                        .url("https://github.com/iamalimaybe/rag-document-assistant"));
    }
}