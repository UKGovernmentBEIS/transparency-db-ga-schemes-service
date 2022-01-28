package com.beis.subsidy.ga.schemes.dbpublishingservice.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.validation.Valid;

import com.beis.subsidy.ga.schemes.dbpublishingservice.util.UserPrinciple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.InvalidRequestException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.AuditLogsRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeDetailsRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeSearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SearchSubsidyResultsResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SubsidyMeasureResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.service.SubsidySchemeService;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.SearchUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@RequestMapping(path = "/scheme")
@RestController
@Slf4j
public class SubsidySchemeController {

    @Autowired
    private SubsidySchemeService subsidySchemeService;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${loggingComponentName}")
    private String loggingComponentName;
    
    @Autowired
    AuditLogsRepository auditLogsRepository;

    @GetMapping("/health")
    public ResponseEntity<String> getHealth() {
        return new ResponseEntity<>("Successful health check - GA Scheme API", HttpStatus.OK);
    }
    @PostMapping(
            value = "/search",
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SearchSubsidyResultsResponse> findSchemeDetails(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
                                                                          @Valid @RequestBody SchemeSearchInput searchInput) {

        UserPrinciple userPrinicipleResp = SearchUtils.isAllRolesValidation(objectMapper, userPrinciple,"find Subsidy Schema");
        if(searchInput.getTotalRecordsPerPage() == null){
            searchInput.setTotalRecordsPerPage(10);
        }
        if(searchInput.getPageNumber() == null) {
            searchInput.setPageNumber(1);
        }
        SearchSubsidyResultsResponse searchResults = subsidySchemeService.findMatchingSubsidySchemeDetails(searchInput,userPrinicipleResp);
        return new ResponseEntity<SearchSubsidyResultsResponse>(searchResults, HttpStatus.OK);
    }

    @PostMapping(
            value = "/add"
    )
    public String addSchemeDetails(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
                                   @Valid @RequestBody SchemeDetailsRequest scheme) {

        log.info("{} :: inside addSchemeDetails method",loggingComponentName);
        //check user role here
    	UserPrinciple userPrincipleObj = SearchUtils.isSchemeRoleValidation(objectMapper, userPrinciple,"Add Subsidy Schema");
    	        
        String scNumber = subsidySchemeService.addSubsidySchemeDetails(scheme);
        //Audit entry
        StringBuilder eventMsg = new StringBuilder("Scheme ").append(scNumber).append(" is added");
        SearchUtils.saveAuditLog(userPrincipleObj,"create Schemes", scNumber,eventMsg.toString(),auditLogsRepository);
        log.info("{} :: End of  addSchemeDetails method",loggingComponentName);
   	    return scNumber;
    }

    @PutMapping(
            value="update/{scNumber}"
    )
    public String updateSchemeDetails(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
                                      @RequestBody SchemeDetailsRequest schemeReq,
                                      @PathVariable("scNumber") String scNumber) {

        log.info("{} ::Before calling updateSchemeDetails", loggingComponentName);
        if(Objects.isNull(schemeReq)|| StringUtils.isEmpty(scNumber)) {
            throw new InvalidRequestException("schemeReq is empty or scNumber");
        }
    	//check user role here
		UserPrinciple userPrincipleObj = SearchUtils.isSchemeRoleValidation(objectMapper, userPrinciple,"update Subsidy Schema");
        String scNumberRes= subsidySchemeService.updateSubsidySchemeDetails(schemeReq,scNumber, userPrincipleObj);

        StringBuilder eventMsg = new StringBuilder("Scheme ").append(scNumber).append(" is updated to ")
                .append(schemeReq.getStatus());


        SearchUtils.saveAuditLog(userPrincipleObj,"Update Schemes", scNumberRes,eventMsg.toString(),auditLogsRepository);
        log.info("{} ::end of calling updateSchemeDetails", loggingComponentName);

        return scNumber;
    }

    @GetMapping(
            value = "{scNumber}",
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SubsidyMeasureResponse> findSubsidyScheme(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
                                                                    @PathVariable("scNumber") String scNumber) {
        log.info("{} ::Before calling findSubsidyScheme", loggingComponentName);
        SearchUtils.isAllRolesValidation(objectMapper, userPrinciple,"find Subsidy Schema");
        if (StringUtils.isEmpty(scNumber)) {
            throw new InvalidRequestException("Bad Request SC Number is null");
        }
        SubsidyMeasureResponse subsidySchemeById = subsidySchemeService.findSubsidySchemeById(scNumber);
        return new ResponseEntity<SubsidyMeasureResponse>(subsidySchemeById, HttpStatus.OK);
    }
}