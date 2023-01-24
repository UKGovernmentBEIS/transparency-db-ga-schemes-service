package com.beis.subsidy.ga.schemes.dbpublishingservice.request;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.Award;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.LegalBasis;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class AdminProgramDetailsRequest {
    private String scNumber;
    private String adminProgramName;
    private String grantingAuthorityName;
    private BigDecimal budget;
    private String status;
}
