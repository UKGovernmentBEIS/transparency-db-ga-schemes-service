package com.beis.subsidy.ga.schemes.dbpublishingservice.response;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.SubsidyMeasure;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 
 * Search results object - Represents search results for award search
 *
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchSubsidyResultsResponse {
	private long activeScheme;
	private long inactiveScheme;
	private long deletedScheme;
	private long allScheme;

	public long totalSearchResults;
	public int currentPage;
	public int totalPages;

	@JsonProperty
	private List<SubsidyMeasureResponse> schemes;

	public SearchSubsidyResultsResponse(List<SubsidyMeasure> schemes, long totalSearchResults,
										int currentPage, int totalPages, Map<String, Long> schemeCount) {

		this.schemes = schemes.stream().map(scheme ->
				new SubsidyMeasureResponse(scheme)).collect(Collectors.toList());
		this.totalSearchResults = totalSearchResults;
		this.currentPage = currentPage;
		this.totalPages = totalPages;
		this.activeScheme = schemeCount.get("activeScheme");
		this.inactiveScheme = schemeCount.get("inactiveScheme");
		this.deletedScheme = schemeCount.get("deletedScheme");
		this.allScheme = schemeCount.get("allScheme");
	}
	public SearchSubsidyResultsResponse(Map<String, Long> schemeCount) {
		this.activeScheme = schemeCount.get("activeScheme");
		this.inactiveScheme = schemeCount.get("inactiveScheme");
		this.deletedScheme = schemeCount.get("deletedScheme");
		this.allScheme = schemeCount.get("allScheme");
	}
}
