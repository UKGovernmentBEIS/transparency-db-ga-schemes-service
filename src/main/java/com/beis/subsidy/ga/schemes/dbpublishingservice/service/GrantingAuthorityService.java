package com.beis.subsidy.ga.schemes.dbpublishingservice.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.beis.subsidy.ga.schemes.dbpublishingservice.controller.feign.GraphAPIFeignClient;
import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.AccessManagementException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.InvalidRequestException;
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
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.GrantingAuthSpecificationUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;
import feign.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GrantingAuthorityService {

	@Autowired
	private GrantingAuthorityRepository gaRepository;

	private static final ObjectMapper json = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Autowired
	GraphAPIFeignClient graphAPIFeignClient;

	@Value("${loggingComponentName}")
	private String loggingComponentName;

	@Transactional
	public GrantingAuthority createGrantingAuthority(GrantingAuthorityRequest grantingAuthorityRequest,
			String accessToken) {
		
			log.info("inside createGrantingAuthority ");

			AddGroupRequest request = new AddGroupRequest(grantingAuthorityRequest.getName(),
					grantingAuthorityRequest.getName(), false, grantingAuthorityRequest.getName(), true);
			GroupResponse response = addGroup(accessToken, request);
			//
			if(response==null || response.getId()==null) {
				throw new AccessManagementException(HttpStatus.INTERNAL_SERVER_ERROR, "Create Group id is null");
			}
			GrantingAuthority grantingAuthority = new GrantingAuthority(null, grantingAuthorityRequest.getName(),
					"SYSTEM", "SYSTEM", "Active", response.getId(),grantingAuthorityRequest.getAz_group_name(), LocalDate.now(),
					LocalDate.now());

			GrantingAuthority savedAwards = gaRepository.save(grantingAuthority);
			log.info("End createGrantingAuthority ");

			return savedAwards;
		
	}

	@Transactional
	public GrantingAuthority updateGrantingAuthority(GrantingAuthorityRequest grantingAuthorityRequest, Long gaNumber,String accessToken) {
		try {
			log.info("inside createGrantingAuthority ");

			//GroupResponse response = addGroup(accessToken, request);
			GrantingAuthority grantingAuthority = new GrantingAuthority(gaNumber, grantingAuthorityRequest.getName(),
					"SYSTEM", "SYSTEM", "Active", null, grantingAuthorityRequest.getAz_group_name(),LocalDate.now(), LocalDate.now());

			GrantingAuthority savedAwards = gaRepository.save(grantingAuthority);
			log.info("End createGrantingAuthority ");

			return savedAwards;

		} catch (Exception serviceException) {
			log.info("serviceException occured::" + serviceException.getMessage());
			return null;
		}
	}

	@Transactional
	public UserDetailsResponse deActivateGrantingAuthority(GrantingAuthorityRequest grantingAuthorityRequest,
			Long gaNumber,String accessToken) {
		try {
			log.info("inside deActivateGrantingAuthority ");
			UserDetailsResponse userDetailsResponse=null;
			GrantingAuthority		grantingAuthorityById =gaRepository.findBygaId(gaNumber);
			if(grantingAuthorityById!=null) {
			 userDetailsResponse= getUserRolesByGrpId(accessToken,grantingAuthorityById.getAzureGroupId());
			}
			
		
			log.info("End createGrantingAuthority ");

			return userDetailsResponse;

		} catch (Exception serviceException) {
			log.info("serviceException occured::" + serviceException.getMessage());
			return null;
		}
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

		if (searchInput.getGrantingAuthorityID() != null || searchInput.getGrantingAuthorityName() != null || searchInput.getStatus()!=null) {
			gaSpecifications = getSpecificationGADetails(searchInput);
		}

		List<Order> orders = getOrderByCondition(searchInput.getSortBy());

		Pageable pagingSortAwards = PageRequest.of(searchInput.getPageNumber() - 1,
				searchInput.getTotalRecordsPerPage(), Sort.by(orders));

		Page<GrantingAuthority> pageGrantingAuthorities = gaRepository.findAll(gaSpecifications, pagingSortAwards);

		List<GrantingAuthority> gaResults = pageGrantingAuthorities.getContent();
		int inActive = 0;
		int active = 0;

		for (GrantingAuthority grantingAuthority : gaResults) {

			if (grantingAuthority.getStatus().equalsIgnoreCase("Inactive")) {
				inActive++;

			}
			if (grantingAuthority.getStatus().equalsIgnoreCase("Active")) {
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
	private List<Order> getOrderByCondition(String[] sortBy) {

		List<Order> orders = new ArrayList<Order>();

		if (sortBy != null && sortBy.length > 0 && sortBy[0].contains(",")) {
			// will sort more than 2 fields
			// sortOrder="field, direction"
			for (String sortOrder : sortBy) {
				String[] _sort = sortOrder.split(",");
				orders.add(new Order(getSortDirection(_sort[1]), _sort[0]));
			}
		} else {
			// Default sort - Legal Granting Date with recent one at top
			orders.add(new Order(getSortDirection("desc"), "gaId"));
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
				.where(searchinput.getGrantingAuthorityID() == null || searchinput.getGrantingAuthorityID().isEmpty()
						? null
						: GrantingAuthSpecificationUtils
								.grantingAuthorityId(Long.valueOf(searchinput.getGrantingAuthorityID())))

				// getGrantingAuthorityName from input parameter
				.and(searchinput.getGrantingAuthorityName() == null || searchinput.getGrantingAuthorityName().isEmpty()
						? null
						: GrantingAuthSpecificationUtils.grantingAuthorityName(searchinput.getGrantingAuthorityName()))
						
						.and(searchinput.getStatus() == null || searchinput.getStatus().isEmpty()
						? null
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
			long time1 = System.currentTimeMillis();
			response = graphAPIFeignClient.addGroup("Bearer " + token, request);
			log.info("{}:: Time taken to call Graph Api is {}", loggingComponentName,
					(System.currentTimeMillis() - time1));
			log.info("{}:: Graph Api status  {}", response.status());

			if (response.status() == 201) {
				clazz = GroupResponse.class;
				ResponseEntity<Object> responseResponseEntity = toResponseEntity(response, clazz);
				groupResponse = (GroupResponse) responseResponseEntity.getBody();
				
			} else if (response.status() == 400) {
				throw new InvalidRequestException("create user request is invalid");
			} else {
				log.error("{}:: Graph Api failed:: status code {}", loggingComponentName, 500);
				throw new AccessManagementException(HttpStatus.valueOf(500), "Create User Graph Api Failed");
			}

		} catch (FeignException ex) {
			log.error("{}:: Graph Api failed:: status code {} & message {}", loggingComponentName, ex.status(),
					ex.getMessage());
			throw new AccessManagementException(HttpStatus.valueOf(ex.status()), "Graph Api failed");
		}
		return groupResponse;
	}

	public List<GrantingAuthority> getAllGrantingAuthorities() {
		return gaRepository.findAll();
	}

	public static ResponseEntity<Object> toResponseEntity(Response response, Object clazz) {
		Optional<Object> payload = decode(response, clazz);

		return new ResponseEntity<>(payload.orElse(null), convertHeaders(response.headers()),
				HttpStatus.valueOf(response.status()));
	}

	public static MultiValueMap<String, String> convertHeaders(Map<String, Collection<String>> responseHeaders) {
		MultiValueMap<String, String> responseEntityHeaders = new LinkedMultiValueMap<>();
		responseHeaders.entrySet().stream().forEach(e -> {
			if (!(e.getKey().equalsIgnoreCase("request-context") || e.getKey().equalsIgnoreCase("x-powered-by")
					|| e.getKey().equalsIgnoreCase("content-length"))) {
				responseEntityHeaders.put(e.getKey(), new ArrayList<>(e.getValue()));
			}
		});

		return responseEntityHeaders;
	}

	public static Optional<Object> decode(Response response, Object clazz) {
		try {
			return Optional
					.of(json.readValue(response.body().asReader(Charset.defaultCharset()), (Class<Object>) clazz));
		} catch (IOException e) {
			return Optional.empty();
		}
	}

	/**
	 * 
	 * @param token
	 * @param request
	 * @return
	 */
	public GroupResponse deleteGroup(String token, String groupId) {
		Response response = null;

		GroupResponse groupResponse;
		Object clazz;
		try {
			long time1 = System.currentTimeMillis();
			response = graphAPIFeignClient.deleteGroup("Bearer " + token, groupId);
			log.info("{}:: Time taken to call Graph Api is {}", loggingComponentName,
					(System.currentTimeMillis() - time1));
			log.info("{}:: Graph Api status  {}", response.status());

			if (response.status() == 204) {
				clazz = GroupResponse.class;
				ResponseEntity<Object> responseResponseEntity = toResponseEntity(response, clazz);
				groupResponse = (GroupResponse) responseResponseEntity.getBody();
				
			} else if (response.status() == 400) {
				throw new InvalidRequestException("create user request is invalid");
			} else {
				log.error("{}:: Graph Api failed:: status code {}", loggingComponentName, 500);
				throw new AccessManagementException(HttpStatus.valueOf(500), "Create User Graph Api Failed");
			}

		} catch (FeignException ex) {
			log.error("{}:: Graph Api failed:: status code {} & message {}", loggingComponentName, ex.status(),
					ex.getMessage());
			throw new AccessManagementException(HttpStatus.valueOf(ex.status()), "Delete group Graph Api failed");
		}
		return groupResponse;
	}	
	

	public GrantingAuthority deleteUser(String token, UsersGroupRequest usersGroupRequest,long gaNumber) {
		Response response = null;
		int status = 0;
		try {
			List<String> userIds=usersGroupRequest.getUserId();
			long time1 = System.currentTimeMillis();
			for (String userId : userIds) {
				response = graphAPIFeignClient.deleteUser("Bearer " + token, userId);
			}
			GrantingAuthority		grantingAuthorityById =gaRepository.findBygaId(gaNumber);
			deleteGroup("Bearer " + token, grantingAuthorityById.getAzureGroupId());
			log.info("{}:: Time taken to call Graph Api deleteUser is {}", loggingComponentName,
					(System.currentTimeMillis() - time1));

			if (response.status() == 204) {
				status = response.status();
			} else {
				throw new AccessManagementException(HttpStatus.valueOf(response.status()),
						"unable to delete the user profile");
			}
			
			
			GrantingAuthority grantingAuthority = new GrantingAuthority(gaNumber, usersGroupRequest.getName(),
					"SYSTEM", "SYSTEM", "Inactive", null, grantingAuthorityById.getAzureGroupName(),LocalDate.now(), LocalDate.now());

			GrantingAuthority savedAwards = gaRepository.save(grantingAuthority);
			log.info("End createGrantingAuthority ");

			return grantingAuthority;

		} catch (FeignException ex) {
			log.error("{}:: Graph Api failed:: status code {} & message {}", loggingComponentName, ex.status(),
					ex.getMessage());
			throw new AccessManagementException(HttpStatus.valueOf(ex.status()), "Delete User Graph Api failed");
		}
		
	}
	
	 public UserDetailsResponse getUserRolesByGrpId(String token, String groupId) {
	        // Graph API call.
	        UserDetailsResponse userDetailsResponse = null;
	        Response response = null;
	        Object clazz;
	        try {
	            long time1 = System.currentTimeMillis();
	            log.info("{}::before calling toGraph deActivateGrantingAuthority  Api is",loggingComponentName);
	            response = graphAPIFeignClient.getUsersByGroupId("Bearer " + token,groupId);
	            log.info("{}:: Time taken to call Graph Api is {}", loggingComponentName, (System.currentTimeMillis() - time1));

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
