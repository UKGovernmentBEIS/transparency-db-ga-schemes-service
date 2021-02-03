package com.beis.subsidy.ga.schemes.dbpublishingservice.request;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.Award;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.LegalBasis;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class SchemeDetailsRequest {
    private String scNumber;
    private List<Award> awards;
    private GrantingAuthority grantingAuthority;
    private LegalBasis legalBases;
    private String subsidyMeasureTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigInteger duration;
    private String budget;
    private boolean adhoc;
    private String gaSubsidyWebLink;
    private LocalDate publishedMeasureDate;
    private String createdBy;
    private String approvedBy;
    private String status;
    private LocalDate createdTimestamp;
    private LocalDate lastModifiedTimestamp;
    private Long gaId;
    private String gaName;
    private String legalBasisText;
}
