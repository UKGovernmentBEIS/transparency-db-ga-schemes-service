package com.beis.subsidy.ga.schemes.dbpublishingservice.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthorityRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SearchResults;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.ValidationResult;
import com.beis.subsidy.ga.schemes.dbpublishingservice.service.GrantingAuthorityService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class GrantingAuthorityController {

	@Autowired
	public GrantingAuthorityService grantingAuthorityService;
	
	
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
				throw new Exception("granting authority InputRequest is empty");
			}
			
			ValidationResult validationResult = new ValidationResult();
			GrantingAuthority grantingAuthority = grantingAuthorityService.createGrantingAuthority(gaInputRequest);
			
			validationResult.setMessage(grantingAuthority.getGaId()+ " created successfully");

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
			
			GrantingAuthority grantingAuthority = grantingAuthorityService.updateGrantingAuthority(gaInputRequest,gaNumber);
			
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
	public ResponseEntity<ValidationResult> deActivateGrantingAuthority(@Valid @RequestBody GrantingAuthorityRequest gaInputRequest,@PathVariable("gaNumber") Long gaNumber) {

		try {
			log.info("Beofre calling update award::::");
			// TODO - check if we can result list of errors here it self
			if(gaInputRequest==null) {
				throw new Exception("gaInputRequest is empty");
			}
			ValidationResult validationResult = new ValidationResult();
			
			GrantingAuthority grantingAuthority = grantingAuthorityService.deActivateGrantingAuthority(gaInputRequest,gaNumber);
			
			validationResult.setMessage(grantingAuthority.getGaId()+ " updated as Inactive successfully");

			return ResponseEntity.status(HttpStatus.OK).body(validationResult);
		} catch (Exception e) {

			// 2.0 - CatchException and return validation errors
			ValidationResult validationResult = new ValidationResult();

			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(validationResult);
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

}
