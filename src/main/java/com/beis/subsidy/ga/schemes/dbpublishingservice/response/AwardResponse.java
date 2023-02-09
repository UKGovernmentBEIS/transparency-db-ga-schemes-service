package com.beis.subsidy.ga.schemes.dbpublishingservice.response;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.Award;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.LegalBasis;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.SubsidyMeasure;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.SearchUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AwardResponse {
    @JsonProperty
    private Long awardNumber;

    @JsonProperty
    private BeneficiaryResponse beneficiary;

    @JsonProperty
    private SubsidyMeasureResponse subsidyMeasure;

    @JsonProperty
    private GrantingAuthorityResponse grantingAuthorityResponse;

    @JsonProperty
    private String subsidyFullAmountRange;

    @JsonProperty
    private String subsidyFullAmountExact;

    @JsonProperty
    private String subsidyObjective;

    @JsonProperty
    private String subsidyInstrument;

    @JsonProperty
    private String spendingSector;

    @JsonProperty
    private String goodsServicesFilter;

    @JsonProperty
    private String legalGrantingDate;

    @JsonProperty
    private String createdBy;

    @JsonProperty
    private String approvedBy;

    @JsonProperty
    private String status;

    @JsonProperty
    private String rejectReason;

    @JsonProperty
    private String spendingRegion;

    @JsonProperty
    private String createdTimestamp;

    @JsonProperty
    private String lastModifiedTimestamp;

    @JsonProperty
    private String publishedAwardDate;

    @JsonProperty
    private String standaloneAward;

    @JsonProperty
    private String subsidyAwardDescription;

    @JsonProperty
    private String adminProgramNumber;

    @JsonProperty
    private String adminProgramName;

    public AwardResponse(Award award, boolean flag) {
    	
    	log.info("inside  AwardResponse::");
        this.awardNumber = award.getAwardNumber();
        this.subsidyFullAmountRange = SearchUtils.formatedFullAmountRange(award.getSubsidyFullAmountRange());
        this.subsidyFullAmountExact = SearchUtils.decimalNumberFormat(award.getSubsidyFullAmountExact());
        this.subsidyObjective = award.getSubsidyObjective();
        this.spendingSector = award.getSpendingSector();
        this.subsidyInstrument = award.getSubsidyInstrument();
        this.spendingRegion = award.getSpendingRegion();
        this.legalGrantingDate = SearchUtils.dateToFullMonthNameInDate(award.getLegalGrantingDate());
        this.status = award.getStatus();
        if (flag) {
            this.goodsServicesFilter = award.getGoodsServicesFilter();
            this.createdBy = award.getCreatedBy();
            this.approvedBy = award.getApprovedBy();
            this.createdTimestamp = SearchUtils.dateToFullMonthNameInDate(award.getCreatedTimestamp());
            this.lastModifiedTimestamp = SearchUtils.dateToFullMonthNameInDate(award.getLastModifiedTimestamp());
            if ("Awaiting Approval".equals(this.status)) {
                this.publishedAwardDate = "Awaiting Approval";
            } else {
                this.publishedAwardDate = SearchUtils.dateToFullMonthNameInDate(award.getPublishedAwardDate());
            }
        }
        this.rejectReason = award.getReason()!= null ?  award.getReason().trim(): "";
        this.beneficiary = new BeneficiaryResponse(award.getBeneficiary(),flag);
        if(award.getSubsidyMeasure() != null){
            this.subsidyMeasure = new SubsidyMeasureResponse(award.getSubsidyMeasure());
        }else{
            // mock details of a scheme this will likely be a standalone award
            SubsidyMeasure mockedScheme = new SubsidyMeasure();
            LegalBasis mockedLegal = new LegalBasis();
            GrantingAuthority mockedGA = new GrantingAuthority();

            mockedLegal.setLegalBasisText("Mocked");
            mockedGA.setGrantingAuthorityName("Mocked");
            mockedScheme.setStatus("Mocked");
            mockedScheme.setScNumber("NA");
            mockedScheme.setSubsidyMeasureTitle("NA");

            mockedScheme.setLegalBases(mockedLegal);
            mockedScheme.setGrantingAuthority(mockedGA);

            this.subsidyMeasure = new SubsidyMeasureResponse(mockedScheme);
        }
        if (flag) {
         this.grantingAuthorityResponse = new GrantingAuthorityResponse(award.getGrantingAuthority(), true);
        }
        this.standaloneAward = award.getStandaloneAward();
        this.subsidyAwardDescription = award.getSubsidyAwardDescription();

        if(award.getAdminProgram() != null){
            this.adminProgramNumber = award.getAdminProgram().getApNumber();
            this.adminProgramName = award.getAdminProgram().getAdminProgramName();
        }else{
            this.adminProgramNumber = "NA";
            this.adminProgramName = "NA";
        }
    }
}
