package com.beis.subsidy.ga.schemes.dbpublishingservice.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
	 * @param searchInput
	 *            - Input as SearchInput object from front end
	 * @return ResponseEntity - Return response status and description
	 */
	@PostMapping("grantingAuthority")
	public ResponseEntity<ValidationResult> addGrantingAuthority(@Valid @RequestBody GrantingAuthorityRequest gaInputRequest) {

		try {
			log.info("Beofre calling add GrantingAuthority::::");
			// TODO - check if we can result list of errors here it self
			if(gaInputRequest==null) {
				throw new InvalidRequestException("Invalid Request");
			}
			String accessToken=getBearerToken();
			log.info(" add GrantingAuthority accessToken ::::"+accessToken);
			ValidationResult validationResult = new ValidationResult();
			GrantingAuthority grantingAuthority = grantingAuthorityService.createGrantingAuthority(gaInputRequest,accessToken);
			
			validationResult.setMessage("created successfully : gaId: "+grantingAuthority.getGaId());

			return ResponseEntity.status(HttpStatus.OK).body(validationResult);
		} catch (Exception e) {

			// 2.0 - CatchException and return validation errors
			ValidationResult validationResult = new ValidationResult();

			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(validationResult);
		}

	}
	
	/**
	 * get the Granting AUthority as input from UI and update the same in DBand  return Validation results based on input.
	 * 
	 * @param searchInput
	 *            - Input as SearchInput object from front end
	 * @return ResponseEntity - Return response status and description
	 */
	@PutMapping(
			value="grantingAuthority/{gaNumber}"
		
			)
	public ResponseEntity<ValidationResult> updateGrantingAuthority(@Valid @RequestBody GrantingAuthorityRequest gaInputRequest,@PathVariable("gaNumber") Long gaNumber) {

		try {
			log.info("Beofre calling update award::::");
			// TODO - check if we can result list of errors here it self
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
	 * get the Granting AUthority as input from UI and update the same in DBand  return Validation results based on input.
	 * 
	 * @param searchInput
	 *            - Input as SearchInput object from front end
	 * @return ResponseEntity - Return response status and description
	 */
	@DeleteMapping(
			value="grantingAuthority/{gaNumber}"
			)
	public ResponseEntity<UserDetailsResponse> deActivateGrantingAuthority(@Valid @RequestBody GrantingAuthorityRequest gaInputRequest,@PathVariable("gaNumber") Long gaNumber) {

		try {
			log.info("Beofre calling deActivateGrantingAuthority::::");
			// TODO - check if we can result list of errors here it self
			if(gaInputRequest==null) {
				throw new Exception("gaInputRequest is empty");
			}
			ValidationResult validationResult = new ValidationResult();
			String accessToken=getBearerToken();
			
			UserDetailsResponse userDetailsResponse  = grantingAuthorityService.deActivateGrantingAuthority(gaInputRequest,gaNumber,accessToken);
			
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
		
		log.info("graph api scope::{}", environment.getProperty("graph-api-scope"));
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

		map.add("grant_type", "client_credentials");
		map.add("client_id", environment.getProperty("client-Id"));
		map.add("client_secret", environment.getProperty("client-secret"));
		map.add("scope", environment.getProperty("graph-api-scope"));

		AccessTokenResponse openIdTokenResponse = graphAPILoginFeignClient
				.getAccessIdToken(environment.getProperty("tenant-id"), map);
		
		//log.info("openIdTokenResponse  ::{}",openIdTokenResponse);

		if (openIdTokenResponse == null) {
			throw new AccessTokenException(HttpStatus.valueOf(500),
					"Graph Api Service Failed while bearer token generate");
		}
		

		log.warn(" after access token " + openIdTokenResponse.getAccessToken());
		return openIdTokenResponse.getAccessToken();
		
	}
	
	/**
	 * get the Granting AUthority as input from UI and update the same in DBand  return Validation results based on input.
	 * 
	 * @param searchInput
	 *            - Input as SearchInput object from front end
	 * @return ResponseEntity - Return response status and description
	 */
	@DeleteMapping(
			value="usersGroup/{gaNumber}"
			)
	public ResponseEntity<ValidationResult> deleteUsersGroup(@Valid @RequestBody UsersGroupRequest usersGroupRequest,@PathVariable("gaNumber") Long gaNumber) {

		try {
			log.info("Beofre calling delete UsersGroup::::");
			// TODO - check if we can result list of errors here it self
			if(usersGroupRequest==null) {
				throw new Exception("usersGroupRequest is empty");
			}
			ValidationResult validationResult = new ValidationResult();
			
			String accessToken=getBearerToken();
			
			GrantingAuthority grantingAuthority = grantingAuthorityService.deleteUser(accessToken, usersGroupRequest,gaNumber);
			
			validationResult.setMessage(grantingAuthority.getGaId()+ " deActivated  successfully");

			return ResponseEntity.status(HttpStatus.OK).body(validationResult);
		} catch (Exception e) {

			// 2.0 - CatchException and return validation errors
			ValidationResult validationResult = new ValidationResult();

			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(validationResult);
		}

	}

}
