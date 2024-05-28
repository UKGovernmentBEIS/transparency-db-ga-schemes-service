package com.beis.subsidy.ga.schemes.dbpublishingservice.model;

import java.time.LocalDate;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class BulkUploadSchemes {

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
    private LocalDate confirmationDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String spendingSectors;
    private boolean hasNoEndDate;
    private String subsidySchemeInterest;

}
