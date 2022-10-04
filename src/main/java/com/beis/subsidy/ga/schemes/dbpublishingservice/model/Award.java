package com.beis.subsidy.ga.schemes.dbpublishingservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * Award Entity class
 */
@Entity(name = "AWARD")
@AllArgsConstructor
@NoArgsConstructor
@SequenceGenerator(name = "award_read_seq", sequenceName = "award_read_seq",
		allocationSize = 1)
@Setter
@Getter
public class Award {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "award_read_seq")
	@Column(name="AWARD_NUMBER")
	private Long awardNumber;

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name = "beneficiaryId", nullable = false, insertable = false, updatable = false)
	private Beneficiary beneficiary;

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name = "gaId", nullable = false, insertable = false, updatable = false)
	private GrantingAuthority grantingAuthority;

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name = "scNumber", insertable = false, updatable = false)
	private SubsidyMeasure subsidyMeasure;

	@Column(name = "SUBSIDY_ELEMENT_FULL_AMOUNT_RANGE")
	private String subsidyFullAmountRange;

	@Column(name = "SUBSIDY_ELEMENT_FULL_AMOUNT_EXACT")
	private BigDecimal subsidyFullAmountExact;

	@Column(name = "SUBSIDY_OBJECTIVE")
	private String subsidyObjective;

	@Column(name = "GOOD_SERVICES_FILTER")
	private String goodsServicesFilter;

	@Column(name = "LEGAL_GRANTING_DATE")
	private LocalDate legalGrantingDate;

	@Column(name = "PUBLISHED_AWARD_DATE")
	private LocalDate publishedAwardDate;

	@Column(name = "SPENDING_REGION")
	private String spendingRegion;

	@Column(name = "SUBSIDY_INSTRUMENT")
	private String subsidyInstrument;

	@Column(name = "SPENDING_SECTOR")
	private String spendingSector;

	@Column(name = "CREATED_BY")
	private String createdBy;

	@Column(name = "APPROVED_BY")
	private String approvedBy;

	@Column(name = "STATUS")
	private String status;

	@Column(name = "REASON")
	private String reason;

	@CreationTimestamp
	@Column(name = "CREATED_TIMESTAMP")
	private LocalDate createdTimestamp;

	@UpdateTimestamp
	@Column(name = "LAST_MODIFIED_TIMESTAMP")
	private LocalDate lastModifiedTimestamp;

	@Column(name = "STANDALONE_AWARD")
	private String standaloneAward;

	@Column(name = "SUBSIDY_AWARD_DESCRIPTION")
	private String subsidyAwardDescription;
}
