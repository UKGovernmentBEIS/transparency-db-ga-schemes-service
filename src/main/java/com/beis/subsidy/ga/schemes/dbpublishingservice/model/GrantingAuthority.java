package com.beis.subsidy.ga.schemes.dbpublishingservice.model;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Entity(name = "GRANTING_AUTHORITY")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SequenceGenerator(name = "granting_authority_seq", sequenceName = "granting_authority_seq",
allocationSize = 1)
public class GrantingAuthority {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "granting_authority_seq")
	@Column(name="GA_ID")
	private Long gaId;
	
	@Column(name = "GA_NAME")
	private String grantingAuthorityName;

	@Column(name = "CREATED_BY")
	private String createdBy;
	
	@Column(name = "APPROVED_BY")
	private String approvedBy;
	
	@Column(name = "STATUS")
	private String status;
	
	@CreationTimestamp
	@Column(name = "CREATED_TIMESTAMP")
	private LocalDate createdTimestamp;
	
	@UpdateTimestamp
	@Column(name = "LAST_MODIFIED_TIMESTAMP")
	private LocalDate lastModifiedTimestamp;

}
