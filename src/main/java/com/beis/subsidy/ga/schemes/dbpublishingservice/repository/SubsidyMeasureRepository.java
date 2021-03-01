package com.beis.subsidy.ga.schemes.dbpublishingservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.SubsidyMeasure;

/**
 * 
 * Interface for Subsidy Measure repository to get subsidy measure details from database 
 *
 */
public interface SubsidyMeasureRepository extends JpaRepository<SubsidyMeasure, String>, JpaSpecificationExecutor<SubsidyMeasure> {

	List<SubsidyMeasure>findBySubsidyMeasureTitle(String subsidyMeasureTitle);
}
