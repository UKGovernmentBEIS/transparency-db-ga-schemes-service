package com.beis.subsidy.ga.schemes.dbpublishingservice.util;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.AuditLogs;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.AuditLogsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.UnauthorisedAccessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * 
 * Search Utility class 
 */
@Slf4j
public class SearchUtils {

	/**
	 * To check if input string is null or empty
	 *
	 * @param inputString - input string
	 * @return boolean - true or false
	 */
	public static boolean checkNullOrEmptyString(String inputString) {
		return inputString == null || inputString.trim().isEmpty();
	}

	/**
	 * To convert string date in format YYYY-MM-DD to LocalDate (without timezone)	
	 * @param inputStringDate - input string date
	 * @return
	 */
	public static LocalDate stringToDate(String inputStringDate) {
		return LocalDate.parse(inputStringDate);
	}

	/**
	 * To convert string date in format YYYY-MM-DD to DD FullMONTHNAME YYYY
	 * @param inputStringDate - input string date
	 * @return
	 */
	public static  String dateToFullMonthNameInDate(LocalDate inputStringDate) {
		log.info("input Date ::{}", inputStringDate);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy");
		return dateFormat.format(inputStringDate);
	}

	/**
	 * To convert Local DateTime to DD FullMONTHNAME YYYY
	 *
	 * @param inputDateTime - input string date
	 * @return
	 */
	public static String dateTimeToFullMonthNameInDate(LocalDateTime inputDateTime) {
		log.info("input Date ::{}", inputDateTime);
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss");
		return dateFormat.format(inputDateTime);
	}

	/**
	 * To convert BigDecimal to string by adding , for thousands.
	 * @param subsidyFullAmountExact
	 * @return
	 */
	public static String decimalNumberFormat(BigDecimal subsidyFullAmountExact) {
		DecimalFormat numberFormat = new DecimalFormat("###,###.##");
		return  numberFormat.format(subsidyFullAmountExact.longValue());
	}
	/**
	 * To convert Amount Range to by adding pound and , for thousands.
	 * @param amountRange
	 * @return formatted string
	 */
	public static String formatedFullAmountRange(String amountRange) {
		String finalAmtRange = "NA";
		if (StringUtils.isNotBlank(amountRange) &&
				!(amountRange.equalsIgnoreCase("NA") || amountRange.contains("N/A")
						|| amountRange.contains("n/a"))
				&& !amountRange.endsWith(">")) {

			StringBuilder format = new StringBuilder();
			String[] tokens = amountRange.split("-");
			if (tokens.length == 2) {
				finalAmtRange = format.append(convertDecimalValue(tokens[0]))
						.append(" - ")
						.append("£")
						.append(decimalNumberFormat(new BigDecimal(tokens[1].trim()))).toString();
			} else 	{
				finalAmtRange = new BigDecimal(amountRange).longValue() > 0 ? format.append("£")
						.append(decimalNumberFormat(new BigDecimal(amountRange))).toString() : "0";
			}

		} else if(StringUtils.isNotBlank(amountRange) && amountRange.endsWith(">")) {
			String removedLessThanVal = amountRange.substring(0, amountRange.length()-1).trim();
			finalAmtRange = "£"  + decimalNumberFormat(new BigDecimal(removedLessThanVal)) + " or more";
		}
		return  finalAmtRange;
	}
	public static String convertDecimalValue(String token){
		String formatNumber = "";
		if (!token.contains("NA/na")) {
			String removedLessThanVal = token.contains(">") ? token.substring(1, token.length()).trim() : token.trim();
			formatNumber = decimalNumberFormat(new BigDecimal(removedLessThanVal));
			if (token.contains(">")) {
				formatNumber = ">£" + formatNumber;
			} else {
				formatNumber = "£" + formatNumber;
			}
		}
		return formatNumber;
	}

	public static String getDurationInYears(BigInteger noOfdays){
		StringBuffer yearsStr = new StringBuffer();
		Integer days = noOfdays.intValue();
		int years = (days / 365);
		int months = ((days % 365) / 7)/4;
		int weeks = ((days % 365) / 7)%4;
		days  = (days % 365) % 7 ;
		if(years > 0 && years == 1){
			yearsStr.append(years+" year ");
		} else if(years >0 && years > 1){
			yearsStr.append(years+" years ");
		}if(months > 0 && months == 1){
			yearsStr.append(months+" month ");
		} else if(months > 0 && months > 1){
			yearsStr.append(months+" months ");
		}if(weeks > 0 && weeks == 1){
			yearsStr.append(weeks+" week ");
		} else if(weeks > 0 && weeks > 1){
			yearsStr.append(weeks+" weeks ");
		}if(days > 0 && days == 1){
			yearsStr.append(days+" day ");
		} else if(days > 0 && days > 1){
			yearsStr.append(days+" days ");
		}
		return yearsStr.toString();
	}
	
	public static  UserPrinciple beisAdminRoleValidation(ObjectMapper objectMapper,HttpHeaders userPrinciple,String entity) {
		UserPrinciple userPrincipleObj = null;
		String userPrincipleStr = userPrinciple.get("userPrinciple").get(0);
		try {
			
			userPrincipleObj = objectMapper.readValue(userPrincipleStr, UserPrinciple.class);
			if (!Arrays.asList(AccessManagementConstant.ADMIN_ROLES).contains(userPrincipleObj.getRole())) {
				throw new UnauthorisedAccessException("You are not authorised to " + entity);
			}
		} catch(JsonProcessingException exception){
			throw new UnauthorisedAccessException("Unauthorised exception");
		}
		return userPrincipleObj;
	}
	
	public static  UserPrinciple isAllRolesValidation(ObjectMapper objectMapper,HttpHeaders userPrinciple,String entity) {
		 UserPrinciple userPrincipleObj = null;
	        String userPrincipleStr = userPrinciple.get("userPrinciple").get(0);
	        try {
	            userPrincipleObj = objectMapper.readValue(userPrincipleStr, UserPrinciple.class);
	            if (!Arrays.asList(AccessManagementConstant.ROLES).contains(userPrincipleObj.getRole())) {
	                throw new UnauthorisedAccessException("You are not authorised to "+ entity);
	            }
	        } catch(JsonProcessingException exception){
	        	throw new UnauthorisedAccessException("Unauthorised exception");
	        }
	        return userPrincipleObj;
	    }
	
	
	public static  UserPrinciple isSchemeRoleValidation(ObjectMapper objectMapper,HttpHeaders userPrinciple,String entity) {
		UserPrinciple userPrincipleObj = null;
		String userPrincipleStr = userPrinciple.get("userPrinciple").get(0);
		try {
			
			userPrincipleObj = objectMapper.readValue(userPrincipleStr, UserPrinciple.class);
			if (!Arrays.asList(AccessManagementConstant.ADMIN_ROLES).contains(userPrincipleObj.getRole()) && !AccessManagementConstant.GA_APPROVER_ROLE.equalsIgnoreCase(userPrincipleObj.getRole()) ) {
				throw new UnauthorisedAccessException("You are not authorised to " + entity);
			}
		} catch(JsonProcessingException exception){
			throw new UnauthorisedAccessException("Unauthorised exception");
		}
		return userPrincipleObj;
	}

	public static void saveAuditLogForCreateGA(String userName, String action,String scOrGaId,
									String groupName,String eventMsg,AuditLogsRepository auditLogsRepository) {
		AuditLogs audit = new AuditLogs();
		try {
			audit.setUserName(userName);
			audit.setEventType(action);
			audit.setEventId(scOrGaId);
			audit.setEventMessage(eventMsg);
			audit.setGaName("Business Energy And Industrial Strategy");
			audit.setCreatedTimestamp(LocalDate.now());
			auditLogsRepository.save(audit);
		} catch(Exception e) {
			log.error("{} :: saveAuditLogForCreateGA failed to perform action", e);
		}
	}

	public static void saveAuditLog(UserPrinciple userPrinciple, String action,String scOrGaId,
									String eventMsg,AuditLogsRepository auditLogsRepository) {
		AuditLogs audit = new AuditLogs();
		try {
			String userName = userPrinciple.getUserName();
			audit.setUserName(userName);
			audit.setEventType(action);
			audit.setEventId(scOrGaId);
			audit.setEventMessage(eventMsg);
			audit.setGaName(userPrinciple.getGrantingAuthorityGroupName());
			audit.setCreatedTimestamp(LocalDate.now());
			auditLogsRepository.save(audit);
		} catch(Exception e) {
			log.error("{} :: saveAuditLog failed to perform action", e);
		}

	}
	public static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
}
