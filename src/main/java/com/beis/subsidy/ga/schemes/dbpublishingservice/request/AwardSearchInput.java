package com.beis.subsidy.ga.schemes.dbpublishingservice.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
@NoArgsConstructor
@Setter
@Getter
public class AwardSearchInput extends SearchInput{
    private String beneficiaryName;

    private String subsidyMeasureTitle;

    private List<String> subsidyObjective;

    private List<String> otherSubsidyObjective;

    private List<String> spendingRegion;

    private List<String> subsidyInstrument;

    private List<String> otherSubsidyInstrument;

    private List<String> spendingSector;

    private String legalGrantingDate;

    private String scNumber;

    private String grantingAuthorityName;

    private String subsidyStatus;

    private BigDecimal budgetFrom;

    private BigDecimal budgetTo;
}
