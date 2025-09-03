package com.beis.subsidy.ga.schemes.dbpublishingservice.service;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.*;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.AuditLogsRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.LegalBasisRepository;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.GrantingAuthorityRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.SubsidyMeasureRepository;

import com.beis.subsidy.ga.schemes.dbpublishingservice.util.ExcelHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class SchemeService {

    private static final String CONSTANT_SYSTEM = "SYSTEM";

    @Autowired
    private SubsidyMeasureRepository subsidyMeasureRepository;


    @Autowired
    private GrantingAuthorityRepository gaRepository;

    @Autowired
    private LegalBasisRepository legalBasisRepository;

    @Autowired
    private SubsidyMeasureRepository smRepository;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Autowired
    AuditLogsRepository auditLogsRepository;

    @Transactional
    public List<SubsidyMeasure> processBulkSchemes(List<BulkUploadSchemes> bulkSchemes, String role) {
        try {

            List<SubsidyMeasure> schemes = bulkSchemes.stream()
                    .map((BulkUploadSchemes bulkUploadScheme) -> generateSubsidyMeasureFromBulkUpload(bulkUploadScheme, role)).collect(Collectors.toList());


            List<SubsidyMeasure> savedSchemes = subsidyMeasureRepository.saveAll(schemes);
            ExcelHelper.saveBulkUploadSchemesAuditLog(savedSchemes, auditLogsRepository);
            log.info("End process Bulk Schemes db");

            return savedSchemes;
        } catch(Exception serviceException) {
            log.error("serviceException occurred::" , serviceException);
            return null;
        }
    }

    private SubsidyMeasure generateSubsidyMeasureFromBulkUpload(BulkUploadSchemes bulkUploadScheme, String role){
        LegalBasis legalBasis = new LegalBasis(null, null, bulkUploadScheme.getLegalBasis(), "SYSTEM", "SYSTEM", "Active", new Date(), new Date());
        SubsidyMeasure subsidyMeasure = new SubsidyMeasure(
                null,
                getGrantingAuthority(bulkUploadScheme),
                getGrantingAuthority(bulkUploadScheme).getGaId(),
                null,
                bulkUploadScheme.getSubsidySchemeName(),
                bulkUploadScheme.getStartDate(),
                bulkUploadScheme.getEndDate(),
                getDuration(bulkUploadScheme),
                bulkUploadScheme.getBudget(),
                false,
                bulkUploadScheme.getPublicAuthorityPolicyURL(),
                LocalDate.now(),
                role,
                "SYSTEM",
                "Active",
                bulkUploadScheme.getPublicAuthorityPolicyPageDescription(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null,
                bulkUploadScheme.isHasNoEndDate(),
                bulkUploadScheme.getSubsidySchemeDescription(),
                bulkUploadScheme.getSpecificPolicyObjective(),
                bulkUploadScheme.getConfirmationDate(),
                convertToSectorJson(bulkUploadScheme.getSpendingSectors()),
                bulkUploadScheme.getMaximumAmountGivenUnderScheme(),
                convertToSectorJson(bulkUploadScheme.getPurpose()),
                null,
                bulkUploadScheme.getSubsidySchemeInterest(),
                null,
                null);
        legalBasis.setSubsidyMeasure(subsidyMeasure);
        subsidyMeasure.setLegalBases(legalBasis);


        return subsidyMeasure;
    }

    private String convertToSectorJson(String spendingSectors) {
        String[] spendingSectorSplit = (spendingSectors.split("\\s*\\|\\s*"));
        List<String> spendingSectorJson = new ArrayList<String>();
        for (String sector:spendingSectorSplit) {
            sector = '\"' + StringUtils.capitalize(sector) + '\"';
            spendingSectorJson.add(sector);
        }
        String spendingSectorString = "[" + String.join(",", spendingSectorJson) +"]";
        return spendingSectorString;
    }

    public BigInteger getDuration(BulkUploadSchemes bulkUploadScheme) {
        long noOfDaysBetween = 0;
        if(!bulkUploadScheme.isHasNoEndDate() && !(bulkUploadScheme.getStartDate().equals(bulkUploadScheme.getEndDate()))){
            noOfDaysBetween = ChronoUnit.DAYS.between(bulkUploadScheme.getStartDate(), bulkUploadScheme.getEndDate());
        } else if (bulkUploadScheme.getStartDate().equals(bulkUploadScheme.getEndDate())){
            noOfDaysBetween = 1;
        }
        return BigInteger.valueOf(noOfDaysBetween);
    }

    private GrantingAuthority getGrantingAuthority(BulkUploadSchemes scheme) {

        log.info("Inside getGrantingAuthority...");

        List<GrantingAuthority> gaList = gaRepository.findAll();

        Optional<GrantingAuthority> gaOptional = gaList.stream().filter(ga -> ga.getGrantingAuthorityName().equals(scheme.getPublicAuthorityName())).findAny();

        log.info("Returning from getGrantingAuthorityId.. = " + gaOptional.get().getGaId());
        return ((gaOptional != null) ? gaOptional.get() : null);
    }

}

