package com.beis.subsidy.ga.schemes.dbpublishingservice.service.impl;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.BulkUploadSchemes;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.GrantingAuthorityRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.ValidationErrorResult;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.ValidationResult;
import com.beis.subsidy.ga.schemes.dbpublishingservice.service.BulkUploadSchemesService;
import com.beis.subsidy.ga.schemes.dbpublishingservice.service.SchemeService;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.AccessManagementConstant;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.ExcelHelper;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class BulkUploadSchemesServiceImpl implements BulkUploadSchemesService {
    @Autowired
    private SchemeService schemeService;

    @Autowired
    private GrantingAuthorityRepository gaRepository;

    private final HashMap<String, String> columnMapping = new HashMap<String, String>() {{
        put("Public authority name", "A");
        put("Subsidy scheme name", "B");
        put("Subsidies or Schemes of Interest (SSoI) or Subsidies or Schemes of Particular Interest (SSoPI)", "C");
        put("Subsidy scheme description", "D");
        put("Legal basis", "E");
        put("Public authority policy URL", "F");
        put("Public authority policy page description", "G");
        put("Budget (£)", "H");
        put("Maximum amount given under scheme", "I");
        put("Confirmation Date", "J");
        put("Start Date", "K");
        put("End Date", "L");
        put("Spending sectors", "M");
        put("Purpose", "N");
        put("Purpose - Other", "O");
    }};


    /*
     * the below method validate the excel file passed in request.
     */
    public ValidationResult validateFile(MultipartFile file, String role) {

        try {

            Boolean isLatestVersion = ExcelHelper.validateColumnCount(file.getInputStream());

            if (!isLatestVersion) {
                ValidationResult validationResult = new ValidationResult();

                ValidationErrorResult validationErrorResult = new ValidationErrorResult();
                validationErrorResult.setRow("All");
                validationErrorResult.setColumns("All");
                validationErrorResult.setErrorMessages("The version of the template being used is not up to date. Please re-download and use the latest version.");

                validationResult.setTotalRows(1);
                validationResult.setErrorRows(1);
                validationResult.setValidationErrorResult(Arrays.asList(validationErrorResult));

                return validationResult;
            }


            // Read Excel file
            List<BulkUploadSchemes> bulkUploadSchemes = ExcelHelper.excelToSchemes(file.getInputStream());

            log.info("Back from Excel to schemes...printed list of schemes - end");

            List<ValidationErrorResult> publicAuthorityNameErrorList = validatePublicAuthorityName(bulkUploadSchemes);

            List<ValidationErrorResult> subsidySchemeNameErrorList = validateSubsidySchemeName(bulkUploadSchemes);

            List<ValidationErrorResult> subsidySchemeInterestErrorList = validateSubsidySchemeInterest(bulkUploadSchemes);

            List<ValidationErrorResult> subsidySchemeDescriptionErrorList = validateSubsidySchemeDescription(bulkUploadSchemes);

            List<ValidationErrorResult> legalBasisErrorList = validateLegalBasis(bulkUploadSchemes);

            List<ValidationErrorResult> publicAuthorityPolicyURLErrorList = validatePublicAuthorityPolicyURL(bulkUploadSchemes);

            List<ValidationErrorResult> PublicAuthorityPolicyDescriptionErrorList = validatePublicAuthorityPolicyDescription(bulkUploadSchemes);

            List<ValidationErrorResult> subsidySchemeBudgetErrorList = validateSubsidySchemeBudget(bulkUploadSchemes);

            List<ValidationErrorResult> MaximumAmountGivenUnderSchemeErrorList = validateMaximumAmountGivenUnderScheme(bulkUploadSchemes);

            List<ValidationErrorResult> confirmationDateErrorList = validateConfirmationDate(bulkUploadSchemes);

            List<ValidationErrorResult> startDateErrorList = validateStartDate(bulkUploadSchemes);

            List<ValidationErrorResult> endDateErrorList = validateEndDate(bulkUploadSchemes);

            List<ValidationErrorResult> spendingSectorsErrorList = validateSpendingSectors(bulkUploadSchemes);

            List<ValidationErrorResult> purposeErrorList = validatePurpose(bulkUploadSchemes);

            List<ValidationErrorResult> validationErrorResultList = Stream.of(publicAuthorityNameErrorList,
                    subsidySchemeNameErrorList,subsidySchemeInterestErrorList, subsidySchemeDescriptionErrorList, legalBasisErrorList,
                    publicAuthorityPolicyURLErrorList, PublicAuthorityPolicyDescriptionErrorList,
                    subsidySchemeBudgetErrorList, MaximumAmountGivenUnderSchemeErrorList, confirmationDateErrorList,
                    startDateErrorList, endDateErrorList, spendingSectorsErrorList, purposeErrorList).flatMap(x -> x.stream()).collect(Collectors.toList());

            log.info("Final validation errors list ...printing list of errors - start");

            ValidationResult validationResult = new ValidationResult();
            validationResult.setValidationErrorResult(validationErrorResultList);
            validationResult.setTotalRows(bulkUploadSchemes.size());
            validationResult.setErrorRows(validationErrorResultList.size());
            validationResult.setMessage((validationErrorResultList.size() > 0) ? "Validation Errors in Uploaded file"
                    : "No errors in Uploaded file");

            log.info("Final validation Result object ...printing validationResult - start");

            if (validationResult.getValidationErrorResult().size() == 0) {

                log.info("No validation error in bulk excel template");

                schemeService.processBulkSchemes(bulkUploadSchemes,role);

                log.info("After calling process api - response = ");
                validationResult
                        .setMessage((true ? "All Schemes saved in Database" : "Error while saving schemes in Database"));
            }

            return validationResult;


        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    private List<ValidationErrorResult> validatePublicAuthorityName(List<BulkUploadSchemes> bulkUploadSchemes) {

        List<ValidationErrorResult> validationPublicAuthorityResultList = new ArrayList<>();

        List<BulkUploadSchemes> validatePublicAuthorityNameErrorLengthList = bulkUploadSchemes.stream()
                .filter(scheme -> ((scheme.getPublicAuthorityName() != null && scheme.getPublicAuthorityName().length() > 255))).collect(Collectors.toList());

        validationPublicAuthorityResultList.addAll(validatePublicAuthorityNameErrorLengthList.stream()
                .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Public authority name"),
                        "The public authority name must be 255 characters or less."))
                .collect(Collectors.toList()));


        List<BulkUploadSchemes> validatePublicAuthorityMissingErrorRecordsList = bulkUploadSchemes.stream()
                .filter(scheme -> (scheme.getPublicAuthorityName()==null)).collect(Collectors.toList());

        if(validatePublicAuthorityMissingErrorRecordsList.size() > 0) {
            validationPublicAuthorityResultList.addAll(validatePublicAuthorityMissingErrorRecordsList.stream()
                    .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Public authority name"),
                            "You must enter the name of the public authority."))
                    .collect(Collectors.toList()));
        }

        List<BulkUploadSchemes> publicAuthorityNameErrorRecordsList = bulkUploadSchemes.stream()
                .filter(scheme -> {
                    GrantingAuthority authority = gaRepository.findByGrantingAuthorityName(scheme.getPublicAuthorityName());
                    return authority == null;
                }).collect(Collectors.toList());

        validationPublicAuthorityResultList.addAll(publicAuthorityNameErrorRecordsList.stream()
                .map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Public authority name"),
                        "You must enter the name of an existing public authority."))
                .collect(Collectors.toList()));

        return validationPublicAuthorityResultList;

    }

    private List<ValidationErrorResult> validateSubsidySchemeName(List<BulkUploadSchemes> bulkUploadSchemes) {

        List<ValidationErrorResult> validationSubsidySchemeNameResultList = new ArrayList<>();

        List<BulkUploadSchemes> validateSubsidySchemeNameLengthList = bulkUploadSchemes.stream()
                .filter(scheme -> ((scheme.getSubsidySchemeName() != null && scheme.getSubsidySchemeName().length() > 255))).collect(Collectors.toList());

        validationSubsidySchemeNameResultList.addAll(validateSubsidySchemeNameLengthList.stream()
                .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Subsidy scheme name"),
                        "The subsidy scheme name must be 255 characters or less."))
                .collect(Collectors.toList()));

        List<BulkUploadSchemes> validateSubsidySchemeNameMissingErrorList = bulkUploadSchemes.stream()
                .filter(scheme -> (scheme.getSubsidySchemeName() == null)).collect(Collectors.toList());

        if (validateSubsidySchemeNameMissingErrorList.size() > 0){
            validationSubsidySchemeNameResultList.addAll(validateSubsidySchemeNameMissingErrorList.stream()
                    .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Subsidy scheme name"),
                            "You must enter the name of the subsidy scheme."))
                    .collect(Collectors.toList()));
        }

        return validationSubsidySchemeNameResultList;
    }

    private List<ValidationErrorResult> validateSubsidySchemeInterest(List<BulkUploadSchemes> bulkUploadSchemes) {
        List<ValidationErrorResult> validationSubsidySchemeInterestResultList = new ArrayList<>();

        Set<String> validOptions = new HashSet<>();
        validOptions.add("Subsidies or Schemes of Interest (SSoI)");
        validOptions.add("Subsidies or Schemes of Particular Interest (SSoPI)");
        validOptions.add("Neither");

        List<BulkUploadSchemes> validateSubsidySchemeInterestNotNullErrorList = bulkUploadSchemes.stream().filter(
                        scheme -> ((scheme.getSubsidySchemeInterest() == null || StringUtils.isEmpty(scheme.getSubsidySchemeInterest())
                                || !(validOptions.toString().contains(scheme.getSubsidySchemeInterest())))))
                .collect(Collectors.toList());

        validationSubsidySchemeInterestResultList.addAll(validateSubsidySchemeInterestNotNullErrorList.stream()
                .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Subsidies or Schemes of Interest (SSoI) or Subsidies or Schemes of Particular Interest (SSoPI)"),
                        "The subsidy scheme of interest or particular interest must be selected. This will be Subsidies or Schemes of Interest (SSoI), Subsidies or Schemes of Particular Interest (SSoPI) or Neither"))
                .collect(Collectors.toList()));

        return validationSubsidySchemeInterestResultList;
    }

    private List<ValidationErrorResult> validateSubsidySchemeDescription(List<BulkUploadSchemes> bulkUploadSchemes) {

        List<ValidationErrorResult> validationSubsidySchemeDescriptionResultList = new ArrayList<>();

        List<BulkUploadSchemes> validateSubsidySchemeDescriptionLengthList = bulkUploadSchemes.stream()
                .filter(scheme -> ((scheme.getSubsidySchemeDescription() != null && scheme.getSubsidySchemeDescription()
                        .length() > 10000))).collect(Collectors.toList());

        validationSubsidySchemeDescriptionResultList.addAll(validateSubsidySchemeDescriptionLengthList.stream()
                .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Subsidy scheme description"),
                        "The subsidy scheme description must be 10000 characters or less."))
                .collect(Collectors.toList()));

        List<BulkUploadSchemes> validateSubsidyDescriptionNameMissingErrorList = bulkUploadSchemes.stream()
                .filter(scheme -> (scheme.getSubsidySchemeDescription() == null)).collect(Collectors.toList());

        if (validateSubsidyDescriptionNameMissingErrorList.size() > 0){
            validationSubsidySchemeDescriptionResultList.addAll(validateSubsidyDescriptionNameMissingErrorList.stream()
                    .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Subsidy scheme description"),
                            "You must enter the subsidy scheme description."))
                    .collect(Collectors.toList()));
        }


        return validationSubsidySchemeDescriptionResultList;
    }

    private List<ValidationErrorResult> validateLegalBasis(List<BulkUploadSchemes> bulkUploadSchemes) {

        List<ValidationErrorResult> validationLegalBasisResultList = new ArrayList<>();

        List<BulkUploadSchemes> validateLegalBasisLengthList = bulkUploadSchemes.stream()
                .filter(scheme -> ((scheme.getLegalBasis() != null && scheme.getLegalBasis().length() > 5000))).collect(Collectors.toList());

        validationLegalBasisResultList.addAll(validateLegalBasisLengthList.stream()
                .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Legal basis"),
                        "The legal basis must be 5000 characters or less."))
                .collect(Collectors.toList()));

        List<BulkUploadSchemes> validateLegalBasisMissingErrorList = bulkUploadSchemes.stream()
                .filter(scheme -> (scheme.getLegalBasis() == null)).collect(Collectors.toList());

        if (validateLegalBasisMissingErrorList.size() > 0){
            validationLegalBasisResultList.addAll(validateLegalBasisMissingErrorList.stream()
                    .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Legal basis"),
                            "You must enter the legal basis."))
                    .collect(Collectors.toList()));
        }

        return validationLegalBasisResultList;
    }

    private List<ValidationErrorResult> validatePublicAuthorityPolicyURL(List<BulkUploadSchemes> bulkUploadSchemes) {

        List<ValidationErrorResult> validationPublicAuthorityPolicyURLResultList = new ArrayList<>();

        List<BulkUploadSchemes> validatePublicAuthorityPolicyURLLengthList = bulkUploadSchemes.stream()
                .filter(scheme -> ((scheme.getPublicAuthorityPolicyURL() != null && scheme.getPublicAuthorityPolicyURL().length() > 255)))
                .collect(Collectors.toList());

        validationPublicAuthorityPolicyURLResultList.addAll(validatePublicAuthorityPolicyURLLengthList.stream()
                .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Public authority policy URL"),
                        "The public authority policy URL must be 255 characters or less."))
                .collect(Collectors.toList()));

        List<BulkUploadSchemes> validatePublicAuthorityPolicyURLHasDescriptionList = bulkUploadSchemes.stream()
                .filter(scheme -> ((scheme.getPublicAuthorityPolicyURL() != null) && (scheme.getPublicAuthorityPolicyPageDescription() == null)))
                .collect(Collectors.toList());

        validationPublicAuthorityPolicyURLResultList.addAll(validatePublicAuthorityPolicyURLHasDescriptionList.stream()
                .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Public authority policy URL"),
                        "If you wish to provide a Public authority URL you must also provide a Public authority policy page description."))
                .collect(Collectors.toList()));

        return validationPublicAuthorityPolicyURLResultList;
    }

    private List<ValidationErrorResult> validatePublicAuthorityPolicyDescription(List<BulkUploadSchemes> bulkUploadSchemes) {

        List<ValidationErrorResult> validationPublicAuthorityPolicyDescriptionResultList = new ArrayList<>();

        List<BulkUploadSchemes> validatePublicAuthorityPolicyDescriptionLengthList = bulkUploadSchemes.stream()
                .filter(scheme -> ((scheme.getPublicAuthorityPolicyPageDescription() != null && scheme.getPublicAuthorityPolicyPageDescription().length() > 255)))
                .collect(Collectors.toList());

        validationPublicAuthorityPolicyDescriptionResultList.addAll(validatePublicAuthorityPolicyDescriptionLengthList.stream()
                .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Public authority policy page description"),
                        "The public authority policy page description must be 255 characters or less."))
                .collect(Collectors.toList()));

        List<BulkUploadSchemes> validatePublicAuthorityPolicyDescriptionHasURLList = bulkUploadSchemes.stream()
                .filter(scheme -> ((scheme.getPublicAuthorityPolicyURL() == null) && (scheme.getPublicAuthorityPolicyPageDescription() != null)))
                .collect(Collectors.toList());

        validationPublicAuthorityPolicyDescriptionResultList.addAll(validatePublicAuthorityPolicyDescriptionHasURLList.stream()
                .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Public authority policy page description"),
                        "If you wish to provide a Public authority policy page description you must also provide a Public authority URL."))
                .collect(Collectors.toList()));

        return validationPublicAuthorityPolicyDescriptionResultList;
    }


    private List<ValidationErrorResult> validateSubsidySchemeBudget(List<BulkUploadSchemes> bulkUploadSchemes) {
        List<ValidationErrorResult> validationSubsidySchemeBudgetResultList = new ArrayList<>();

        List<BulkUploadSchemes> validateSubsidySchemeBudgetMissingErrorList = bulkUploadSchemes.stream()
                .filter(scheme -> (scheme.getBudget() == null)).collect(Collectors.toList());

        if (validateSubsidySchemeBudgetMissingErrorList.size() > 0){
            validationSubsidySchemeBudgetResultList.addAll(validateSubsidySchemeBudgetMissingErrorList.stream()
                    .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Budget (£)"),
                            "You must enter the budget."))
                    .collect(Collectors.toList()));
        }

        List<BulkUploadSchemes> validatePositiveIntegerBudgetErrorList = bulkUploadSchemes.stream()
                .filter(scheme -> (scheme.getBudget().equals("Invalid") || (Float.parseFloat(scheme.getBudget()) < 0)
                        || !(Float.parseFloat(scheme.getBudget()) % 1 == 0))).collect(Collectors.toList());

        validationSubsidySchemeBudgetResultList.addAll(validatePositiveIntegerBudgetErrorList.stream()
                .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Budget (£)"),
                        "You must enter the budget as a positive whole number. The budget must contain only numbers and no letters or symbols"))
                .collect(Collectors.toList()));

        return validationSubsidySchemeBudgetResultList;

    }

    private List<ValidationErrorResult> validateMaximumAmountGivenUnderScheme(List<BulkUploadSchemes> bulkUploadSchemes) {

        List<ValidationErrorResult> validationMaximumAmountGivenUnderSchemeResultList = new ArrayList<>();

        List<BulkUploadSchemes> validateMaximumAmountGivenUnderSchemeList = bulkUploadSchemes.stream()
                .filter(scheme -> ((scheme.getMaximumAmountGivenUnderScheme() != null && scheme.getMaximumAmountGivenUnderScheme().length() > 255)))
                .collect(Collectors.toList());

        validationMaximumAmountGivenUnderSchemeResultList.addAll(validateMaximumAmountGivenUnderSchemeList.stream()
                .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Maximum amount given under scheme"),
                        "The maximum amount given under a scheme must be 255 characters or less."))
                .collect(Collectors.toList()));

        return validationMaximumAmountGivenUnderSchemeResultList;
    }


    private List<ValidationErrorResult> validateConfirmationDate(List<BulkUploadSchemes> bulkUploadSchemes) {

        List<ValidationErrorResult> validationConfirmationDateResultList = new ArrayList<>();

        List<BulkUploadSchemes> confirmationDateFormatErrorRecordsList = bulkUploadSchemes.stream()
                .filter(scheme -> (scheme.getConfirmationDate() == null)).collect(Collectors.toList());

        validationConfirmationDateResultList.addAll(confirmationDateFormatErrorRecordsList.stream()
                .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Confirmation Date"),
                        "You must enter a valid confirmation date of the subsidy scheme and it must be in the following format: DD-MM-YYYY."))
                .collect(Collectors.toList()));

        List<BulkUploadSchemes> confirmationDateInFutureErrorList = bulkUploadSchemes.stream()
                .filter(scheme -> (scheme.getConfirmationDate().isAfter(LocalDate.now()))).collect(Collectors.toList());

        validationConfirmationDateResultList.addAll(confirmationDateInFutureErrorList.stream()
                .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Confirmation Date"),
                        "Confirmation date cannot be in the future"))
                .collect(Collectors.toList()));

        return validationConfirmationDateResultList;
    }

    private List<ValidationErrorResult> validateStartDate(List<BulkUploadSchemes> bulkUploadSchemes) throws ParseException {
        List<ValidationErrorResult> validationStartDateResultList = new ArrayList<>();

        List<BulkUploadSchemes> StartDateFormatErrorRecordsList = bulkUploadSchemes.stream()
                .filter(scheme -> (scheme.getStartDate() == null)).collect(Collectors.toList());

        validationStartDateResultList.addAll(StartDateFormatErrorRecordsList.stream()
                .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Start Date"),
                        "You must enter a valid start date of the subsidy scheme and it must be in the following format: DD-MM-YYYY."))
                .collect(Collectors.toList()));

        List<BulkUploadSchemes> validateStartDateExceedEndDateErrorList = bulkUploadSchemes.stream()
                .filter(scheme -> (scheme.getStartDate() != null && scheme.getEndDate() != null &&
                        (scheme.getStartDate().isAfter(scheme.getEndDate())))).collect(Collectors.toList());


        validationStartDateResultList.addAll(validateStartDateExceedEndDateErrorList.stream()
                .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Start Date"),
                        "The start date of the scheme cannot be after the end date."))
                .collect(Collectors.toList()));

        return validationStartDateResultList;
    }


    private List<ValidationErrorResult> validateEndDate(List<BulkUploadSchemes> bulkUploadSchemes) {

        List<ValidationErrorResult> validationEndDateResultList = new ArrayList<>();

        DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        List<BulkUploadSchemes> EndDateFormatErrorRecordsList = bulkUploadSchemes.stream()
                .filter(scheme -> (!scheme.isHasNoEndDate() && scheme.getEndDate().equals(LocalDate.parse("01-01-0001", DATE_FORMAT))))
                .collect(Collectors.toList());

        validationEndDateResultList.addAll(EndDateFormatErrorRecordsList.stream()
                .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("End Date"),
                        "You must enter a valid end date of the subsidy scheme and it must be in the following format: DD-MM-YYYY."))
                .collect(Collectors.toList()));


        return validationEndDateResultList;
    }

    private List<ValidationErrorResult> validateSpendingSectors(List<BulkUploadSchemes> bulkUploadSchemes) {

        List<ValidationErrorResult> validationSpendingSectorsResultList = new ArrayList<>();

        List<BulkUploadSchemes> validateSpendingSectorsMissingErrorList = bulkUploadSchemes.stream()
                .filter(scheme -> (scheme.getSpendingSectors() == null)).collect(Collectors.toList());


        if (validateSpendingSectorsMissingErrorList.size() > 0){
            validationSpendingSectorsResultList.addAll(validateSpendingSectorsMissingErrorList.stream()
                    .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Spending sectors"),
                            "You must enter a minimum of one spending sector."))
                    .collect(Collectors.toList()));
        }


        List<BulkUploadSchemes> spendingSectorFormatErrorList = bulkUploadSchemes.stream()
                .filter(scheme -> (scheme.getSpendingSectors() != null &&
                        !(AccessManagementConstant.SPENDING_SECTORS.containsAll(Arrays.asList(scheme.getSpendingSectors().toLowerCase().trim().split("\\s*\\|\\s*"))))))
                .collect(Collectors.toList());


        ArrayList<String> spendingSectorsErrorList = new ArrayList<>();

        for (BulkUploadSchemes scheme : spendingSectorFormatErrorList) {
            ArrayList<String> spendingSectorsList = new ArrayList<>(Arrays.asList(scheme.getSpendingSectors().toLowerCase().trim().split("\\s*\\|\\s*")));
            for (int i = 0; i < spendingSectorsList.size(); i++) {
                if (!AccessManagementConstant.SPENDING_SECTORS.contains(spendingSectorsList.get(i))) {
                    String currentError = spendingSectorsList.get(i);
                    spendingSectorsErrorList.add(spendingSectorsList.get(i));
                    validationSpendingSectorsResultList.add(new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Spending sectors"),
                            "The following sectors are incorrect '" + currentError + "'. Check that the spelling and punctuation matches the spending sectors list."));
                }
            }
        }
        return validationSpendingSectorsResultList;
    }

    private List<ValidationErrorResult> validatePurpose(List<BulkUploadSchemes> bulkUploadSchemes) {

        List<ValidationErrorResult> validationPurposeResultList = new ArrayList<>();

//        List<BulkUploadSchemes> validatePurposeMissingErrorList = bulkUploadSchemes.stream()
//                .filter(scheme -> (scheme.getPurpose() == null)).collect(Collectors.toList());
//
//
//        if (validatePurposeMissingErrorList.size() > 0){
//            validationPurposeResultList.addAll(validatePurposeMissingErrorList.stream()
//                    .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Purpose"),
//                            "You must enter a minimum of one purpose."))
//                    .collect(Collectors.toList()));
//        }

//validation issue atm is getPurpose contains the "other -" entry so line 502 will mean non other entries will skip over the validation
        List<BulkUploadSchemes> purposeFormatErrorList = bulkUploadSchemes.stream()
                .filter(scheme -> {
                    if (scheme.getPurpose() != null) {
                        ArrayList<String> purposeList = new ArrayList<>(Arrays.asList(scheme.getPurpose().toLowerCase().trim().split("\\s*\\|\\s*")));
                        int otherIndex = -1;
                        for (int i = 0; i < purposeList.size(); i++) {
                            if(purposeList.get(i).startsWith("other")) {
                                otherIndex = i;
                            }
                        }
                        if(otherIndex >= 0){
                            purposeList.remove(otherIndex);
                        }
                        if(!Objects.equals(purposeList.get(0), "")) {
                            return (!new HashSet<>(AccessManagementConstant.PURPOSES).containsAll(purposeList));
                        }
                    }
                    return false;
                }).collect(Collectors.toList());


        ArrayList<String> purposeErrorList = new ArrayList<>();

        for (BulkUploadSchemes scheme : purposeFormatErrorList) {
            ArrayList<String> purposeList = new ArrayList<>(Arrays.asList(scheme.getPurpose().toLowerCase().trim().split("\\s*\\|\\s*")));
            for (int i = 0; i < purposeList.size(); i++) {
                if (!AccessManagementConstant.PURPOSES.contains(purposeList.get(i))) {
                    String currentError = purposeList.get(i);
                    purposeErrorList.add(purposeList.get(i));
                    validationPurposeResultList.add(new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Purpose"),
                            "The following purpose(s) are incorrect '" + currentError + "'. Check that the spelling and punctuation matches the purpose list."));
                }
            }
        }

        List<BulkUploadSchemes> validatePurposeOtherMissingErrorList = bulkUploadSchemes.stream()
                .filter(scheme -> (scheme.getPurposeOther() == null && scheme.getPurpose() == null)).collect(Collectors.toList());


        if (validatePurposeOtherMissingErrorList.size() > 0){
            validationPurposeResultList.addAll(validatePurposeOtherMissingErrorList.stream()
                    .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Purpose - Other"),
                            "You must enter a Purpose or Other Purpose."))
                    .collect(Collectors.toList()));
        }

        List<BulkUploadSchemes> validatePurposeOtherCharLimitErrorList = bulkUploadSchemes.stream()
                .filter(scheme -> (scheme.getPurposeOther() != null && scheme.getPurposeOther().length() > 255)).collect(Collectors.toList());


        if (validatePurposeOtherCharLimitErrorList.size() > 0){
            validationPurposeResultList.addAll(validatePurposeOtherCharLimitErrorList.stream()
                    .map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Purpose - Other"),
                            "You cannot have more than 255 characters for the Other Purpose field."))
                    .collect(Collectors.toList()));
        }
        return validationPurposeResultList;
    }
}
