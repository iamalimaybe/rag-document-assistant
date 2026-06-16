package com.aliniaz.ragdocumentassistant;

import com.aliniaz.ragdocumentassistant.document.config.ChunkingProperties;
import com.aliniaz.ragdocumentassistant.document.config.DocumentNormalizationProperties;
import com.aliniaz.ragdocumentassistant.document.config.TokenEstimationProperties;
import com.aliniaz.ragdocumentassistant.embedding.config.EmbeddingProperties;
import com.aliniaz.ragdocumentassistant.common.config.OllamaProperties;
import com.aliniaz.ragdocumentassistant.llm.config.LlmProperties;
import com.aliniaz.ragdocumentassistant.rag.config.RagContextBudgetProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        EmbeddingProperties.class,
        OllamaProperties.class,
        LlmProperties.class,
        ChunkingProperties.class,
        TokenEstimationProperties.class,
        DocumentNormalizationProperties.class,
        RagContextBudgetProperties.class
})
public class RagDocumentAssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(RagDocumentAssistantApplication.class, args);
	}

}
