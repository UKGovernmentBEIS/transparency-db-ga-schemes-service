package com.beis.subsidy.ga.schemes.dbpublishingservice.controller;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.AdminProgram;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.AdminProgramRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.AuditLogsRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.AdminProgramDetailsRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeSearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.AdminProgramResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.AdminProgramResultsResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.service.AdminProgramService;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.AccessManagementConstant;
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

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.util.Objects;

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
    public ResponseEntity<AdminProgramResponse> getAdminProgramById(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
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

        // Set canDelete
        if(PermissionUtils.isBeisAdmin(userPrincipleObj)){
            response.setCanDelete(true);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping(
            value="update/{id}"
    )
    public ResponseEntity<AdminProgramResponse> updateAdminProgramDetails(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
                                                                          @RequestBody AdminProgramDetailsRequest adminProgramDetailsRequest,
                                                                          @PathVariable("id") String adminProgramNumber,
                                                                          HttpServletResponse response) {

        log.info("{} ::Before calling updateSchemeDetails", loggingComponentName);
        if(Objects.isNull(adminProgramDetailsRequest)|| StringUtils.isEmpty(adminProgramNumber)) {
            log.error("No admin program number provided");
            return new ResponseEntity<>(new AdminProgramResponse(), HttpStatus.BAD_REQUEST);
        }
        //check user role here
        UserPrinciple userPrincipleObj = SearchUtils.isSchemeRoleValidation(objectMapper, userPrinciple,"update admin program");

        AdminProgram adminProgram = adminProgramRepository.findById(adminProgramNumber).orElse(null);
        if(adminProgram == null){
            return new ResponseEntity<>(new AdminProgramResponse(), HttpStatus.BAD_REQUEST);
        }

        // if user not BEIS Admin then;
        if (!PermissionUtils.userHasRole(userPrincipleObj, AccessManagementConstant.BEIS_ADMIN_ROLE)) {
            if(!PermissionUtils.userPrincipleContainsId(userPrinciple, adminProgram.getGrantingAuthority().getAzureGroupId())){
                log.error("User " + userPrincipleObj.getUserName() + " does not have the rights to update scheme: " + adminProgramNumber);
                return new ResponseEntity<>(new AdminProgramResponse(), HttpStatus.FORBIDDEN);
            }
        }


        AdminProgram updatedAdminProgram = adminProgramService.updateAdminProgramDetails(adminProgramDetailsRequest,adminProgramNumber, userPrincipleObj);

        if(updatedAdminProgram == null){
            return new ResponseEntity<>(new AdminProgramResponse(), HttpStatus.BAD_REQUEST);
        }

        AdminProgramResponse adminProgramResponse = new AdminProgramResponse(updatedAdminProgram);

        StringBuilder eventMsg = new StringBuilder("Admin program ");

        if (!adminProgramDetailsRequest.getStatus().equalsIgnoreCase(adminProgram.getStatus())){
            eventMsg.append(adminProgramNumber).append(" is updated to ").append(adminProgramDetailsRequest.getStatus());
        }else{
            eventMsg.append(adminProgramNumber).append(" updated.");
        }

        SearchUtils.saveAuditLog(userPrincipleObj,"Update admin program", adminProgramNumber,eventMsg.toString(),auditLogsRepository);
        log.info("{} ::end of calling updateSchemeDetails", loggingComponentName);

        return new ResponseEntity<>(adminProgramResponse, HttpStatus.OK);
    }
}