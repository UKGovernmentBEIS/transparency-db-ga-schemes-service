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

	private String grantingAuthorityName;

	private String grantingAuthorityID;

	private int pageNumber;

	private int totalRecordsPerPage;

	private String[] sortBy;

}
