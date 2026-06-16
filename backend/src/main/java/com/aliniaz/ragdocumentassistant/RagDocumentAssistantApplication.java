package com.aliniaz.ragdocumentassistant;

import com.aliniaz.ragdocumentassistant.embedding.config.EmbeddingProperties;
import com.aliniaz.ragdocumentassistant.embedding.config.OllamaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        EmbeddingProperties.class,
        OllamaProperties.class
})
public class RagDocumentAssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(RagDocumentAssistantApplication.class, args);
	}

}
