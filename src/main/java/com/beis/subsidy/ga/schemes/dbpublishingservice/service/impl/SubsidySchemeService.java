package com.beis.subsidy.ga.schemes.dbpublishingservice.service.impl;

import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeDetailsRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeSearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SearchSubsidyResultsResponse;
import org.springframework.http.ResponseEntity;

public interface SubsidySchemeService {

    SearchSubsidyResultsResponse findMatchingSubsidySchemeDetails(SchemeSearchInput searchInput);

    String addSubsidySchemeDetails(SchemeDetailsRequest scheme);

    ResponseEntity<Object> updateSubsidySchemeDetails(SchemeDetailsRequest scheme);
}
