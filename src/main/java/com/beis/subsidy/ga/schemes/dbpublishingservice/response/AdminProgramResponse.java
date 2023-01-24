package com.beis.subsidy.ga.schemes.dbpublishingservice.response;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.AdminProgram;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.SubsidyMeasure;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.SearchUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class AdminProgramResponse {

    @JsonProperty
    private String adminProgramName;

    @JsonProperty
    private String apNumber;

    private SubsidyMeasureResponse subsidyMeasure;

    @JsonProperty
    private String budget;

    @JsonProperty
    private String budgetFormatted;

    @JsonProperty
    private String gaName;

    @JsonProperty
    private String createdDate;

    @JsonProperty
    private String lastModifiedDate;

    @JsonProperty
    private String status;

    @JsonProperty
    private String deletedBy;

    @JsonProperty
    private String deletedTimestamp;

    @JsonProperty
    private Boolean canEdit;

    public AdminProgramResponse(AdminProgram adminProgram) {
        this.adminProgramName = adminProgram.getAdminProgramName();
        this.apNumber = adminProgram.getApNumber();
        this.subsidyMeasure = new SubsidyMeasureResponse(adminProgram.getSubsidyMeasure());
        this.budget = adminProgram.getBudget().toString();
        this.budgetFormatted = SearchUtils.decimalNumberFormat(adminProgram.getBudget());
        this.gaName = adminProgram.getGrantingAuthority().getGrantingAuthorityName();
        this.status = adminProgram.getStatus();
        this.createdDate = SearchUtils.dateTimeToFullMonthNameInDate(adminProgram.getCreatedTimestamp());
        this.lastModifiedDate = SearchUtils.dateTimeToFullMonthNameInDate(adminProgram.getLastModifiedTimestamp());
        if(adminProgram.getDeletedBy() != null) {
            this.deletedBy = adminProgram.getDeletedBy();
        }
        if(adminProgram.getDeletedTimestamp() != null) {
            this.deletedTimestamp = SearchUtils.dateTimeToFullMonthNameInDate(adminProgram.getDeletedTimestamp());
        }
        this.canEdit = false;
    }
}
