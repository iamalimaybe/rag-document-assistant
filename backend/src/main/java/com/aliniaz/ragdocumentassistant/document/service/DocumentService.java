package com.aliniaz.ragdocumentassistant.document.service;

import com.aliniaz.ragdocumentassistant.document.api.response.DocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    DocumentResponse upload(MultipartFile file);

    List<DocumentResponse> findAll();

    DocumentResponse findById(Long id);
}