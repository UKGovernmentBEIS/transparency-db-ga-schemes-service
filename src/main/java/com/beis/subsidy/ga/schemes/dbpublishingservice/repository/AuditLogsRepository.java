package com.beis.subsidy.ga.schemes.dbpublishingservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.AuditLogs;



/**
 * Interface for Award repository to get award details from database 
 *
 */
public interface AuditLogsRepository extends JpaRepository<AuditLogs, Long>, JpaSpecificationExecutor<AuditLogs> {

	Page<AuditLogs> findByUserName(String  userName, Pageable pageable);
}
