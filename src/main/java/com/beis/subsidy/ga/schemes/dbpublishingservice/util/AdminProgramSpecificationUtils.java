package com.beis.subsidy.ga.schemes.dbpublishingservice.util;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.AdminProgram;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.SubsidyMeasure;
import org.springframework.data.jpa.domain.Specification;

import java.text.MessageFormat;

public final class AdminProgramSpecificationUtils {

	public static Specification<AdminProgram> adminProgramByGrantingAuthority(Long gaId) {
		return (root, query, builder) -> builder.equal(root.get("grantingAuthority").get("gaId"), gaId);
	}

	/**
	 * To define specification for admin program status
	 * @param status
	 * @return Specification<AdminProgram>
	 */
	public static Specification<AdminProgram> adminProgramByStatus(String status) {
		return (root, query, builder) -> builder.like(root.get("status"), status);
	}

	/**
	 * To define specification for admin program title
	 *
	 * @param name - Admin program name
	 * @return Specification<AdminProgram> - Specification for AdminProgram
	 */
	public static Specification<AdminProgram> adminProgramByName(String name) {
		return (root, query, builder) -> builder.like(builder.lower(root.get("adminProgramName")),
				builder.lower(builder.literal("%" + name.trim() + "%")));
	}

	/**
	 * To define specification for admin program by SC number
	 *
	 * @param scNumber - SC number
	 * @return Specification<AdminProgram> - Specification for AdminProgram
	 */
	public static Specification<AdminProgram> adminProgramByScNumber(String scNumber) {
		return (root, query, builder) -> builder.equal(builder.lower(root.get("subsidyMeasure").get("scNumber")), scNumber.toLowerCase());
	}

	/**
	 * To define specification for admin program by AP number
	 *
	 * @param apNumber - AP number
	 * @return Specification<AdminProgram> - Specification for AdminProgram
	 */
	public static Specification<AdminProgram> adminProgramByApNumber(String apNumber) {
		return (root, query, builder) -> builder.equal(builder.lower(root.get("apNumber")), apNumber.toLowerCase());
	}

	/**
	 * To check contains operations
	 * @param expression - input string
	 * @return - message format with like expression
	 */
	private static String contains(String expression) {
		return MessageFormat.format("%{0}%", expression);
	}


	public static Specification<AdminProgram> grantingAuthorityName(String searchName) {

		return (root, query, builder) -> builder.like(builder.lower(root.get("grantingAuthority").get("grantingAuthorityName")),
				builder.lower(builder.literal("%" + searchName.trim() + "%")));
	}
}
