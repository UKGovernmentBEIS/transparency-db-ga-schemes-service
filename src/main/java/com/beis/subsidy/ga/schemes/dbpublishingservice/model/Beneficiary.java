package com.beis.subsidy.ga.schemes.dbpublishingservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 
 * Beneficiary Entity class
 *
 */
@Entity(name = "BENEFICIARY")
@AllArgsConstructor
@NoArgsConstructor
@SequenceGenerator(name = "beneficiary_read_seq",sequenceName="beneficiary_read_seq",allocationSize=1)
@Setter
@Getter
public class Beneficiary {

	@Id
	@Column(name="BENEFICIARY_ID")
	@GeneratedValue(strategy = GenerationType.AUTO,generator="beneficiary_read_seq")
	private Long beneficiaryId;

	@OneToMany(mappedBy="beneficiary",  cascade = CascadeType.ALL)
	private List<Award> awards;

	@Column(name = "BENEFICIARY_NAME")
	private String beneficiaryName;

	@Column(name = "BENEFICIARY_TYPE")
	private String beneficiaryType;

	@Column(name = "NATIONAL_ID")
	private String nationalId;

	@Column(name = "NATIONAL_ID_TYPE")
	private String nationalIdType;

	@Column(name = "SIC_CODE")
	private String sicCode;

	@Column(name = "SIZE_OF_ORG")
	private String orgSize;

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
