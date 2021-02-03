package com.beis.subsidy.ga.schemes.dbpublishingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 
 * Search Result Not found Exception class - custom exception
 *
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SearchResultNotFoundException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	/**
	 * constructor with message as param
	 * @param message - exception message
	 */
	public SearchResultNotFoundException(String message) {
		super(message);
	}
	
}
