package com.beis.subsidy.ga.schemes.dbpublishingservice.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * Search Input object - represents search input criteria for public search
 * service
 *
 */
@NoArgsConstructor
@Setter
@Getter
public class SearchInput {

	protected String searchText;

	protected String id;

	protected String status;

	protected int pageNumber;

	protected int totalRecordsPerPage;

	protected String[] sortBy;

}
