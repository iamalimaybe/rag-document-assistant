package com.aliniaz.ragdocumentassistant.document.service;

import com.aliniaz.ragdocumentassistant.document.domain.DocumentSourceType;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentTextExtractor {

    ExtractedDocumentText extract(MultipartFile file, DocumentSourceType sourceType);
}