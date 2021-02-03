package com.beis.subsidy.ga.schemes.dbpublishingservice.util;

import java.text.MessageFormat;

import org.springframework.data.jpa.domain.Specification;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthority;

/**
 * 
 * To define specification on Award for Public Search Award functionality
 */
public final class GrantingAuthSpecificationUtils {

	/**
	 * To define specification for subsidy measure title
	 * 
	 * @param subsidyMeasureTitle
	 *            - Add subsidy measure title
	 * @return Specification<Award> - Specification for Granting Authority
	 */
	public static Specification<GrantingAuthority> grantingAuthorityId(Long gaId) {

		return (root, query, builder) -> builder.equal(root.get("gaId"), gaId);
	}

	/**
	 * To define specification for beneficiary name
	 * 
	 * @param beneficiaryName
	 *            -beneficiaryName
	 * @return Specification<Award> - Specification for Granting Authority
	 */
	public static Specification<GrantingAuthority> grantingAuthorityName(String grantingAuthorityName) {

		return (root, query, builder) -> builder.like(builder.lower(root.get("grantingAuthorityName")),
				builder.lower(builder.literal("%" + grantingAuthorityName.trim() + "%")));
	}

	/**
	 * To define specification for status
	 *
	 * @param status
	 *            -status
	 * @return Specification<Award> - Specification for Granting Authority
	 */
	public static Specification<GrantingAuthority> status(String status) {
		return (root, query, builder) -> builder.like(root.get("status"), status);
	}

	/**
	 * To check contains operations
	 * 
	 * @param expression
	 *            - input string
	 * @return - message format with like expression
	 */
	private static String contains(String expression) {
		return MessageFormat.format("%{0}%", expression);
	}

}
