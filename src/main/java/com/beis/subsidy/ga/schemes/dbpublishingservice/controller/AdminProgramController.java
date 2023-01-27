package com.beis.subsidy.ga.schemes.dbpublishingservice.controller;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.AdminProgram;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.AdminProgramRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.AuditLogsRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.AdminProgramDetailsRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeSearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.AdminProgramResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.AdminProgramResultsResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.service.AdminProgramService;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.PermissionUtils;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.SearchUtils;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.UserPrinciple;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping(path = "/adminprogram")
@RestController
@Slf4j
public class AdminProgramController {

    @Autowired
    private AdminProgramService adminProgramService;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${loggingComponentName}")
    private String loggingComponentName;
    
    @Autowired
    AuditLogsRepository auditLogsRepository;

    @Autowired
    private AdminProgramRepository adminProgramRepository;

    @GetMapping("/health")
    public ResponseEntity<String> getHealth() {
        return new ResponseEntity<>("Successful health check - GA Scheme API", HttpStatus.OK);
    }

    @PostMapping(
            value = "/add"
    )
    public ResponseEntity<AdminProgramResponse> addAdminProgram(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
                                                @Valid @RequestBody AdminProgramDetailsRequest adminProgramRequest) {

        log.info("{} :: inside addSchemeDetails method",loggingComponentName);
        //check user role here
    	UserPrinciple userPrincipleObj = SearchUtils.isSchemeRoleValidation(objectMapper, userPrinciple,"Add Subsidy Schema");
    	        
        AdminProgram adminProgram = adminProgramService.addAdminProgram(adminProgramRequest, userPrincipleObj);

        StringBuilder eventMsg = new StringBuilder("Admin program ").append(adminProgram.getApNumber()).append(" is added");
        SearchUtils.saveAuditLog(userPrincipleObj,"create Admin Program", adminProgram.getApNumber(),eventMsg.toString(),auditLogsRepository);
        log.info("{} :: End of  addSchemeDetails method",loggingComponentName);

        AdminProgramResponse response = new AdminProgramResponse(adminProgram);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(
            value = "/search",
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AdminProgramResultsResponse> findAdminPrograms(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
                                                                         @Valid @RequestBody SchemeSearchInput searchInput) {

        UserPrinciple userPrincipleResp = SearchUtils.isAllRolesValidation(objectMapper, userPrinciple,"find admin programs");
        if(searchInput.getTotalRecordsPerPage() == null){
            searchInput.setTotalRecordsPerPage(10);
        }
        if(searchInput.getPageNumber() == null) {
            searchInput.setPageNumber(1);
        }
        AdminProgramResultsResponse searchResults = adminProgramService.findMatchingAdminProgramDetails(searchInput,userPrincipleResp);
        return new ResponseEntity<AdminProgramResultsResponse>(searchResults, HttpStatus.OK);
    }

    @GetMapping(
            value = "{id}",
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AdminProgramResponse> findSubsidyScheme(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
                                                                    @PathVariable("id") String apNumber) {
        log.info("{} ::Before calling findById", loggingComponentName);
        UserPrinciple userPrincipleObj = SearchUtils.isAllRolesValidation(objectMapper, userPrinciple,"find admin program");
        if (StringUtils.isEmpty(apNumber)) {
            return new ResponseEntity<>(new AdminProgramResponse(), HttpStatus.NOT_FOUND);
        }
        AdminProgram adminProgram = adminProgramRepository.findById(apNumber).orElse(null);

        if (adminProgram == null){
            return new ResponseEntity<>(new AdminProgramResponse(), HttpStatus.NOT_FOUND);
        }

        AdminProgramResponse response = new AdminProgramResponse(adminProgram);

        // Set canEdit
        if (PermissionUtils.isBeisAdmin(userPrincipleObj) ||
                (PermissionUtils.isGaAdmin(userPrincipleObj) &&
                        PermissionUtils.userPrincipleContainsId(userPrinciple, adminProgram.getGrantingAuthority().getAzureGroupId()))){
            response.setCanEdit(true);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}