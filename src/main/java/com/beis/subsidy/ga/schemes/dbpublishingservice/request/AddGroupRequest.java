package com.beis.subsidy.ga.schemes.dbpublishingservice.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddGroupRequest {

	
	private String displayName;
	private String description;
	private boolean mailEnabled;
	private String mailNickname;
	
	private boolean securityEnabled;

	@JsonCreator
	public AddGroupRequest(@JsonProperty("description") String description,@JsonProperty("displayName") String displayName,
			@JsonProperty("mailEnabled") boolean mailEnabled,
			@JsonProperty("mailNickname") String mailNickname ,@JsonProperty("securityEnabled") boolean securityEnabled
			) {

		this.securityEnabled = securityEnabled;
		this.displayName = displayName;
		this.mailNickname = mailNickname;
		this.description = description;
		this.mailEnabled = mailEnabled;

	}
}
