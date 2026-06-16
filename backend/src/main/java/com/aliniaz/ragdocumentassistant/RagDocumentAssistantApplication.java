package com.aliniaz.ragdocumentassistant;

import com.aliniaz.ragdocumentassistant.embedding.config.EmbeddingProperties;
import com.aliniaz.ragdocumentassistant.common.config.OllamaProperties;
import com.aliniaz.ragdocumentassistant.llm.config.LlmProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        EmbeddingProperties.class,
        OllamaProperties.class,
        LlmProperties.class
})
public class RagDocumentAssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(RagDocumentAssistantApplication.class, args);
	}

}
