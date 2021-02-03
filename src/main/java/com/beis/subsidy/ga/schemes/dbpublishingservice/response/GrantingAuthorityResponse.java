package com.beis.subsidy.ga.schemes.dbpublishingservice.response;

import java.time.LocalDate;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthority;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GrantingAuthorityResponse {
	@JsonProperty
	private Long grantingAuthorityId;

	@JsonProperty
	private String grantingAuthorityName;

	@JsonProperty
	private String status;

	@JsonProperty
	private String createdBy;

	@JsonProperty
	private String approvedBy;

	@JsonProperty
	private LocalDate createdTimestamp;

	@JsonProperty
	private LocalDate lastModifiedTimestamp;

	public GrantingAuthorityResponse(GrantingAuthority grantingAuthority, boolean flag) {

		this.grantingAuthorityId = grantingAuthority.getGaId();
		this.grantingAuthorityName = grantingAuthority.getGrantingAuthorityName();
		this.createdBy = grantingAuthority.getCreatedBy();
		this.approvedBy = grantingAuthority.getApprovedBy();
		this.status = grantingAuthority.getStatus();
		this.createdTimestamp = grantingAuthority.getCreatedTimestamp();
		this.lastModifiedTimestamp = grantingAuthority.getLastModifiedTimestamp();
		
	}
}
