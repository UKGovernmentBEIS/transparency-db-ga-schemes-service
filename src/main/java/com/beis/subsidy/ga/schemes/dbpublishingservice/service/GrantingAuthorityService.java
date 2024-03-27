package com.beis.subsidy.ga.schemes.dbpublishingservice.service;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthorityRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.UsersGroupRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.AddGroupRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.GrantingAuthorityResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SearchResults;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.UserDetailsResponse;

public interface GrantingAuthorityService {

    GrantingAuthority createGrantingAuthority(GrantingAuthorityRequest grantingAuthorityRequest,
                                              String accessToken);
    GrantingAuthority updateGrantingAuthority(GrantingAuthorityRequest grantingAuthorityRequest,
                                              Long gaNumber,String accessToken);
    UserDetailsResponse deActivateGrantingAuthority(String azGrpId, String accessToken);

    SearchResults<GrantingAuthorityResponse> findMatchingGrantingAuthorities(SearchInput searchInput);

    GrantingAuthority deleteUser(String token, UsersGroupRequest usersGroupRequest, String azGrpId);

    int getGroupCountByName(String token, AddGroupRequest request);
}
