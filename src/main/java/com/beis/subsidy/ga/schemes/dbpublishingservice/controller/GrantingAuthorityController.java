package com.beis.subsidy.ga.schemes.dbpublishingservice.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.beis.subsidy.ga.schemes.dbpublishingservice.controller.feign.GraphAPILoginFeignClient;
import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.AccessTokenException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.InvalidRequestException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthorityRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.UsersGroupRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SearchResults;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.UserDetailsResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.ValidationResult;
import com.beis.subsidy.ga.schemes.dbpublishingservice.service.AccessTokenResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.service.GrantingAuthorityService;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@RestController
public class GrantingAuthorityController {

	@Autowired
	public GrantingAuthorityService grantingAuthorityService;
	
	@Autowired
	GraphAPILoginFeignClient graphAPILoginFeignClient;

	static final String BEARER = "Bearer ";
	    
	@Autowired
	Environment environment;
	    
	/**
	 * To get Granting AUthority as input from UI and return Validation results based on input.
	 * 
	 * @param gaInputRequest
	 *            - Input as SearchInput object from front end
	 * @return ResponseEntity - Return response status and description
	 */
	@PostMapping("grantingAuthority")
	public ResponseEntity<ValidationResult> addGrantingAuthority(@Valid @RequestBody GrantingAuthorityRequest
																			 gaInputRequest) {

		try {
			log.info("Before calling add addGrantingAuthority::::");
			if(gaInputRequest==null) {
				throw new InvalidRequestException("Invalid Request");
			}
			String accessToken=getBearerToken();
			log.info("after GrantingAuthority accessToken ::::");
			ValidationResult validationResult = new ValidationResult();
			GrantingAuthority grantingAuthority = grantingAuthorityService.createGrantingAuthority(gaInputRequest,accessToken);
			
			validationResult.setMessage("gaId: " + grantingAuthority.getGaId());

			return ResponseEntity.status(HttpStatus.OK).body(validationResult);
		} catch (Exception e) {

			// 2.0 - CatchException and return validation errors
			ValidationResult validationResult = new ValidationResult();

			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(validationResult);
		}

	}
	
	/**
	 * get the Granting Authority as input from UI and update the same in
	 * DBand  return Validation results based on input.
	 * 
	 *   - Input as SearchInput object from front end
	 * @return ResponseEntity - Return response status and description
	 */
	@PutMapping(
			value="grantingAuthority/{gaNumber}"
		
			)
	public ResponseEntity<ValidationResult> updateGrantingAuthority(@Valid @RequestBody GrantingAuthorityRequest gaInputRequest
			,@PathVariable("gaNumber") Long gaNumber) {

		try {
			log.info("{}::Before calling updateGrantingAuthority award");

			if(gaInputRequest==null) {
				throw new Exception("gaInputRequest is empty");
			}
			ValidationResult validationResult = new ValidationResult();
			String accessToken=getBearerToken();
			GrantingAuthority grantingAuthority = grantingAuthorityService.updateGrantingAuthority(gaInputRequest,gaNumber,accessToken);
			
			validationResult.setMessage(grantingAuthority.getGaId()+ " updated successfully");

			return ResponseEntity.status(HttpStatus.OK).body(validationResult);
		} catch (Exception e) {

			// 2.0 - CatchException and return validation errors
			ValidationResult validationResult = new ValidationResult();

			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(validationResult);
		}

	}
	
	/**
	 * get the Granting Authority as input from UI and update the same in DBand
	 * return Validation results based on input.
	 * 
	 * @return ResponseEntity - Return response status and description
	 */
	@GetMapping(
			value="grantingAuthority/{azGrpId}"
			)
	public ResponseEntity<UserDetailsResponse> deActivateGrantingAuthority(@PathVariable("azGrpId") String azGrpId) {

		try {
			log.info("Before calling deActivateGrantingAuthority::::");
			if(azGrpId==null) {
				throw new InvalidRequestException("deActivateGrantingAuthority request is empty");
			}
			String accessToken=getBearerToken();
			
			UserDetailsResponse userDetailsResponse  = grantingAuthorityService
					.deActivateGrantingAuthority(azGrpId,accessToken);
			
			return new ResponseEntity<UserDetailsResponse>(userDetailsResponse, HttpStatus.OK);
		} catch (Exception e) {

			// 2.0 - CatchException and return validation errors
			ValidationResult validationResult = new ValidationResult();

			return new ResponseEntity<UserDetailsResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	/**
	 * To get search input from UI and return search results based on search criteria
	 * 
	 * @param searchInput - Input as SearchInput object from front end 
	 * @return ResponseEntity - Return response status and description
	 */
	
	@PostMapping("searchGrantingAuthority")
	public ResponseEntity<SearchResults> findSearchResults(@Valid @RequestBody SearchInput searchInput) {

			//Set Default Page records
			if(searchInput.getTotalRecordsPerPage() == 0) {
				searchInput.setTotalRecordsPerPage(10);
			}
			SearchResults searchResults = grantingAuthorityService.findMatchingGrantingAuthorities(searchInput);
			
			return new ResponseEntity<SearchResults>(searchResults, HttpStatus.OK);
	}
	/**
	 * 
	 * @return
	 * @throws AccessTokenException
	 */
	public String getBearerToken() throws AccessTokenException {

		log.info("inside getBearerToken method::{}", environment);
		
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

		map.add("grant_type", "client_credentials");
		map.add("client_id", environment.getProperty("client-Id"));
		map.add("client_secret", environment.getProperty("client-secret"));
		map.add("scope", environment.getProperty("graph-api-scope"));

		AccessTokenResponse openIdTokenResponse = graphAPILoginFeignClient
				.getAccessIdToken(environment.getProperty("tenant-id"), map);


		if (openIdTokenResponse == null) {
			throw new AccessTokenException(HttpStatus.valueOf(500),
					"Graph Api Service Failed while bearer token generate");
		}
		log.warn(" after access token " + openIdTokenResponse.getAccessToken());
		return openIdTokenResponse.getAccessToken();
	}
	
	@DeleteMapping(
			value="group/{azGrpId}")
	public ResponseEntity<ValidationResult> deleteUsersGroup(@PathVariable("azGrpId") String azGrpId,
												 @RequestBody UsersGroupRequest usersGroupRequest)
	{
		try {
			log.info("before calling delete UsersGroup::::");
			if(Objects.isNull(usersGroupRequest)|| StringUtils.isEmpty(azGrpId)) {
				throw new InvalidRequestException("usersGroupRequest is empty");
			}
			ValidationResult validationResult = new ValidationResult();
			String accessToken=getBearerToken();
			GrantingAuthority grantingAuthority = grantingAuthorityService.deleteUser(accessToken, usersGroupRequest, azGrpId);
			
			validationResult.setMessage(grantingAuthority.getGaId() + " deActivated  successfully");

			return ResponseEntity.status(HttpStatus.OK).body(validationResult);
		} catch (Exception e) {

			ValidationResult validationResult = new ValidationResult();
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(validationResult);
		}
	}
}