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
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.GrantingAuthorityRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.AddGroupRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.GrantingAuthorityResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.GroupResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SearchResults;
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
					"SYSTEM", "SYSTEM", "Active", response.getId(), LocalDate.now(),
					LocalDate.now());

			GrantingAuthority savedAwards = gaRepository.save(grantingAuthority);
			log.info("End createGrantingAuthority ");

			return savedAwards;
		
	}

	@Transactional
	public GrantingAuthority updateGrantingAuthority(GrantingAuthorityRequest grantingAuthorityRequest, Long gaNumber) {
		try {
			log.info("inside createGrantingAuthority ");

			GrantingAuthority grantingAuthority = new GrantingAuthority(gaNumber, grantingAuthorityRequest.getName(),
					"SYSTEM", "SYSTEM", "Active", null, LocalDate.now(), LocalDate.now());

			GrantingAuthority savedAwards = gaRepository.save(grantingAuthority);
			log.info("End createGrantingAuthority ");

			return savedAwards;

		} catch (Exception serviceException) {
			log.info("serviceException occured::" + serviceException.getMessage());
			return null;
		}
	}

	@Transactional
	public GrantingAuthority deActivateGrantingAuthority(GrantingAuthorityRequest grantingAuthorityRequest,
			Long gaNumber) {
		try {
			log.info("inside createGrantingAuthority ");

			GrantingAuthority grantingAuthority = new GrantingAuthority(gaNumber, grantingAuthorityRequest.getName(),
					"SYSTEM", "SYSTEM", "Inactive", null, LocalDate.now(), LocalDate.now());

			GrantingAuthority savedAwards = gaRepository.save(grantingAuthority);
			log.info("End createGrantingAuthority ");

			return savedAwards;

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

		if (searchInput.getGrantingAuthorityID() != null || searchInput.getGrantingAuthorityName() != null) {
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
						: GrantingAuthSpecificationUtils.grantingAuthorityName(searchinput.getGrantingAuthorityName()));

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

}
