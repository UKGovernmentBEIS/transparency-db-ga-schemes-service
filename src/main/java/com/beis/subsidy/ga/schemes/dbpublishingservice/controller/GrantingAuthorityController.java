package com.beis.subsidy.ga.schemes.dbpublishingservice.controller;

import java.util.Objects;

import javax.validation.Valid;

import com.beis.subsidy.ga.schemes.dbpublishingservice.response.GAResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.GrantingAuthorityResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.UserDetailsResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SearchResults;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.AccessTokenResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.service.GrantingAuthorityService;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.UserPrinciple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.beis.subsidy.ga.schemes.dbpublishingservice.controller.feign.GraphAPILoginFeignClient;
import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.AccessTokenException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.InvalidRequestException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthorityRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.UsersGroupRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.AuditLogsRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.SearchUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	
	@Autowired
    AuditLogsRepository auditLogsRepository;

	 @Autowired
	 private ObjectMapper objectMapper;

	@Value("${loggingComponentName}")
	private String loggingComponentName;
	    
	/**
	 * To get Granting Authority as input from UI and return Validation results based on input.
	 * 
	 * @param gaInputRequest
	 *            - Input as SearchInput object from front end
	 * @return ResponseEntity - Return response status and description
	 */
	@PostMapping("grantingAuthority")
	public ResponseEntity<GAResponse> addGrantingAuthority(@RequestBody
							 GrantingAuthorityRequest gaInputRequest) {

		log.info("{} ::Before calling add addGrantingAuthority",loggingComponentName);
		if(gaInputRequest==null) {
				throw new InvalidRequestException("Invalid Request");
		}
		String accessToken=getBearerToken();
		log.info("{} ::after GrantingAuthority accessToken", loggingComponentName);
			
		GAResponse response = new GAResponse();
		GrantingAuthority grantingAuthority = grantingAuthorityService
					.createGrantingAuthority(gaInputRequest,accessToken);
			
		response.setGaId(grantingAuthority.getGaId());
		response.setMessage("Created successfully");

		StringBuilder eventMsg = new StringBuilder("Public Authority ").append(gaInputRequest.getName())
				.append(" is added");

		SearchUtils.saveAuditLogForCreateGA(gaInputRequest.getUserName(),"Create Public Authority",
				grantingAuthority.getGaId().toString(),grantingAuthority.getGrantingAuthorityName(),eventMsg.toString(),auditLogsRepository);
		return ResponseEntity.status(HttpStatus.OK).body(response);
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
	public ResponseEntity<GAResponse> updateGrantingAuthority(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
			@Valid @RequestBody GrantingAuthorityRequest gaInputRequest,@PathVariable("gaNumber") Long gaNumber) {
		UserPrinciple userPrincipleObj= null;
		HttpStatus status  = HttpStatus.OK;
		try {
			log.info("{}::Inside  updateGrantingAuthority", loggingComponentName);
			//check user role here
			userPrincipleObj = SearchUtils.beisAdminRoleValidation(objectMapper, userPrinciple,"update Granting Authority");

			if(gaInputRequest==null || gaNumber == 0) {
				throw new InvalidRequestException("gaInputRequest is empty");
			}

			String accessToken=getBearerToken();
			GrantingAuthority grantingAuthority = grantingAuthorityService.updateGrantingAuthority(gaInputRequest,gaNumber,accessToken);
			GAResponse response = new GAResponse();
			if (Objects.nonNull(grantingAuthority)) {

				response.setGaId(grantingAuthority.getGaId());
				response.setMessage("updated successfully");

				//Audit entry
				StringBuilder eventMsg = new StringBuilder("Public Authority ").append(grantingAuthority.getGrantingAuthorityName())
						.append(" is updated" );
				SearchUtils.saveAuditLog(userPrincipleObj,"Update Public Authority", grantingAuthority.getGaId().toString(),
						eventMsg.toString(),auditLogsRepository);
			} else {
				status = HttpStatus.BAD_REQUEST;
				log.error("{}::Failed the updateGrantingAuthority operation", loggingComponentName);
				response.setMessage("failed with gaId");
			}
			log.info("{}::after calling updateGrantingAuthority", loggingComponentName);
			return ResponseEntity.status(status).body(response);
		} catch (Exception e) {
			log.info("{}::In catch block of updateGrantingAuthority", loggingComponentName, e);
			// 2.0 - CatchException and return validation errors
			GAResponse response = new GAResponse();
			response.setMessage("failed to update Public Authority");

			String eventMsg = "failed to update Public Authority";
			SearchUtils.saveAuditLog(userPrincipleObj,"Update Public Authority",gaNumber.toString() ,
					eventMsg,auditLogsRepository);

			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
		}

	}
	
	/**
	 * get the users associated to the GA
	 * return list of users
	 * 
	 * @return ResponseEntity - Return response status and description
	 */
	@GetMapping(
			value="grantingAuthority/{azGrpId}"
			)
	public ResponseEntity<UserDetailsResponse> deActivateGrantingAuthority(@RequestHeader("userPrinciple") HttpHeaders userPrinciple
			,@PathVariable("azGrpId") String azGrpId) {

		
		log.info("{} ::Before calling deActivateGrantingAuthority", loggingComponentName);
		if(azGrpId==null) {
			throw new InvalidRequestException("deActivateGrantingAuthority request is empty");
		}
		//check user role here
		SearchUtils.beisAdminRoleValidation(objectMapper, userPrinciple,
					"DeActivate Public Authority");
		String accessToken=getBearerToken();
			
		UserDetailsResponse userDetailsResponse  = grantingAuthorityService
					.deActivateGrantingAuthority(azGrpId,accessToken);

		log.info("{} ::After calling deActivateGrantingAuthority", loggingComponentName);
		return new ResponseEntity<UserDetailsResponse>(userDetailsResponse, HttpStatus.OK);
	}
	
	/**
	 * To get search input from UI and return search results based on search criteria
	 * 
	 * @param searchInput - Input as SearchInput object from front end 
	 * @return ResponseEntity - Return response status and description
	 */
	
	@PostMapping("searchGrantingAuthority")
	public ResponseEntity<SearchResults<GrantingAuthorityResponse>> findSearchResults(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
																					  @Valid @RequestBody SearchInput searchInput) {

		    SearchUtils.isAllRolesValidation(objectMapper, userPrinciple,"Search Results");
			//Set Default Page records
			if(searchInput.getTotalRecordsPerPage() == 0) {
				searchInput.setTotalRecordsPerPage(10);
			}
			SearchResults<GrantingAuthorityResponse> searchResults = grantingAuthorityService.findMatchingGrantingAuthorities(searchInput);

			return new ResponseEntity<SearchResults<GrantingAuthorityResponse>>(searchResults, HttpStatus.OK);
	}
	/**
	 * 
	 * @return
	 * @throws AccessTokenException
	 */
	public String getBearerToken() throws AccessTokenException {

		log.info("{} ::inside getBearerToken method", loggingComponentName);

	   MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

		map.add("grant_type", "client_credentials");
		map.add("client_id", environment.getProperty("client-Id"));
		map.add("client_secret", environment.getProperty("client-secret"));
		map.add("scope", environment.getProperty("graph-api-scope"));

		AccessTokenResponse openIdTokenResponse = graphAPILoginFeignClient
				.getAccessIdToken(environment.getProperty("tenant-id"), map);

		if (openIdTokenResponse == null) {
			log.error("{} ::Graph Api Service Failed while bearer token generate in getBearerToken", loggingComponentName);
			throw new AccessTokenException(HttpStatus.valueOf(500),
					"Graph Api Service Failed while bearer token generate");
		}
		return openIdTokenResponse.getAccessToken();
	}

	@DeleteMapping(
			value="group/{azGrpId}")
	public ResponseEntity<GAResponse> deleteUsersGroup(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
							@PathVariable("azGrpId") String azGrpId,@RequestBody UsersGroupRequest usersGroupRequest)
	{
		log.info("{} ::before calling delete UsersGroup", loggingComponentName);
		if(Objects.isNull(usersGroupRequest)|| StringUtils.isEmpty(azGrpId)) {
			throw new InvalidRequestException("usersGroupRequest is empty");
		}

		UserPrinciple userPrincipleObj = SearchUtils.beisAdminRoleValidation(objectMapper, userPrinciple,
					"DeActive Public Authority and Users");

		GAResponse response = new GAResponse();
		String accessToken=getBearerToken();
		GrantingAuthority grantingAuthority = grantingAuthorityService.deleteUser(accessToken, usersGroupRequest, azGrpId);
		if (Objects.nonNull(grantingAuthority)) {
			response.setGaId(grantingAuthority.getGaId());
			response.setMessage("deActivated  successfully");
		} else {
			response.setMessage(" status not updated deActive for GA");
		}
		//Audit entry
		StringBuilder eventMsg = new StringBuilder("Granting Authority ")
				.append(" is Deactivated" );
		SearchUtils.saveAuditLog(userPrincipleObj,"Deactivated Public Authority", azGrpId.toString(),
					eventMsg.toString(),auditLogsRepository);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}