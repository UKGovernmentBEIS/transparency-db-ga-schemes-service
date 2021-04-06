package com.beis.subsidy.ga.schemes.dbpublishingservice.service.impl;

import static com.beis.subsidy.ga.schemes.dbpublishingservice.util.JsonFeignResponseUtil.toResponseEntity;

import com.beis.subsidy.ga.schemes.dbpublishingservice.controller.feign.GraphAPIFeignClient;
import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.AccessManagementException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.SearchResultNotFoundException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthorityRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.UsersGroupRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.GrantingAuthorityRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.AddGroupRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.GrantingAuthorityResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.GroupResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SearchResults;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.UserDetailsResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.service.GrantingAuthorityService;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.GrantingAuthSpecificationUtils;
import feign.FeignException;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GrantingAuthorityServiceImpl implements GrantingAuthorityService {

    @Autowired
    private GrantingAuthorityRepository gaRepository;

    @Autowired
    GraphAPIFeignClient graphAPIFeignClient;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Transactional
    public GrantingAuthority createGrantingAuthority(GrantingAuthorityRequest grantingAuthorityRequest,
                                                     String accessToken) {

        log.info("{}::inside createGrantingAuthority", loggingComponentName);

        String[] mailNickname = grantingAuthorityRequest.getName().split(" ");
        AddGroupRequest request = new AddGroupRequest(grantingAuthorityRequest.getName(),
                grantingAuthorityRequest.getName(), false, mailNickname[0], true);
        GroupResponse response = addGroup(accessToken, request);
        //
        if(response==null || response.getId()==null) {
            throw new AccessManagementException(HttpStatus.INTERNAL_SERVER_ERROR, "Create Group id is null");
        }
        GrantingAuthority grantingAuthority = new GrantingAuthority(null, grantingAuthorityRequest.getName(),
        		grantingAuthorityRequest.getUserName(), "SYSTEM", "Active", response.getId(),grantingAuthorityRequest.getName()
                , LocalDateTime.now(), LocalDateTime.now());

        GrantingAuthority savedAwards = gaRepository.save(grantingAuthority);
        log.info("{}:: End of createGrantingAuthority ", loggingComponentName);

        return savedAwards;

    }

    @Transactional
    public GrantingAuthority updateGrantingAuthority(GrantingAuthorityRequest grantingAuthorityRequest, Long gaNumber,String accessToken) {
            log.info("{}::inside updateGrantingAuthority", loggingComponentName);

            GrantingAuthority grantingAuthority = new GrantingAuthority(gaNumber, grantingAuthorityRequest.getName(),
                    "SYSTEM", "SYSTEM", "Active", null,
                    grantingAuthorityRequest.getName(),LocalDateTime.now(), LocalDateTime.now());

            GrantingAuthority savedAwards = gaRepository.save(grantingAuthority);
            log.info("{}::End of updateGrantingAuthority", loggingComponentName);
            return savedAwards;
    }

    @Transactional
    public UserDetailsResponse deActivateGrantingAuthority(String azGrpId, String accessToken) {
        log.info("{}::inside deActivateGrantingAuthority", loggingComponentName);
        UserDetailsResponse userDetailsResponse=null;
        userDetailsResponse= getUserDetailsByGrpId(accessToken,azGrpId);
        log.info("{}::End Of deActivateGrantingAuthority", loggingComponentName);
        return userDetailsResponse;

    }

    /**
     * To return matching Granting Authority based on search inputs
     *
     * @param searchInput
     *            - Search input object, that contains search criteria
     * @return SearchResults - Returns search result based on search criteria
     */

    public SearchResults findMatchingGrantingAuthorities(SearchInput searchInput) {

        Specification<GrantingAuthority> gaSpecifications = null;
        List<GrantingAuthority> totalGaList;
        boolean isValid = false;

        if (!StringUtils.isEmpty(searchInput.getGrantingAuthorityID()) ||
                !StringUtils.isEmpty(searchInput.getGrantingAuthorityName())||
                !StringUtils.isEmpty(searchInput.getStatus())) {
            gaSpecifications = getSpecificationGADetails(searchInput);
            isValid = true;
        }

        List<Sort.Order> orders = getOrderByCondition(searchInput.getSortBy());

        Pageable pagingSortAwards = PageRequest.of(searchInput.getPageNumber() - 1,
                searchInput.getTotalRecordsPerPage(), Sort.by(orders));

        Page<GrantingAuthority> pageGrantingAuthorities = gaRepository.findAll(gaSpecifications, pagingSortAwards);

        List<GrantingAuthority> gaResults = pageGrantingAuthorities.getContent();
        int inActive = 0;
        int active = 0;

        if(isValid){
            totalGaList = gaRepository.findAll(gaSpecifications);
        } else {
            totalGaList = gaRepository.findAll();
        }

        for (GrantingAuthority grantingAuthority : totalGaList) {

            if (grantingAuthority.getStatus().equalsIgnoreCase("Inactive")) {
                inActive++;

            } else if (grantingAuthority.getStatus().equalsIgnoreCase("Active")) {
                active++;
            }
        }

        SearchResults searchResults = null;

        if (!gaResults.isEmpty()) {

            searchResults = new SearchResults(gaResults, pageGrantingAuthorities.getTotalElements(),
                    pageGrantingAuthorities.getNumber() + 1, pageGrantingAuthorities.getTotalPages(), active, inActive);
        } else {
            throw new SearchResultNotFoundException("grantingAuthority Results NotFound");
        }
        return searchResults;
    }

    public GrantingAuthorityResponse findByGrantingAuthorityId(Long gaId) {

        GrantingAuthority grantingAuthority = gaRepository.findBygaId(gaId);
        if (grantingAuthority == null) {
            throw new SearchResultNotFoundException("grantingAuthority Results NotFound");
        }
        return new GrantingAuthorityResponse(grantingAuthority, true);
    }

    /**
     *
     * @param sortBy
     *            - Array of string with format "field, direction" - for example,
     *            "gaName, asc"
     * @return List<Order> - List of order
     */
    private List<Sort.Order> getOrderByCondition(String[] sortBy) {

        List<Sort.Order> orders = new ArrayList<Sort.Order>();

        if (sortBy != null && sortBy.length > 0 && sortBy[0].contains(",")) {
            // will sort more than 2 fields
            // sortOrder="field, direction"
            for (String sortOrder : sortBy) {
                String[] _sort = sortOrder.split(",");
                orders.add(new Sort.Order(getSortDirection(_sort[1]), _sort[0]));
            }
        } else {
            // Default sort - Legal Granting Date with recent one at top
            orders.add(new Sort.Order(getSortDirection("desc"), "gaId"));
        }

        return orders;
    }

    /**
     *
     * @param direction
     *            - direction of sort
     * @return Sort.Direction - sort direction
     */
    private Sort.Direction getSortDirection(String direction) {
        Sort.Direction sortDir = Sort.Direction.ASC;
        if (direction.equals("desc")) {
            sortDir = Sort.Direction.DESC;
        }
        return sortDir;
    }

    public Specification<GrantingAuthority> getSpecificationGADetails(SearchInput searchinput) {
        Specification<GrantingAuthority> awardSpecifications = Specification

                // getGrantingAuthorityID from input parameter
                .where(StringUtils.isEmpty(searchinput.getGrantingAuthorityID())  ? null
                        : GrantingAuthSpecificationUtils
                        .grantingAuthorityId(Long.valueOf(searchinput.getGrantingAuthorityID())))

                // getGrantingAuthorityName from input parameter
                .and(StringUtils.isEmpty(searchinput.getGrantingAuthorityName()) ? null
                        : GrantingAuthSpecificationUtils.grantingAuthorityName(searchinput.getGrantingAuthorityName()))

                .and(StringUtils.isEmpty(searchinput.getStatus()) ? null
                        : GrantingAuthSpecificationUtils.status(searchinput.getStatus()));

        return awardSpecifications;
    }

    /**
     *
     * @param token
     * @param request
     * @return
     */
    public GroupResponse addGroup(String token, AddGroupRequest request) {
        Response response = null;

        GroupResponse groupResponse;
        Object clazz;
        try {
            response = graphAPIFeignClient.addGroup("Bearer " + token, request);
            log.info("{}::  Graph Api status  {}", loggingComponentName, response.status());

            if (response.status() == 201) {
                clazz = GroupResponse.class;
                ResponseEntity<Object> responseResponseEntity = toResponseEntity(response, clazz);
                groupResponse = (GroupResponse) responseResponseEntity.getBody();

            } else if (response.status() == 400 || response.status() == 417  ) {
                throw new AccessManagementException(HttpStatus.valueOf(response.status()),
                        "create group request is invalid or group already exist");
            } else {
                log.error("{}:: Graph Api failed:: status code {}", loggingComponentName, 500);
                throw new AccessManagementException(HttpStatus.valueOf(500), "Create group Graph Api Failed");
            }

        } catch (FeignException ex) {
            log.error("{}:: Graph Api failed:: status code {} & message {}",
                    loggingComponentName, ex.status(),
                    ex.getMessage());
            throw new AccessManagementException(HttpStatus.valueOf(ex.status()), "Graph Api failed");
        }
        return groupResponse;
    }

    /**
     *
     * @param token
     * @return
     */
    public int deleteGroup(String token, String groupId) {
        Response response = null;
        GroupResponse groupResponse;
        Object clazz;
        int status;
        try {
            response = graphAPIFeignClient.deleteGroup("Bearer " + token, groupId);
            log.info("{}:: Graph Api deleteGroup status  {}", loggingComponentName, response.status());

            if (response.status() == 204) {
                status = 204;

            } else {
                status = response.status();
                log.info("{}:: Graph Api failed:: status code {}", loggingComponentName, response.status());

            }

        } catch (FeignException ex) {
            log.error("{}:: Delete Group Graph Api failed:: status code {} & message {}",
                    loggingComponentName, ex.status(),
                    ex.getMessage());
            throw new AccessManagementException(HttpStatus.valueOf(ex.status()), "Delete group Graph Api failed");
        }
        return status;
    }


    public GrantingAuthority deleteUser(String token, UsersGroupRequest usersGroupRequest, String azGrpId) {
        Response response = null;
        boolean isInValid = false;
        GrantingAuthority grantingAuthority = null;
        try {
            List<String> userIds=usersGroupRequest.getUserIds();
            if (userIds != null && userIds.size() > 0) {
                for (String userId : userIds) {
                    response = graphAPIFeignClient.deleteUser("Bearer " + token, userId);
                    if (response.status() != 204) {
                        isInValid = true;
                        break;
                    }
                }
            }

            if (isInValid) {
                throw new AccessManagementException(HttpStatus.valueOf(response.status()),
                        "unable to delete the user profile");
            }
            int groupResponse = deleteGroup( token, azGrpId);
            if (groupResponse == 204) {

                GrantingAuthority grantingAuthResp= gaRepository.findByAzureGroupId(azGrpId);
                grantingAuthority = new GrantingAuthority(grantingAuthResp.getGaId(), grantingAuthResp.getGrantingAuthorityName(),
                        "SYSTEM", "SYSTEM", "Inactive", azGrpId,
                        grantingAuthResp.getAzureGroupName(),LocalDateTime.now(), LocalDateTime.now());
                GrantingAuthority savedAwards = gaRepository.save(grantingAuthority);
                log.info("{}:: GrantingAuthority saved successfully", loggingComponentName);
            }
            return grantingAuthority;

        } catch (FeignException ex) {
            log.error("{}:: Graph Api failed:: status code {} & message {}",
                    loggingComponentName, ex.status(),
                    ex.getMessage());
            throw new AccessManagementException(HttpStatus.valueOf(ex.status()),
                    "Delete User Graph Api failed");
        }

    }

    public UserDetailsResponse getUserDetailsByGrpId(String token, String groupId) {
        // Graph API call.
        UserDetailsResponse userDetailsResponse = null;
        Response response = null;
        Object clazz;
        try {
            log.info("{}::before calling toGraph getUserDetailsByGrpId  Api is",loggingComponentName);
            response = graphAPIFeignClient.getUsersByGroupId("Bearer " + token,groupId);
            if (response.status() == 200) {
                clazz = UserDetailsResponse.class;
                ResponseEntity<Object> responseResponseEntity =  toResponseEntity(response, clazz);
                userDetailsResponse
                        = (UserDetailsResponse) responseResponseEntity.getBody();


            } else if (response.status() == 404) {
                throw new SearchResultNotFoundException("Group Id not found");
            } else {
                log.error("get user details by groupId Graph Api is failed ::{}",response.status());
                throw new AccessManagementException(HttpStatus.valueOf(response.status()),
                        "Graph Api failed");
            }

        } catch (FeignException ex) {
            log.error("{}:: get  groupId Graph Api is failed:: status code {} & message {}",
                    loggingComponentName, ex.status(), ex.getMessage());
            throw new AccessManagementException(HttpStatus.valueOf(ex.status()), "Graph Api failed");
        }
        return userDetailsResponse;
    }

}
