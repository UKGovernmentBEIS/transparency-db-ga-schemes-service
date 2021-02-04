package com.beis.subsidy.ga.schemes.dbpublishingservice.controller;

import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.InvalidRequestException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.SubsidyMeasure;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeDetailsRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeSearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SearchSubsidyResultsResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SubsidyMeasureResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.service.impl.SubsidySchemeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.Objects;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping(path = "/scheme")
@RestController
@Slf4j
public class SubsidySchemeController {

    @Autowired
    private SubsidySchemeService subsidySchemeService;

    @GetMapping("/health")
    public ResponseEntity<String> getHealth() {
        return new ResponseEntity<>("Successful health check - GA Scheme API", HttpStatus.OK);
    }
    @PostMapping(
            value = "/search",
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SearchSubsidyResultsResponse> findSchemeDetails(@Valid @RequestBody SchemeSearchInput searchInput) {
        if(searchInput.getTotalRecordsPerPage() == null){
            searchInput.setTotalRecordsPerPage(10);
        }
        if(searchInput.getPageNumber() == null) {
            searchInput.setPageNumber(1);
        }
        SearchSubsidyResultsResponse searchResults = subsidySchemeService.findMatchingSubsidySchemeDetails(searchInput);
        return new ResponseEntity<SearchSubsidyResultsResponse>(searchResults, HttpStatus.OK);
    }
    @PostMapping(
            value = "/add"
    )
    public String addSchemeDetails(@Valid @RequestBody SchemeDetailsRequest scheme) {
        return subsidySchemeService.addSubsidySchemeDetails(scheme);
    }

    @PostMapping(
            value = "/update"
    )
    public ResponseEntity<Object> updateSchemeDetails(@Valid @RequestBody SchemeDetailsRequest scheme) {
        return subsidySchemeService.updateSubsidySchemeDetails(scheme);
    }
    @GetMapping(
            value = "{scNumber}",
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SubsidyMeasureResponse> findSubsidyScheme(@PathVariable("scNumber") String scNumber) {
        log.info("Before calling findSubsidyScheme::{}");
        if (StringUtils.isEmpty(scNumber)) {
            throw new InvalidRequestException("Bad Request SC Number is null");
        }
        SubsidyMeasureResponse subsidySchemeById = subsidySchemeService.findSubsidySchemeById(scNumber);
        return new ResponseEntity<SubsidyMeasureResponse>(subsidySchemeById, HttpStatus.OK);
    }
}