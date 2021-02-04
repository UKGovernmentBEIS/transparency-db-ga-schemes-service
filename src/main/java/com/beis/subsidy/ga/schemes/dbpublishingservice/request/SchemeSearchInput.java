package com.beis.subsidy.ga.schemes.dbpublishingservice.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * Search Input object - represents search input criteria for subsidy scheme service
 *
 */
@NoArgsConstructor
@Setter
@Getter
public class SchemeSearchInput {
	private String searchName;
	private Integer pageNumber;
	private Integer totalRecordsPerPage;
	private String[] sortBy;
	private String status;
}
