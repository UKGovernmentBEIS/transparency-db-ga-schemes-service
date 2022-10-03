package com.beis.subsidy.ga.schemes.dbpublishingservice.response;

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
public class SubsidyMeasureResponse {

    @JsonProperty
    private String subsidyMeasureTitle;

    @JsonProperty
    private String scNumber;

    @JsonProperty
    private String startDate;

    @JsonProperty
    private String endDate;

    @JsonProperty
    private String duration;

    @JsonProperty
    private String budget;

    @JsonProperty
    private String gaName;

    @JsonProperty
    private String adhoc;

    @JsonProperty
    private String gaSubsidyWebLink;

    @JsonProperty
    private String gaSubsidyWebLinkDescription;

    @JsonProperty
    private String lastModifiedDate;

    @JsonProperty
    private String legalBasisText;

    @JsonProperty
    private String status;

    @JsonProperty
    private String publishedMeasureDate;

    @JsonProperty
    private String deletedBy;

    @JsonProperty
    private String deletedTimestamp;

    @JsonProperty
    private Boolean canEdit;

    @JsonProperty
    private boolean hasNoEndDate;

    @JsonProperty
    private String confirmationDate;

    public SubsidyMeasureResponse(SubsidyMeasure subsidyMeasure) {
        this.scNumber = subsidyMeasure.getScNumber();
        this.subsidyMeasureTitle  = subsidyMeasure.getSubsidyMeasureTitle();
        this.startDate =  SearchUtils.dateToFullMonthNameInDate(subsidyMeasure.getStartDate());

        if(subsidyMeasure.getEndDate() == null){
            this.endDate = "";
        } else {
            this.endDate = SearchUtils.dateToFullMonthNameInDate(subsidyMeasure.getEndDate());
        }
        this.duration = SearchUtils.getDurationInYears(subsidyMeasure.getDuration());
        if(SearchUtils.isNumeric(subsidyMeasure.getBudget())){
            this.budget = subsidyMeasure.getBudget().contains(",") ? subsidyMeasure.getBudget():
                    SearchUtils.decimalNumberFormat(new BigDecimal(subsidyMeasure.getBudget().trim()));
        }else{
            log.error("Budget for scheme {} is not numeric, Budget is {}",subsidyMeasure.getScNumber(),subsidyMeasure.getBudget());
            this.budget = subsidyMeasure.getBudget();
        }
        this.gaName = subsidyMeasure.getGrantingAuthority().getGrantingAuthorityName();
        this.adhoc = ""+subsidyMeasure.isAdhoc();
        this.status = subsidyMeasure.getStatus();
        this.gaSubsidyWebLink = subsidyMeasure.getGaSubsidyWebLink();
        this.gaSubsidyWebLinkDescription = subsidyMeasure.getGaSubsidyWebLinkDescription();
        this.legalBasisText = subsidyMeasure.getLegalBases().getLegalBasisText();
        this.lastModifiedDate = SearchUtils.dateToFullMonthNameInDate(subsidyMeasure.getLastModifiedTimestamp());
        this.publishedMeasureDate = SearchUtils.dateToFullMonthNameInDate(subsidyMeasure.getPublishedMeasureDate());
        this.hasNoEndDate =subsidyMeasure.isHasNoEndDate();
        if(subsidyMeasure.getDeletedBy() != null) {
            this.deletedBy = subsidyMeasure.getDeletedBy();
        }
        if(subsidyMeasure.getDeletedTimestamp() != null) {
            this.deletedTimestamp = SearchUtils.dateTimeToFullMonthNameInDate(subsidyMeasure.getDeletedTimestamp());
        }
        this.canEdit = true;
        this.confirmationDate = SearchUtils.dateToFullMonthNameInDate(subsidyMeasure.getConfirmationDate());
    }

}
