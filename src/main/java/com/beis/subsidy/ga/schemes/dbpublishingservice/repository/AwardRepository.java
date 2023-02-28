package com.beis.subsidy.ga.schemes.dbpublishingservice.repository;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.AdminProgram;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.Award;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AwardRepository extends JpaRepository<Award, Long> {

    List<Award> findByAdminProgram(AdminProgram adminProgram);
}
