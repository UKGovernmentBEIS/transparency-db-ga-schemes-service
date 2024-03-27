package com.beis.subsidy.ga.schemes.dbpublishingservice.service;

import com.beis.subsidy.ga.schemes.dbpublishingservice.request.AwardSearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeDetailsRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeSearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.AwardResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SearchResults;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SearchSubsidyResultsResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SubsidyMeasureResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.UserPrinciple;

public interface SubsidySchemeService {

    SearchSubsidyResultsResponse findMatchingSubsidySchemeDetails(SchemeSearchInput searchInput, UserPrinciple userPrinicipleResp);

    String addSubsidySchemeDetails(SchemeDetailsRequest scheme);

    String updateSubsidySchemeDetails(SchemeDetailsRequest schemeReq, String scNumber, UserPrinciple userPrinciple);

    SubsidyMeasureResponse findSubsidySchemeById(String scNumber);
    SubsidyMeasureResponse findSubsidySchemeWithAwardsById(String scNumber, AwardSearchInput awardSearchInput);

}
