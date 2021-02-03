package com.beis.subsidy.ga.schemes.dbpublishingservice.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.SearchResultNotFoundException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthorityRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.GrantingAuthorityRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.GrantingAuthorityResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SearchResults;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.GrantingAuthSpecificationUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GrantingAuthorityService {

	@Autowired
	private GrantingAuthorityRepository gaRepository;

	@Transactional
	public GrantingAuthority createGrantingAuthority(GrantingAuthorityRequest grantingAuthorityRequest) {
		try {
			log.info("inside createGrantingAuthority ");

			//micro graph api =grantingauthoid call create gropup api
			GrantingAuthority grantingAuthority = new GrantingAuthority(null, grantingAuthorityRequest.getName(),
					"SYSTEM", "SYSTEM", "Active", LocalDate.now(), LocalDate.now());

			GrantingAuthority savedAwards = gaRepository.save(grantingAuthority);
			log.info("End createGrantingAuthority ");

			return savedAwards;
		} catch (Exception serviceException) {
			log.info("serviceException occured::" + serviceException.getMessage());
			return null;
		}
	}

	@Transactional
	public GrantingAuthority updateGrantingAuthority(GrantingAuthorityRequest grantingAuthorityRequest) {
		try {
			log.info("inside createGrantingAuthority ");

			GrantingAuthority grantingAuthority = new GrantingAuthority(
					Long.valueOf(grantingAuthorityRequest.getGaNumber()), grantingAuthorityRequest.getName(), "SYSTEM",
					"SYSTEM", "Active", LocalDate.now(), LocalDate.now());

			GrantingAuthority savedAwards = gaRepository.save(grantingAuthority);
			log.info("End createGrantingAuthority ");

			return savedAwards;

		} catch (Exception serviceException) {
			log.info("serviceException occured::" + serviceException.getMessage());
			return null;
		}
	}

	
	@Transactional
	public GrantingAuthority deActivateGrantingAuthority(GrantingAuthorityRequest grantingAuthorityRequest) {
		try {
			log.info("inside createGrantingAuthority ");

			GrantingAuthority grantingAuthority = new GrantingAuthority(
					Long.valueOf(grantingAuthorityRequest.getGaNumber()), grantingAuthorityRequest.getName(), "SYSTEM",
					"SYSTEM", "Inactive", LocalDate.now(), LocalDate.now());

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
		int inActive=0;
		int active=0;
				
		for (GrantingAuthority grantingAuthority : gaResults) {
			
			if(grantingAuthority.getStatus().equalsIgnoreCase("Inactive")){
				inActive++;
				
			}if(grantingAuthority.getStatus().equalsIgnoreCase("Active")) {
				active++;
			}
		}

		SearchResults searchResults = null;

		if (!gaResults.isEmpty()) {

			searchResults = new SearchResults(gaResults, pageGrantingAuthorities.getTotalElements(), pageGrantingAuthorities.getNumber() + 1,
					pageGrantingAuthorities.getTotalPages(),active,inActive);
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

public Specification<GrantingAuthority>  getSpecificationGADetails(SearchInput searchinput) {
	Specification<GrantingAuthority> awardSpecifications = Specification

			// getGrantingAuthorityID from input parameter
			.where(searchinput.getGrantingAuthorityID() == null || searchinput.getGrantingAuthorityID().isEmpty()
			? null : GrantingAuthSpecificationUtils.grantingAuthorityId(Long.valueOf(searchinput.getGrantingAuthorityID())))


			// getGrantingAuthorityName from input parameter
			.and(searchinput.getGrantingAuthorityName() == null || searchinput.getGrantingAuthorityName().isEmpty()
			? null : GrantingAuthSpecificationUtils.grantingAuthorityName(searchinput.getGrantingAuthorityName()));


			return awardSpecifications;
}

	public List<GrantingAuthority> getAllGrantingAuthorities() {
		return gaRepository.findAll();
	}

}
