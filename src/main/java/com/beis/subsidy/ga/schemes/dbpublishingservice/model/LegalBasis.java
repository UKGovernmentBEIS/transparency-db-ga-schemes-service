package com.beis.subsidy.ga.schemes.dbpublishingservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * 
 * LegalBasis Entity Class
 *
 */
@Entity(name = "LEGAL_BASIS")
@AllArgsConstructor
@NoArgsConstructor
@SequenceGenerator(name = "legal_basis_seq", sequenceName = "legal_basis_seq",
		allocationSize = 1)
@Setter
@Getter
public class LegalBasis {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "legal_basis_seq")
	@Column(name="LEGAL_BASIS_ID")
	private Long legalBasisId;

	@OneToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "scNumber", nullable = false)
	private SubsidyMeasure subsidyMeasure;

	@Column(name="LEGAL_BASIS_TEXT")
	private String legalBasisText;

	@Column(name = "CREATED_BY")
	private String createdBy;
	
	@Column(name = "APPROVED_BY")
	private String approvedBy;
	
	@Column(name = "STATUS")
	private String status;
	
	@CreationTimestamp
	@Column(name = "CREATED_TIMESTAMP")
	private Date createdTimestamp;
	
	@UpdateTimestamp
	@Column(name = "LAST_MODIFIED_TIMESTAMP")
	private Date lastModifiedTimestamp;
}
