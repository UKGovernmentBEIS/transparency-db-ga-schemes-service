package com.beis.subsidy.ga.schemes.dbpublishingservice.repository;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.AdminProgram;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.Award;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.SubsidyMeasure;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.AwardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AwardRepository extends JpaRepository<Award, Long>, JpaSpecificationExecutor<Award> {

    List<Award> findByAdminProgram(AdminProgram adminProgram);
    List<Award> findBySubsidyMeasure(SubsidyMeasure subsidyMeasure);
    Page<Award> findBySubsidyMeasure(SubsidyMeasure subsidyMeasure, Specification<Award> awardSpecification, Pageable pageable);
}
