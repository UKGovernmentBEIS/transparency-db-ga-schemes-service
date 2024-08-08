package com.beis.subsidy.ga.schemes.dbpublishingservice.response;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthority;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * Search results object - Represents search results
 *
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SearchResults<T> {

	public long totalSearchResults;
	public int currentPage;
	public int totalPages;
	public int activeCount;
	public int inactiveCount;
	@JsonProperty
	private List<T> responseList;

	public SearchResults(List<T> responseList, long totalSearchResults,
						 int currentPage, int totalPages,int activeCount,int inactiveCount)
	{
		this.responseList = responseList;
		this.totalSearchResults = totalSearchResults;
		this.currentPage = currentPage;
		this.totalPages = totalPages;
		this.activeCount = activeCount;
		this.inactiveCount = inactiveCount;
	}
}
