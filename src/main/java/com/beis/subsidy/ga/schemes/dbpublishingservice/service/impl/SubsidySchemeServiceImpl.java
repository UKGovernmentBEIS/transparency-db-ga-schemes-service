package com.beis.subsidy.ga.schemes.dbpublishingservice.service.impl;


import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.InvalidRequestException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.SearchResultNotFoundException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.UnauthorisedAccessException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.Award;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.LegalBasis;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.SubsidyMeasure;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.AwardRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.AwardSearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.SubsidyMeasureVersion;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.GrantingAuthorityRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.SubsidyMeasureRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.SubsidyMeasureVersionRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeDetailsRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeSearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.*;
import com.beis.subsidy.ga.schemes.dbpublishingservice.service.SubsidySchemeService;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import java.math.BigInteger;
import java.util.*;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubsidySchemeServiceImpl implements SubsidySchemeService {
    @Autowired
    private SubsidyMeasureRepository subsidyMeasureRepository;

    @Autowired
    private SubsidyMeasureVersionRepository subsidyMeasureVersionRepository;

    @Autowired
    private GrantingAuthorityRepository gaRepository;

    @Autowired
    private AwardRepository awardRepository;

    @Override
    public SearchSubsidyResultsResponse findMatchingSubsidySchemeDetails(SchemeSearchInput searchInput,  UserPrinciple userPriniciple) {
        log.info("Inside findMatchingSubsidySchemeDetails searchName : " + searchInput.getSearchName());

        Specification<SubsidyMeasure> schemeSpecifications = getSpecificationSchemeDetails(searchInput);
        Specification<SubsidyMeasure> schemeSpecificationsWithout = getSpecificationSchemeDetailsWithoutStatus(searchInput);
        List<SubsidyMeasure> totalSchemeList = new ArrayList<>();
        List<SubsidyMeasure> schemeResults = null;
        Page<SubsidyMeasure> pageAwards = null;
        List<Sort.Order> orders = getOrderByCondition(searchInput.getSortBy());

        Pageable pagingSortSchemes = PageRequest.of(searchInput.getPageNumber() - 1,
                searchInput.getTotalRecordsPerPage(), Sort.by(orders));


        if (AccessManagementConstant.BEIS_ADMIN_ROLE.equals(userPriniciple.getRole().trim())) {

            pageAwards = subsidyMeasureRepository.findAll(schemeSpecifications, pagingSortSchemes);

            schemeResults = pageAwards.getContent();

            if(!StringUtils.isEmpty(searchInput.getSearchName())){
                totalSchemeList = subsidyMeasureRepository.findAll(schemeSpecificationsWithout);
            } else {
                totalSchemeList = subsidyMeasureRepository.findAll();
            }

        } else {
            if (!StringUtils.isEmpty(searchInput.getSearchName())
                    || !StringUtils.isEmpty(searchInput.getStatus())) {

                schemeSpecifications = getSpecificationSchemeDetailsForGARoles(searchInput,userPriniciple.getGrantingAuthorityGroupName());
                pageAwards = subsidyMeasureRepository.findAll(schemeSpecifications, pagingSortSchemes);

                schemeResults = pageAwards.getContent();
                totalSchemeList = subsidyMeasureRepository.findAll(schemeSpecifications);

            } else {

                Long gaId = getGrantingAuthorityIdByName(userPriniciple.getGrantingAuthorityGroupName());
                if(gaId == null || gaId <= 0){
                    throw new UnauthorisedAccessException("Invalid public authority name");
                }
                pageAwards = subsidyMeasureRepository.
                        findAll(subsidyMeasureByGrantingAuthority(gaId),pagingSortSchemes);
                schemeResults = pageAwards.getContent();
                totalSchemeList = subsidyMeasureRepository.findAll(subsidyMeasureByGrantingAuthority(gaId));

            }
        }

        SearchSubsidyResultsResponse searchResults = null;

        if (!schemeResults.isEmpty()) {
            searchResults = new SearchSubsidyResultsResponse(schemeResults, pageAwards.getTotalElements(),
                    pageAwards.getNumber() + 1, pageAwards.getTotalPages(), schemeCounts(totalSchemeList));
        } else {
            log.info("{}::Scheme results not found");
            throw new SearchResultNotFoundException("Scheme Results NotFound");
        }
        return searchResults;
    }


  public BigInteger getDuration(LocalDate startDate, LocalDate endDate) {
        long noOfDaysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        return BigInteger.valueOf(noOfDaysBetween);
  }

  @Override
  public String addSubsidySchemeDetails(SchemeDetailsRequest scheme) {

        log.info("Inside addSubsidySchemeDetails method :");
        SubsidyMeasure schemeToSave = new SubsidyMeasure();
        LegalBasis legalBasis = new LegalBasis();
        if(!StringUtils.isEmpty(scheme.getSubsidyMeasureTitle())){
            schemeToSave.setSubsidyMeasureTitle(scheme.getSubsidyMeasureTitle());
        }
        schemeToSave.setApprovedBy("SYSTEM");
        schemeToSave.setCreatedBy("SYSTEM");
        if(!StringUtils.isEmpty(scheme.getBudget())){
            schemeToSave.setBudget(scheme.getBudget());
        }
        if(!StringUtils.isEmpty(scheme.getMaximumAmountUnderScheme())) {
            schemeToSave.setMaximumAmountUnderScheme(scheme.getMaximumAmountUnderScheme());
        }
        if(scheme.isHasNoEndDate() || !scheme.isHasNoEndDate()){
          schemeToSave.setHasNoEndDate(scheme.isHasNoEndDate());
          schemeToSave.setEndDate(null);
        }
        if(!scheme.isAdhoc() && scheme.getStartDate() != null && scheme.getEndDate() != null && !scheme.isHasNoEndDate()){
            schemeToSave.setDuration(getDuration(scheme.getStartDate(), scheme.getEndDate()));
        } else if(scheme.getStartDate() != null && scheme.isAdhoc()) {
            schemeToSave.setDuration(BigInteger.ONE);
        } else {
            schemeToSave.setDuration(BigInteger.ZERO);
        }
        if(scheme.getStartDate() != null){
            schemeToSave.setStartDate(scheme.getStartDate());
        }
        if(scheme.isAdhoc()){
            schemeToSave.setEndDate(scheme.getStartDate());
        } else if (scheme.getEndDate().equals(LocalDate.of(9999, 12, 31))) {
            schemeToSave.setEndDate(null);
        } else {
            schemeToSave.setEndDate(scheme.getEndDate());
        }

        schemeToSave.setGaSubsidyWebLink(scheme.getGaSubsidyWebLink());
        schemeToSave.setGaSubsidyWebLinkDescription(scheme.getGaSubsidyWebLinkDescription());

        if(scheme.isAdhoc() || !scheme.isAdhoc()){
            schemeToSave.setAdhoc(scheme.isAdhoc());
        }
        if(!StringUtils.isEmpty(scheme.getStatus())){
            schemeToSave.setStatus(scheme.getStatus());
        }
        if(scheme.getConfirmationDate() != null){
            schemeToSave.setConfirmationDate(scheme.getConfirmationDate());
        }
        if(!StringUtils.isEmpty(scheme.getGaName())){
            GrantingAuthority grantingAuthority = gaRepository.findByGrantingAuthorityName(scheme.getGaName().trim());

            log.error("{} :: Public Authority and PAName ::{}", grantingAuthority,scheme.getGaName());

            if (Objects.isNull(grantingAuthority) ||
                    "Inactive".equals(grantingAuthority.getStatus())) {

               log.error("{} :: Public Authority is Inactive for the scheme");
               throw new InvalidRequestException("Public Authority is Inactive");
            }
            schemeToSave.setGaId(grantingAuthority.getGaId());
        }
        schemeToSave.setPublishedMeasureDate(LocalDate.now());

        if(!StringUtils.isEmpty(scheme.getLegalBasisText())){
            legalBasis.setLegalBasisText(scheme.getLegalBasisText());
        }
        schemeToSave.setLastModifiedTimestamp(LocalDateTime.now());

        if(!StringUtils.isEmpty(scheme.getSubsidySchemeDescription())) {
            if(scheme.getSubsidySchemeDescription().length() > 10000) {
                log.error("Subsidy scheme description must be less than 10000 characters");
                throw new InvalidRequestException("Subsidy scheme description must be less than 10000 characters");
            }

            schemeToSave.setSubsidySchemeDescription(scheme.getSubsidySchemeDescription());
        }
      if(!StringUtils.isEmpty(scheme.getSpecificPolicyObjective())) {
          if(scheme.getSpecificPolicyObjective().length() > 1500) {
              log.error("Specific policy objective must be less than 1500 characters");
              throw new InvalidRequestException("Specific policy objective must be less than 1500 characters");
          }

          schemeToSave.setSpecificPolicyObjective(scheme.getSpecificPolicyObjective());
      }
        if(!StringUtils.isEmpty(scheme.getSpendingSectorJson())){
            schemeToSave.setSpendingSectors(scheme.getSpendingSectorJson());
        }

      if(!StringUtils.isEmpty(scheme.getPurposeJson())){
          schemeToSave.setPurpose(scheme.getPurposeJson());
      }

        if(!StringUtils.isEmpty(scheme.getSubsidySchemeInterest())){
          schemeToSave.setSubsidySchemeInterest(scheme.getSubsidySchemeInterest());
        }

        legalBasis.setLastModifiedTimestamp(new Date());
        legalBasis.setStatus("Active");
        legalBasis.setCreatedBy("SYSTEM");
        legalBasis.setApprovedBy("SYSTEM");
        legalBasis.setCreatedTimestamp(new Date());
        schemeToSave.setLegalBases(legalBasis);
        legalBasis.setSubsidyMeasure(schemeToSave);

        SubsidyMeasure savedScheme = subsidyMeasureRepository.save(schemeToSave);
        log.info("Scheme added successfully with Id : "+savedScheme.getScNumber());
        return savedScheme.getScNumber();
  }

   @Override
   public String updateSubsidySchemeDetails(SchemeDetailsRequest scheme, String scNumber, UserPrinciple userPrinciple) {
        log.info("Inside updateSubsidySchemeDetails method - sc number " + scheme.getScNumber());
        SubsidyMeasure schemeById = subsidyMeasureRepository.findById(scNumber).get();

       subsidyMeasureSaveOldVersion(schemeById);

        LegalBasis legalBasis = schemeById.getLegalBases();
        if (Objects.isNull(schemeById)) {
            throw new SearchResultNotFoundException("Scheme details not found::" + scheme.getScNumber());
        }
        if(!StringUtils.isEmpty(scheme.getSubsidyMeasureTitle())){
            schemeById.setSubsidyMeasureTitle(scheme.getSubsidyMeasureTitle());
        }
        if(!StringUtils.isEmpty(scheme.getApprovedBy())){
            schemeById.setApprovedBy(scheme.getApprovedBy());
        }
        if(!StringUtils.isEmpty(scheme.getBudget())){
            schemeById.setBudget(scheme.getBudget());
        }
        if(!StringUtils.isEmpty(scheme.getMaximumAmountUnderScheme())){
            schemeById.setMaximumAmountUnderScheme(scheme.getMaximumAmountUnderScheme());
        }
        if(!StringUtils.isEmpty(scheme.getCreatedBy())){
            schemeById.setCreatedBy(scheme.getCreatedBy());
        }
        if(!scheme.isAdhoc() && scheme.getStartDate() != null && scheme.getEndDate() != null){
            schemeById.setDuration(getDuration(scheme.getStartDate(), scheme.getEndDate()));
        } else if(scheme.getStartDate() != null && scheme.isAdhoc()) {
            schemeById.setDuration(BigInteger.ONE);
        }
        if(scheme.getStartDate() != null){
            schemeById.setStartDate(scheme.getStartDate());
        }
        if(scheme.isAdhoc()){
            schemeById.setEndDate(scheme.getStartDate());
        } else if(scheme.getEndDate() != null){
            schemeById.setEndDate(scheme.getEndDate());
        }

        schemeById.setGaSubsidyWebLink(scheme.getGaSubsidyWebLink());
        schemeById.setGaSubsidyWebLinkDescription(scheme.getGaSubsidyWebLinkDescription());

        if(scheme.isAdhoc() || !scheme.isAdhoc()){
            schemeById.setAdhoc(scheme.isAdhoc());
        }
        if(!StringUtils.isEmpty(scheme.getStatus())){
            schemeById.setStatus(scheme.getStatus());
            if(!scheme.getStatus().equalsIgnoreCase("active"))
                schemeById.setReason(scheme.getReason());

            if(scheme.getStatus().equals("Deleted")){
                schemeById.setDeletedBy(userPrinciple.getUserName());
                schemeById.setDeletedTimestamp(LocalDateTime.now());
            }
        }
        if(!StringUtils.isEmpty(scheme.getLegalBasisText())){
            legalBasis.setLegalBasisText(scheme.getLegalBasisText());
        }
        if(!StringUtils.isEmpty(scheme.getSubsidySchemeDescription())){
            schemeById.setSubsidySchemeDescription(scheme.getSubsidySchemeDescription());
        }

       if(!StringUtils.isEmpty(scheme.getSpecificPolicyObjective())){
           schemeById.setSpecificPolicyObjective(scheme.getSpecificPolicyObjective());
       }
        if(scheme.getConfirmationDate() != null){
            schemeById.setConfirmationDate(scheme.getConfirmationDate());
        }
        if(!StringUtils.isEmpty(scheme.getSpendingSectorJson())){
           schemeById.setSpendingSectors(scheme.getSpendingSectorJson());
        }

       if(!StringUtils.isEmpty(scheme.getPurposeJson())){
           schemeById.setPurpose(scheme.getPurposeJson());
       }

        schemeById.setHasNoEndDate(scheme.isHasNoEndDate());

        if(scheme.isHasNoEndDate()){
            schemeById.setEndDate(null);
        }


        schemeById.setLastModifiedTimestamp(LocalDateTime.now());


        legalBasis.setLastModifiedTimestamp(new Date());
        legalBasis.setCreatedTimestamp(new Date());
        schemeById.setLegalBases(legalBasis);
        legalBasis.setSubsidyMeasure(schemeById);
        schemeById.setSubsidySchemeInterest(scheme.getSubsidySchemeInterest());

        SubsidyMeasure updatedScheme = subsidyMeasureRepository.save(schemeById);
        log.info("Updated successfully : ");
        return updatedScheme.getScNumber();
    }

    private void subsidyMeasureSaveOldVersion(SubsidyMeasure scheme) {
        SubsidyMeasureVersion version = new SubsidyMeasureVersion(scheme);
        subsidyMeasureVersionRepository.save(version);
    }

    @Override
    public SubsidyMeasureResponse findSubsidySchemeById(String scNumber) {
        SubsidyMeasure subsidyMeasure = subsidyMeasureRepository.findById(scNumber).get();
        return new SubsidyMeasureResponse(subsidyMeasure);
    }

    @Override
    public SubsidyMeasureResponse findSubsidySchemeWithAwardsById(String scNumber, AwardSearchInput awardSearchInput) {
        SubsidyMeasure subsidyMeasure = subsidyMeasureRepository.findById(scNumber).get();

        List<Sort.Order> orders = getOrderByCondition(awardSearchInput.getSortBy());
        Pageable awardsPageable = PageRequest.of(awardSearchInput.getPageNumber() - 1,
                awardSearchInput.getTotalRecordsPerPage(), Sort.by(orders));
        SearchResults<AwardResponse> searchResults = new SearchResults<>();
        Page<Award> page = awardRepository.findAll(getSpecificationAwardDetails(awardSearchInput),awardsPageable);
        searchResults.setResponseList(page.getContent().stream().map(award -> new AwardResponse(award, true)).collect(Collectors.toList()));
        searchResults.totalSearchResults = subsidyMeasure.getAwardList().size();
        searchResults.totalPages = (int) Math.ceil((double)subsidyMeasure.getAwardList().size() / awardSearchInput.getTotalRecordsPerPage());
        searchResults.currentPage = awardSearchInput.getPageNumber();
        SubsidyMeasureResponse subsidyMeasureResponse = new SubsidyMeasureResponse(subsidyMeasure);
        subsidyMeasureResponse.setAwardSearchResults(searchResults);
        return subsidyMeasureResponse;
    }

    @Override
    public SubsidyMeasureVersionResponse findSubsidySchemeVersion(String scNumber, String version) {
        SubsidyMeasureVersion schemeVersion = subsidyMeasureVersionRepository.findByScNumberAndVersion(scNumber, UUID.fromString(version));

        return new SubsidyMeasureVersionResponse(schemeVersion);
    }

    private Map<String, Long> schemeCounts(List<SubsidyMeasure> schemeList) {
        long allScheme = schemeList.size();
        long activeScheme = 0;
        long inactiveScheme = 0;
        long deletedScheme = 0;

        if(schemeList != null && schemeList.size() > 0){
            for(SubsidyMeasure sm : schemeList){
                if(sm.getStatus().equalsIgnoreCase(AccessManagementConstant.SM_ACTIVE)){
                    activeScheme++;
                }
                if(sm.getStatus().equalsIgnoreCase(AccessManagementConstant.SM_INACTIVE)){
                    inactiveScheme++;
                }
                if(sm.getStatus().equalsIgnoreCase(AccessManagementConstant.SM_DELETED)){
                    deletedScheme++;
                }
            }
        }
        Map<String, Long> smUserActivityCount = new HashMap<>();
        smUserActivityCount.put("allScheme",allScheme);
        smUserActivityCount.put("activeScheme",activeScheme);
        smUserActivityCount.put("inactiveScheme",inactiveScheme);
        smUserActivityCount.put("deletedScheme",deletedScheme);
        return smUserActivityCount;
    }

    public Specification<SubsidyMeasure>  getSpecificationSchemeDetails(SchemeSearchInput searchInput) {
        String searchName = searchInput.getSearchName();
        Specification<SubsidyMeasure> schemeSpecifications = Specification
                .where(
                        SearchUtils.checkNullOrEmptyString(searchName)
                                ? null : SchemeSpecificationUtils.subsidySchemeName(searchName.trim())
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null : SchemeSpecificationUtils.subsidyNumber(searchName.trim()))
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null :SchemeSpecificationUtils.grantingAuthorityName(searchName.trim()))
                )
                .and(SearchUtils.checkNullOrEmptyString(searchInput.getStatus())
                        ? null : SchemeSpecificationUtils.schemeByStatus(searchInput.getStatus().trim()));
        return schemeSpecifications;
    }
    public Specification<SubsidyMeasure>  getSpecificationSchemeDetailsWithoutStatus(SchemeSearchInput searchInput) {
        String searchName = searchInput.getSearchName();
        Specification<SubsidyMeasure> schemeSpecifications = Specification
                .where(
                        SearchUtils.checkNullOrEmptyString(searchName)
                                ? null : SchemeSpecificationUtils.subsidySchemeName(searchName.trim())
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null : SchemeSpecificationUtils.subsidyNumber(searchName.trim()))
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null :SchemeSpecificationUtils.grantingAuthorityName(searchName.trim()))
                );
        return schemeSpecifications;
    }

    public Specification<SubsidyMeasure>  getSpecificationSchemeDetailsForGARoles(SchemeSearchInput searchInput, String gaName) {
        String searchName = searchInput.getSearchName();
        Specification<SubsidyMeasure> schemeSpecifications = Specification
                .where(
                        SearchUtils.checkNullOrEmptyString(searchName)
                                ? null : SchemeSpecificationUtils.subsidySchemeName(searchName.trim())
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null : SchemeSpecificationUtils.subsidyNumber(searchName.trim()))
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                 ? null :SchemeSpecificationUtils.grantingAuthorityName(searchName.trim()))

                )
                .and(SearchUtils.checkNullOrEmptyString(gaName)
                        ? null :SchemeSpecificationUtils.grantingAuthorityName(gaName.trim()))
                .and(SearchUtils.checkNullOrEmptyString(searchInput.getStatus())
                        ? null :SchemeSpecificationUtils.schemeByStatus(searchInput.getStatus().trim()));
        return schemeSpecifications;
    }

    private List<Sort.Order> getOrderByCondition(String[] sortBy) {
        List<Sort.Order> orders = new ArrayList<Sort.Order>();
        if (sortBy != null && sortBy.length > 0 && sortBy[0].contains(",")) {
            for (String sortOrder : sortBy) {
                String[] _sort = sortOrder.split(",");
                orders.add(new Sort.Order(getSortDirection(_sort[1]), _sort[0]));
            }
        } else {
            orders.add(new Sort.Order(getSortDirection("desc"), "lastModifiedTimestamp"));
        }
        return orders;
    }

    private Sort.Direction getSortDirection(String direction) {
        Sort.Direction sortDir = Sort.Direction.ASC;
        if (direction.trim().equals("desc")) {
            sortDir = Sort.Direction.DESC;
        }
        return sortDir;
    }

    private Long getGrantingAuthorityIdByName(String gaName){
        GrantingAuthority gaObj = gaRepository.findByGrantingAuthorityName(gaName);
        if(gaObj != null){
            return gaObj.getGaId();
        }
        return null;
    }

    private Specification<SubsidyMeasure> subsidyMeasureByGrantingAuthority(Long gaId) {
        return Specification.where(subsidyMeasureByGa(gaId));
    }

    public  Specification<SubsidyMeasure> subsidyMeasureByGa(Long gaId) {
        return (root, query, builder) -> builder.equal(root.get("grantingAuthority").get("gaId"), gaId);
    }

    public Specification<Award>  getSpecificationAwardDetails(AwardSearchInput awardSearchInput) {
        Specification<Award> awardSpecifications = Specification

                // getSubsidyObjective from input parameter
                .where(awardSearchInput.getSubsidyObjective() == null || awardSearchInput.getSubsidyObjective().isEmpty()
                        ? null : AwardSpecificationUtils.subsidyObjectiveIn(awardSearchInput.getSubsidyObjective())
                        //Like search for other subsidy objective
                        .or(awardSearchInput.getOtherSubsidyObjective() == null || awardSearchInput.getOtherSubsidyObjective().isEmpty()
                                ? null : AwardSpecificationUtils.otherSubsidyObjective(awardSearchInput.getOtherSubsidyObjective())))
                .and(awardSearchInput.getScNumber() == null || awardSearchInput.getScNumber().isEmpty()
                ? null : AwardSpecificationUtils.scNumber(awardSearchInput.getScNumber()))
                // getSpendingRegion from input parameter
                .and(awardSearchInput.getSpendingRegion() == null || awardSearchInput.getSpendingRegion().isEmpty()
                        ? null : AwardSpecificationUtils.spendingRegionIn(awardSearchInput.getSpendingRegion()))

                // getSpendingSector from input parameter
                .and(awardSearchInput.getSpendingSector() == null || awardSearchInput.getSpendingSector().isEmpty()
                        ? null : AwardSpecificationUtils.spendingSectorIn(awardSearchInput.getSpendingSector()))

                // getSubsidyInstrument from input parameter
                .and(awardSearchInput.getSubsidyInstrument() == null || awardSearchInput.getSubsidyInstrument().isEmpty()
                        ? null : AwardSpecificationUtils.subsidyInstrumentIn(awardSearchInput.getSubsidyInstrument()))
                // Like search for other instrument

                // getSubsidyMeasureTitle from input parameter
                .and(SearchUtils.checkNullOrEmptyString(awardSearchInput.getSubsidyMeasureTitle())
                        ? null : AwardSpecificationUtils.subsidyMeasureTitle(awardSearchInput.getSubsidyMeasureTitle().trim()));

        return awardSpecifications;
    }
}
