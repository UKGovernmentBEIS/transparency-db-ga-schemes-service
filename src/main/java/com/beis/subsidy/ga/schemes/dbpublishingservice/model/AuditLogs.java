package com.beis.subsidy.ga.schemes.dbpublishingservice.model;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * Award Entity class
 */
@Entity(name = "AUDIT_LOGS")
@AllArgsConstructor
@NoArgsConstructor
@SequenceGenerator(name = "audit_logs_seq", sequenceName = "audit_logs_seq", allocationSize = 1)
@Setter
@Getter
public class AuditLogs {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_logs_seq")
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "USER_NAME")
	private String userName;

	@Column(name = "GA_NAME")
	private String gaName;

	@Column(name = "EVENT_TYPE")
	private String eventType;

	@Column(name = "EVENT_ID")
	private String eventId;
	
	@Column(name = "EVENT_MESSAGE")
	private String eventMessage;
	

	@CreationTimestamp
	@Column(name = "CREATED_TIMESTAMP")
	private LocalDate createdTimestamp;

}
