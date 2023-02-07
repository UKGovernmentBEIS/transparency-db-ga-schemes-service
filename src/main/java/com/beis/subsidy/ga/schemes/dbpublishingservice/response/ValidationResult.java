package com.beis.subsidy.ga.schemes.dbpublishingservice.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationResult {

	private int totalRows;
	private int errorRows;
	private List<ValidationErrorResult> validationErrorResult;
	private String message;

}
