package com.beis.subsidy.ga.schemes.dbpublishingservice.model;


import com.beis.subsidy.ga.schemes.dbpublishingservice.util.SequenceGenerator;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 
 * Admin Program entity class
 *
 */
@Entity(name = "ADMIN_PROGRAM")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AdminProgram {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "admin_program_read_seq")
	@GenericGenerator(
			name = "admin_program_read_seq",
			strategy = "com.beis.subsidy.ga.schemes.dbpublishingservice.util.SequenceGenerator",
			parameters = {
					@Parameter(name = SequenceGenerator.INCREMENT_PARAM, value = "1"),
					@Parameter(name = SequenceGenerator.VALUE_PREFIX_PARAMETER, value = "AP"),
					@Parameter(name = SequenceGenerator.NUMBER_FORMAT_PARAMETER, value = "%05d")})
	@Column(name="AP_NUMBER")
	private String apNumber;

	@ManyToOne(fetch=FetchType.EAGER , cascade = CascadeType.ALL)
	@JoinColumn(name = "gaId", nullable = false)
	private GrantingAuthority grantingAuthority;

	@Column(name = "ADMIN_PROGRAM_NAME")
	private String adminProgramName;

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name = "scNumber", nullable = false)
	private SubsidyMeasure subsidyMeasure;

	@Column(name = "BUDGET")
	private BigDecimal budget;

	@Column(name = "STATUS")
	private String status;

	@Column(name = "CREATED_BY")
	private String createdBy;

	@CreationTimestamp
	@Column(name = "CREATED_TIMESTAMP")
	private LocalDateTime createdTimestamp;

	@UpdateTimestamp
	@Column(name = "LAST_MODIFIED_TIMESTAMP")
	private LocalDateTime lastModifiedTimestamp;

	@Column(name = "DELETED_BY")
	private String deletedBy;

	@Column(name = "DELETED_TIMESTAMP", columnDefinition = "TIMESTAMP")
	private LocalDateTime deletedTimestamp;

	@OneToMany
	@JoinColumn(name = "apNumber")
	@OrderBy("awardNumber DESC")
	private List<Award> awardList;
}
