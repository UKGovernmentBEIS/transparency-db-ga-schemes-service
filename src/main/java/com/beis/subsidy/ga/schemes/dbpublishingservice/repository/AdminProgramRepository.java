package com.beis.subsidy.ga.schemes.dbpublishingservice.repository;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.AdminProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 
 * Interface for Subsidy Measure repository to get subsidy measure details from database 
 *
 */
public interface AdminProgramRepository extends JpaRepository<AdminProgram, String>, JpaSpecificationExecutor<AdminProgram> {

}
