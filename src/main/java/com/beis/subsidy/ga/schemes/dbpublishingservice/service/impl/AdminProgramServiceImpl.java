package com.beis.subsidy.ga.schemes.dbpublishingservice.service.impl;


import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.InvalidRequestException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.AdminProgram;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.SubsidyMeasure;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.AdminProgramRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.GrantingAuthorityRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.SubsidyMeasureRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.AdminProgramDetailsRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.service.AdminProgramService;
import com.beis.subsidy.ga.schemes.dbpublishingservice.service.SubsidySchemeService;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.AccessManagementConstant;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.UserPrinciple;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class AdminProgramServiceImpl implements AdminProgramService {
    @Autowired
    private AdminProgramRepository adminProgramRepository;

    @Autowired
    private GrantingAuthorityRepository gaRepository;

    @Autowired
    private SubsidyMeasureRepository smRepository;

    @Autowired
    private SubsidySchemeService subsidySchemeService;

    @Override
    public AdminProgram addAdminProgram(AdminProgramDetailsRequest adminProgramRequest, UserPrinciple userPrinciple) {
        log.info("Inside addSubsidySchemeDetails method :");
        AdminProgram adminProgramToSave = new AdminProgram();

        adminProgramToSave.setCreatedBy(userPrinciple.getUserName());
        if(adminProgramRequest.getBudget() != null){
            adminProgramToSave.setBudget(adminProgramRequest.getBudget());
        }

        if(!StringUtils.isEmpty(adminProgramRequest.getStatus())){
            adminProgramToSave.setStatus(adminProgramRequest.getStatus());
        }

        if(!StringUtils.isEmpty(adminProgramRequest.getGrantingAuthorityName())){
            GrantingAuthority grantingAuthority = gaRepository.findByGrantingAuthorityName(adminProgramRequest.getGrantingAuthorityName().trim());

            log.error("{} :: Public Authority and PAName ::{}", grantingAuthority,adminProgramRequest.getGrantingAuthorityName());

            if (Objects.isNull(grantingAuthority) ||
                    "Inactive".equals(grantingAuthority.getStatus())) {

                log.error("{} :: Public Authority is Inactive for the scheme");
                throw new InvalidRequestException("Public Authority is Inactive");
            }
            adminProgramToSave.setGrantingAuthority(grantingAuthority);
        }

        if(!StringUtils.isEmpty(adminProgramRequest.getAdminProgramName())){
            adminProgramToSave.setAdminProgramName(adminProgramRequest.getAdminProgramName());
        }

        if(!StringUtils.isEmpty(adminProgramRequest.getScNumber())){
            SubsidyMeasure sm = smRepository.findById(adminProgramRequest.getScNumber()).orElse(null);
            if(sm != null) {
                adminProgramToSave.setSubsidyMeasure(sm);
            }
        }
        adminProgramToSave.setCreatedTimestamp(LocalDateTime.now());
        adminProgramToSave.setLastModifiedTimestamp(LocalDateTime.now());


        AdminProgram savedAdminProgram = adminProgramRepository.save(adminProgramToSave);
        log.info("Scheme added successfully with Id : " + savedAdminProgram.getApNumber());
        return savedAdminProgram;
    }

    private Map<String, Long> schemeCounts(List<SubsidyMeasure> schemeList) {
        long allScheme = schemeList.size();
        long activeScheme = 0;
        long inactiveScheme = 0;
        long deletedScheme = 0;

        if(schemeList != null && schemeList.size() > 0){
            for(SubsidyMeasure sm : schemeList){
                if(sm.getStatus().equalsIgnoreCase(AccessManagementConstant.SM_ACTIVE)){
                    activeScheme++;
                }
                if(sm.getStatus().equalsIgnoreCase(AccessManagementConstant.SM_INACTIVE)){
                    inactiveScheme++;
                }
                if(sm.getStatus().equalsIgnoreCase(AccessManagementConstant.SM_DELETED)){
                    deletedScheme++;
                }
            }
        }
        Map<String, Long> smUserActivityCount = new HashMap<>();
        smUserActivityCount.put("allScheme",allScheme);
        smUserActivityCount.put("activeScheme",activeScheme);
        smUserActivityCount.put("inactiveScheme",inactiveScheme);
        smUserActivityCount.put("deletedScheme",deletedScheme);
        return smUserActivityCount;
    }

    private Long getGrantingAuthorityIdByName(String gaName) {
        GrantingAuthority gaObj = gaRepository.findByGrantingAuthorityName(gaName);
        if (gaObj != null) {
            return gaObj.getGaId();
        }
        return null;
    }
}
