package com.beis.subsidy.ga.schemes.dbpublishingservice.response;

import java.util.List;
import java.util.stream.Collectors;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthority;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * Search results object - Represents search results for award search
 *
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SearchResults {

	public long totalSearchResults;
	public int currentPage;
	public int totalPages;
	public int activeCount;
	public int inActiveCount;
	@JsonProperty
	private List<GrantingAuthorityResponse> gaList;

	public SearchResults(List<GrantingAuthority> gaList, long totalSearchResults,
						 int currentPage, int totalPages,int activeCount,int inActiveCount) {

		this.gaList = gaList.stream().map(grantingAuthority ->
				new GrantingAuthorityResponse(grantingAuthority, false)).collect(Collectors.toList());
		this.totalSearchResults = totalSearchResults;
		this.currentPage = currentPage;
		this.totalPages = totalPages;
		this.inActiveCount=inActiveCount;
		this.activeCount=activeCount;
	}
}
