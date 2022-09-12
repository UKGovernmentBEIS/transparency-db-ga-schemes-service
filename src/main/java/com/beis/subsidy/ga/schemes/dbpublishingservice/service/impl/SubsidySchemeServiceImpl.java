package com.beis.subsidy.ga.schemes.dbpublishingservice.service.impl;


import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.InvalidRequestException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.SearchResultNotFoundException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.UnauthorisedAccessException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.LegalBasis;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.SubsidyMeasure;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.GrantingAuthorityRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.SubsidyMeasureRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeDetailsRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeSearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SearchSubsidyResultsResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SubsidyMeasureResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.service.SubsidySchemeService;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.AccessManagementConstant;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.SchemeSpecificationUtils;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.SearchUtils;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.UserPrinciple;
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
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class SubsidySchemeServiceImpl implements SubsidySchemeService {
    @Autowired
    private SubsidyMeasureRepository subsidyMeasureRepository;

    @Autowired
    private GrantingAuthorityRepository gaRepository;

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
                    throw new UnauthorisedAccessException("Invalid granting authority name");
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
        if(!StringUtils.isEmpty(scheme.getGaSubsidyWebLink())){
            schemeToSave.setGaSubsidyWebLink(scheme.getGaSubsidyWebLink());
        }
        if(!StringUtils.isEmpty(scheme.getGaSubsidyWebLinkDescription())){
            schemeToSave.setGaSubsidyWebLinkDescription(scheme.getGaSubsidyWebLinkDescription());
        }
        if(scheme.isAdhoc() || !scheme.isAdhoc()){
            schemeToSave.setAdhoc(scheme.isAdhoc());
        }
        if(!StringUtils.isEmpty(scheme.getStatus())){
            schemeToSave.setStatus(scheme.getStatus());
        }

        if(!StringUtils.isEmpty(scheme.getGaName())){
            GrantingAuthority grantingAuthority = gaRepository.findByGrantingAuthorityName(scheme.getGaName().trim());

            log.error("{} :: Granting Authority and GAName ::{}", grantingAuthority,scheme.getGaName());

            if (Objects.isNull(grantingAuthority) ||
                    "Inactive".equals(grantingAuthority.getStatus())) {

               log.error("{} :: Granting Authority is Inactive for the scheme");
               throw new InvalidRequestException("Granting Authority is Inactive");
            }
            schemeToSave.setGaId(grantingAuthority.getGaId());
        }
        schemeToSave.setPublishedMeasureDate(LocalDate.now());

        if(!StringUtils.isEmpty(scheme.getLegalBasisText())){
            legalBasis.setLegalBasisText(scheme.getLegalBasisText());
        }
        schemeToSave.setLastModifiedTimestamp(LocalDate.now());

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
        if(!StringUtils.isEmpty(scheme.getGaSubsidyWebLink())){
            schemeById.setGaSubsidyWebLink(scheme.getGaSubsidyWebLink());
        }
        if(!StringUtils.isEmpty(scheme.getGaSubsidyWebLinkDescription())){
            schemeById.setGaSubsidyWebLinkDescription(scheme.getGaSubsidyWebLinkDescription());
        }
        if(scheme.isAdhoc() || !scheme.isAdhoc()){
            schemeById.setAdhoc(scheme.isAdhoc());
        }
        if(!StringUtils.isEmpty(scheme.getStatus())){
            schemeById.setStatus(scheme.getStatus());
            if(scheme.getStatus().equals("Deleted")){
                schemeById.setDeletedBy(userPrinciple.getUserName());
                schemeById.setDeletedTimestamp(LocalDateTime.now());
            }
        }
        if(!StringUtils.isEmpty(scheme.getLegalBasisText())){
            legalBasis.setLegalBasisText(scheme.getLegalBasisText());
        }
        if(scheme.isHasNoEndDate()){
            schemeById.setHasNoEndDate(true);
        }
        schemeById.setLastModifiedTimestamp(LocalDate.now());

        legalBasis.setLastModifiedTimestamp(new Date());
        legalBasis.setCreatedTimestamp(new Date());
        schemeById.setLegalBases(legalBasis);
        legalBasis.setSubsidyMeasure(schemeById);

        SubsidyMeasure updatedScheme = subsidyMeasureRepository.save(schemeById);
        log.info("Updated successfully : ");
        return updatedScheme.getScNumber();
    }

    @Override
    public SubsidyMeasureResponse findSubsidySchemeById(String scNumber) {
        SubsidyMeasure subsidyMeasure = subsidyMeasureRepository.findById(scNumber).get();
        return new SubsidyMeasureResponse(subsidyMeasure);
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
        if (direction.equals("desc")) {
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
}
