package com.beis.subsidy.ga.schemes.dbpublishingservice.service.impl;

import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeDetailsRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeSearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SearchSubsidyResultsResponse;
import org.springframework.http.ResponseEntity;

public interface SubsidySchemeService {

/*
    SearchResults findBEISAdminDashboardData(UserPrinciple userPrincipleObj);

    SearchResults findGAAdminDashboardData(UserPrinciple userPrincipleObj);

    ResponseEntity<Object> updateAwardDetailsByAwardId(Long awardId, UpdateAwardDetailsRequest awardUpdateRequest);

    SearchResults findGAApproverDashboardData(UserPrinciple userPrincipleObj);

    SearchResults findGAEncoderDashboardData(UserPrinciple userPrincipleObj);
    List<GrantingAuthorityResponse> getAllGA();
*/

    SearchSubsidyResultsResponse findMatchingSubsidySchemeDetails(SchemeSearchInput searchInput);

    ResponseEntity<Object> addSubsidySchemeDetails(SchemeDetailsRequest scheme);

    ResponseEntity<Object> updateSubsidySchemeDetails(SchemeDetailsRequest scheme);
}
