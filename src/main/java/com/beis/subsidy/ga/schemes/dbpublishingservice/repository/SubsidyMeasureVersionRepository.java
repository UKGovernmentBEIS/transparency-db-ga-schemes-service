package com.beis.subsidy.ga.schemes.dbpublishingservice.repository;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.SubsidyMeasureVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * 
 * Interface for Subsidy Measure repository to get subsidy measure details from database 
 *
 */
public interface SubsidyMeasureVersionRepository extends JpaRepository<SubsidyMeasureVersion, String> {

    SubsidyMeasureVersion findByScNumberAndVersion(String scNumber, UUID version);
}
