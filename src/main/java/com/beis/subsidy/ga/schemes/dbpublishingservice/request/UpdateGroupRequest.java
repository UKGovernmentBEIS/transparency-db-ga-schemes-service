package com.beis.subsidy.ga.schemes.dbpublishingservice.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateGroupRequest {


	private String displayName;
	private String description;

	@JsonCreator
	public UpdateGroupRequest(@JsonProperty("description") String description,
							  @JsonProperty("displayName") String displayName) {

		this.displayName = displayName;
		this.description = description;
	}
}
