package com.beis.subsidy.ga.schemes.dbpublishingservice.service;

import com.beis.subsidy.ga.schemes.dbpublishingservice.response.ValidationResult;
import org.springframework.web.multipart.MultipartFile;

public interface BulkUploadSchemesService {

    ValidationResult validateFile(MultipartFile file, String role);
}
