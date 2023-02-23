package com.beis.subsidy.ga.schemes.dbpublishingservice.repository;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.LegalBasis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LegalBasisRepository extends JpaRepository<LegalBasis, Long>, JpaSpecificationExecutor<LegalBasis> {

}
