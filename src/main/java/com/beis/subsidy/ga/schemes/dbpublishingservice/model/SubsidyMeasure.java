package com.beis.subsidy.ga.schemes.dbpublishingservice.model;


import com.beis.subsidy.ga.schemes.dbpublishingservice.util.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigInteger;
import java.time.LocalDate;

/**
 * 
 * Subsidy Measure entity class
 *
 */
@Entity(name = "SUBSIDY_MEASURE")
//@SequenceGenerator(name = "subsidy_control_read_seq", sequenceName = "subsidy_control_read_seq", allocationSize = 1)
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class SubsidyMeasure {

	//@GeneratedValue(generator = "system-uuid")
	//@GenericGenerator(name="system-uuid", strategy = "uuid")
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subsidy_control_read_seq")
	//@SequenceGenerator(name = "subsidy_control_read_seq", sequenceName = "subsidy_control_read_seq",
	//		allocationSize = 1)
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subsidy_control_read_seq")
	@GenericGenerator(
			name = "subsidy_control_read_seq",
			strategy = "com.beis.subsidy.ga.schemes.dbpublishingservice.util.SequenceGenerator",
			parameters = {
					@Parameter(name = SequenceGenerator.INCREMENT_PARAM, value = "1"),
					@Parameter(name = SequenceGenerator.VALUE_PREFIX_PARAMETER, value = "SC"),
					@Parameter(name = SequenceGenerator.NUMBER_FORMAT_PARAMETER, value = "%06d")})
	@Column(name="SC_NUMBER")
	private String scNumber;

	@ManyToOne(fetch=FetchType.EAGER , cascade = CascadeType.ALL)
	@JoinColumn(name = "ga_id", nullable = false, insertable = false, updatable = false)
	private GrantingAuthority grantingAuthority;

	@Column(name = "GA_ID")
	private Long gaId;

	@OneToOne(mappedBy="subsidyMeasure", cascade = CascadeType.PERSIST)
	@JoinColumn(name = "sc_number", nullable = false, insertable = false, updatable = false)
	private LegalBasis legalBases;

	@Column(name = "SUBSIDY_MEASURE_TITLE")
	private String subsidyMeasureTitle;

	@Column(name = "START_DATE")
	private LocalDate startDate;

	@Column(name = "END_DATE")
	private LocalDate endDate;

	@Column(name = "DURATION")
	private BigInteger duration;

	@Column(name = "BUDGET")
	private String budget;

	@Column(name = "ADHOC")
	private boolean adhoc;

	@Column(name = "GA_SUBSIDY_WEBLINK")
	private String gaSubsidyWebLink;

	@Column(name = "PUBLISHED_MEASURE_DATE")
	private LocalDate publishedMeasureDate;

	@Column(name = "CREATED_BY")
	private String createdBy;

	@Column(name = "APPROVED_BY")
	private String approvedBy;

	@Column(name = "STATUS")
	private String status;

	@Column(name = "GA_SUBSIDY_WEBLINK_DESCRIPTION")
	private String gaSubsidyWebLinkDescription;

	@CreationTimestamp
	@Column(name = "CREATED_TIMESTAMP")
	private LocalDate createdTimestamp;

	@UpdateTimestamp
	@Column(name = "LAST_MODIFIED_TIMESTAMP")
	private LocalDate lastModifiedTimestamp;
}
