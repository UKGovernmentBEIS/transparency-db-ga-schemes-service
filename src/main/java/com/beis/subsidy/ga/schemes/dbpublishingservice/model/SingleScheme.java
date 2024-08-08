package com.beis.subsidy.ga.schemes.dbpublishingservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class SingleScheme {

    private int row;
    private String column;
    private String publicAuthorityName;
    private String subsidySchemeName;
    private String subsidySchemeDescription;
    private String specificPolicyObjective;
    private String legalBasis;
    private String publicAuthorityPolicyURL;
    private String publicAuthorityPolicyPageDescription;
    private String budget;
    private String maximumAmountGivenUnderScheme;
    private String confirmationDate;
    private String startDate;
    private String endDate;
    private String spendingSectors;
    private String purpose;
    private String subsidySchemeInterest;

}