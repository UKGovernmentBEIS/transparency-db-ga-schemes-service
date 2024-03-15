package com.beis.subsidy.ga.schemes.dbpublishingservice.util;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.Award;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * JPA specification for awards in relation to subsidy measures
 */
public final class AwardSpecificationUtils {

	
	/**
	 * To define specification for subsidy measure title
	 * 
	 * @param subsidyMeasureTitle - Add subsidy measure title
	 * @return Specification<Award> - Specification for Award
	 */
	public static Specification<Award> subsidyMeasureTitle(String subsidyMeasureTitle) {
	    return (root, query, builder) -> builder.like(root.get("subsidyMeasure").get("subsidyMeasureTitle"), contains(subsidyMeasureTitle));
	}
	
	/**
	 * To define specification for legal granting date range
	 * 
	 * @param fromLegalGrantingDate - Add legal granting from date 
	 * @param toLegalGrantingDate - Add legal granting to date 
	 * @return Specification<Award> - Specification for Award
	 */
	public static Specification<Award> legalGrantingDateRange(LocalDate fromLegalGrantingDate, LocalDate toLegalGrantingDate) {
	    return (root, query, builder) -> builder.between(root.get("legalGrantingDate"), fromLegalGrantingDate, toLegalGrantingDate);
	}
	
	/**
	 * To define specification for spending region
	 * 
	 * @param spendingRegions - List of spendingRegions
	 * @return Specification<Award> - Specification for Award
	 */
	public static Specification<Award> spendingRegionIn(List<String> spendingRegions) {
		return (root, query, builder) -> builder.or(spendingRegions
		        .stream()
		        .map(spendingRegion -> builder.equal(root.get("spendingRegion"), spendingRegion.trim()))
				.toArray(Predicate[]::new));
	}
	
	/**
	 * To define specification for subsidy objective
	 * 
	 * @param subsidyObjectives - List of subsidyObjectives
	 * @return Specification<Award> - Specification for Award
	 */
	public static Specification<Award> subsidyObjectiveIn(List<String> subsidyObjectives) {
		
		return (root, query, builder) -> builder.or(subsidyObjectives
		        .stream()
		        .map(subsidyObjective -> builder.equal(builder.lower(root.get("subsidyObjective")), subsidyObjective.toLowerCase().trim()))
				.toArray(Predicate[]::new));
	}

	public static Specification<Award> otherSubsidyObjective(List<String> subsidyObjectives) {

		return (root, query, builder) -> builder.or(subsidyObjectives
				.stream()
				.map(subsidyObjective ->  builder.like(builder.lower(root.get("subsidyObjective")), "%"+subsidyObjective.toLowerCase().trim()+"%"))
				.toArray(Predicate[]::new));
	}
	
	/**
	 * To define specification for spending sector 
	 * 
	 * @param spendingSectors - List of spendingSectors
	 * @return Specification<Award> - Specification for Award
	 */
	public static Specification<Award> spendingSectorIn(List<String> spendingSectors) {
		
		return (root, query, builder) -> builder.or(spendingSectors
		        .stream()
		        .map(spendingSector -> builder.equal(root.get("spendingSector"), spendingSector.trim()))
				.toArray(Predicate[]::new));
	}
	
	/**
	 * To define specification for subsidy instrument 
	 * 
	 * @param subsidyInstruments - List of subsidyInstruments
	 * @return Specification<Award> - Specification for Award
	 */
	public static Specification<Award> subsidyInstrumentIn(List<String> subsidyInstruments) {
		return (root, query, builder) -> builder.or(subsidyInstruments
		        .stream()
		        .map(subsidyInstrument -> builder.equal(builder.lower(root.get("subsidyInstrument")), subsidyInstrument.trim().toLowerCase()))
				.toArray(Predicate[]::new));
	}

	public static Specification<Award> otherSubsidyInstrumentIn(List<String> subsidyInstruments) {
		return (root, query, builder) -> builder.or(subsidyInstruments
				.stream()
				.map(subsidyInstrument -> builder.like(builder.lower(root.get("subsidyInstrument")),
						"%"+subsidyInstrument.toLowerCase().trim()+"%"))
				.toArray(Predicate[]::new));
	}

	/**
	 * To define specification for beneficiary name
	 * 
	 * @param beneficiaryName -beneficiaryName
	 * @return Specification<Award> - Specification for Award
	 */
	public static Specification<Award> beneficiaryName(String beneficiaryName) {

	    return (root, query, builder) -> builder.like(builder.lower(root.get("beneficiary").get("beneficiaryName")),
				builder.lower(builder.literal("%" + beneficiaryName.trim() + "%")));
	}

	/**
	 * To define specification for status
	 *
	 * @param status -status
	 * @return Specification<Award> - Specification for Award
	 */
	public static Specification<Award> status(String status) {
		return (root, query, builder) -> builder.like(root.get("status"), status);
	}


	public static Specification<Award> statusIn(List<String> statuses) {
		return (root, query, builder) -> builder.or(statuses
				.stream()
				.map(status -> builder.equal(builder.lower(root.get("status")), status.trim().toLowerCase()))
				.toArray(Predicate[]::new));
	}

	/**
	 * To check contains operations
	 * @param expression - input string
	 * @return - message format with like expression
	 */
	private static String contains(String expression) {
	    return MessageFormat.format("%{0}%", expression);
	}
}
