package com.beis.subsidy.ga.schemes.dbpublishingservice.service;

import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeDetailsRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeSearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SearchSubsidyResultsResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SubsidyMeasureResponse;

public interface SubsidySchemeService {

    SearchSubsidyResultsResponse findMatchingSubsidySchemeDetails(SchemeSearchInput searchInput);

    String addSubsidySchemeDetails(SchemeDetailsRequest scheme);

    String updateSubsidySchemeDetails(SchemeDetailsRequest scheme);

    SubsidyMeasureResponse findSubsidySchemeById(String scNumber);
}
