package com.beis.subsidy.ga.schemes.dbpublishingservice.controller.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.beis.subsidy.ga.schemes.dbpublishingservice.response.AccessTokenResponse;

//@FeignClient(name = "GraphAPILoginFeignClient", url = "${graphApiLoginUrl}")
@FeignClient(name = "GraphAPILoginFeignClient", url = "https://login.microsoftonline.com")

public interface GraphAPILoginFeignClient {

	@PostMapping(value = "/{tenantID}/oauth2/v2.0/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	AccessTokenResponse getAccessIdToken(@PathVariable("tenantID") String tenantID,
			@RequestBody MultiValueMap<String, Object> request);
}
