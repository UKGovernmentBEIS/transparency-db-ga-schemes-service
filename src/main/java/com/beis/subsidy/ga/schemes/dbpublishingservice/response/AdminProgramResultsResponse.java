package com.beis.subsidy.ga.schemes.dbpublishingservice.response;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.AdminProgram;
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
public class AdminProgramResultsResponse {

	private long activeAdminPrograms;
	private long deletedAdminPrograms;

	private long allAdminPrograms;
	public long totalSearchResults;
	public int currentPage;
	public int totalPages;

	@JsonProperty
	private List<AdminProgramResponse> adminPrograms;

	public AdminProgramResultsResponse(List<AdminProgram> adminPrograms, long totalSearchResults,
									   int currentPage, int totalPages, Map<String, Long> itemCounts) {
		this.adminPrograms = adminPrograms.stream().map(adminProgram ->
				new AdminProgramResponse(adminProgram)).collect(Collectors.toList());
		this.totalSearchResults = totalSearchResults;
		this.currentPage = currentPage;
		this.totalPages = totalPages;
		this.activeAdminPrograms = itemCounts.get("active");
		this.deletedAdminPrograms = itemCounts.get("deleted");
		this.allAdminPrograms = itemCounts.get("all");
	}

	public AdminProgramResultsResponse(Map<String, Long> itemCounts) {
		this.activeAdminPrograms = itemCounts.get("active");
		this.deletedAdminPrograms = itemCounts.get("deleted");
		this.allAdminPrograms = itemCounts.get("all");
	}
}
