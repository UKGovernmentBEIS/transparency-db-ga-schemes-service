package com.beis.subsidy.ga.schemes.dbpublishingservice.util;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.Award;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.SubsidyMeasure;
import org.springframework.data.jpa.domain.Specification;

import java.text.MessageFormat;

public final class SchemeSpecificationUtils {

	public static Specification<SubsidyMeasure> schemeByGrantingAuthority(Long gaId) {
		return (root, query, builder) -> builder.equal(root.get("grantingAuthority").get("gaId"), gaId);
	}

	/**
	 * To define specification for award status
	 * @param status
	 * @return Specification<Award>
	 */
	public static Specification<SubsidyMeasure> schemeByStatus(String status) {
		return (root, query, builder) -> builder.equal(root.get("status"), status);
	}

	/**
	 * To define specification for subsidy measure title
	 *
	 * @param subsidySchemeName - Add subsidy measure title
	 * @return Specification<Award> - Specification for Award
	 */
	public static Specification<SubsidyMeasure> subsidySchemeName(String subsidySchemeName) {
		return (root, query, builder) -> builder.like(builder.lower(root.get("subsidyMeasureTitle")),
				builder.lower(builder.literal("%" + subsidySchemeName.trim() + "%")));
	}

	/**
	 * To define specification for subsidy measure title
	 *
	 * @param subsidyNumber - Add subsidy measure title
	 * @return Specification<Award> - Specification for Award
	 */
	public static Specification<SubsidyMeasure> subsidyNumber(String subsidyNumber) {
		return (root, query, builder) -> builder.equal(root.get("scNumber"), subsidyNumber);
	}
	public static Specification<SubsidyMeasure> scNumberSearch(String scNumber) {
		return (root, query, builder) -> builder.like(
				builder.lower(root.get("scNumber")),
				"%" + scNumber.trim().toLowerCase() + "%"
		);

	}
	/**
	 * To check contains operations
	 * @param expression - input string
	 * @return - message format with like expression
	 */
	private static String contains(String expression) {
		return MessageFormat.format("%{0}%", expression);
	}


	public static Specification<SubsidyMeasure> grantingAuthorityName(String searchName) {

		return (root, query, builder) -> builder.like(builder.lower(root.get("grantingAuthority").get("grantingAuthorityName")),
				builder.lower(builder.literal("%" + searchName.trim() + "%")));
	}
}
