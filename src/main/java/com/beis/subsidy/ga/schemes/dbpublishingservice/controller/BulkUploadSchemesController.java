package com.beis.subsidy.ga.schemes.dbpublishingservice.controller;

import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.AuditLogsRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.ValidationErrorResult;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.ValidationResult;
import com.beis.subsidy.ga.schemes.dbpublishingservice.service.BulkUploadSchemesService;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.ExcelHelper;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.UserPrinciple;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.MultipartConfigElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;

@Slf4j
@RestController
public class BulkUploadSchemesController {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    AuditLogsRepository auditLogsRepository;

    @Autowired
    public BulkUploadSchemesService bulkUploadSchemesService;

    public static final String All_ROLES[]= {"BEIS Administrator","Granting Authority Administrator",
            "Granting Authority Approver","Granting Authority Encoder"};


    @GetMapping("/health")
    public ResponseEntity<String> getHealth() {
        return new ResponseEntity<>("Successful health check - DB publishing Subsidies Service API", HttpStatus.OK);
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement("");
    }
    @Bean
    public MultipartResolver multipartResolver() {
        org.springframework.web.multipart.commons.CommonsMultipartResolver multipartResolver = new org.springframework.web.multipart.commons.CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(10000000);
        return multipartResolver;
    }

    @PostMapping(value = "/uploadBulkSchemes", consumes = { "multipart/form-data" })
    public ResponseEntity<ValidationResult> uploadSchemesFile(@RequestParam("file") MultipartFile file,
                                                             @RequestHeader("userPrinciple") HttpHeaders userPrinciple){
        UserPrinciple userPrincipleObj = null;
        //1.0 - Check uploaded file format to be xlsx
        if(ExcelHelper.hasExcelFormat(file)) {

            try {
                log.info("{} :: Before calling validateFile", loggingComponentName);
                String userPrincipleStr = userPrinciple.get("userPrinciple").get(0);
                userPrincipleObj = objectMapper.readValue(userPrincipleStr, UserPrinciple.class);
                if (!Arrays.asList(All_ROLES).contains(userPrincipleObj.getRole())) {
                    ValidationResult validationResult = new ValidationResult();
                    validationResult.setMessage("You are not authorised to bulk upload schemes");
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(validationResult);
                }
                ValidationResult validationResult = bulkUploadSchemesService.validateFile(file,
                        userPrincipleObj.getUserName());
                return ResponseEntity.status(HttpStatus.OK).body(validationResult);

            } catch (Exception e) {

                log.error("{} :: Exception block in BulkUploadSchemesController", loggingComponentName,e);
                //2.0 - CatchException and return validation errors
                ValidationResult validationResult = new ValidationResult();
                validationResult.setMessage("Bulk upload failed");
                ValidationErrorResult errorResult = new ValidationErrorResult();
                errorResult.setErrorMessages(e.getMessage());
                List<ValidationErrorResult> validationErrorResult = new ArrayList<>();
                validationErrorResult.add(errorResult);
                validationResult.setValidationErrorResult(validationErrorResult);
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(validationResult);
            }
        } else {

            //3.0 - Wrong format file
            ValidationResult validationResult = new ValidationResult();

            ValidationErrorResult validationErrorResult = new ValidationErrorResult();
            validationErrorResult.setRow("All");
            validationErrorResult.setColumns("All");
            validationErrorResult.setErrorMessages("Upload an excel file (in format xlsx) !");

            validationResult.setTotalRows(0);
            validationResult.setErrorRows(0);
            validationResult.setValidationErrorResult(Arrays.asList(validationErrorResult));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);
        }
    }
}


