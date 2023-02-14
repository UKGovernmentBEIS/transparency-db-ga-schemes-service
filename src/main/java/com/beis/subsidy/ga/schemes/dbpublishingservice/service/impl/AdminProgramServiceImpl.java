package com.beis.subsidy.ga.schemes.dbpublishingservice.service.impl;


import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.InvalidRequestException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.SearchResultNotFoundException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.UnauthorisedAccessException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.AdminProgram;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.Award;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.SubsidyMeasure;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.AdminProgramRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.AwardRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.GrantingAuthorityRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.SubsidyMeasureRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.AdminProgramDetailsRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeSearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.AdminProgramResultsResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.service.AdminProgramService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminProgramServiceImpl implements AdminProgramService {
    @Autowired
    private AdminProgramRepository adminProgramRepository;

    @Autowired
    private GrantingAuthorityRepository gaRepository;

    @Autowired
    private SubsidyMeasureRepository smRepository;

    @Autowired
    private AwardRepository awardRepository;

    @Override
    public AdminProgram addAdminProgram(AdminProgramDetailsRequest adminProgramRequest, UserPrinciple userPrinciple) {
        log.info("Inside addSubsidySchemeDetails method :");
        AdminProgram adminProgramToSave = new AdminProgram();

        adminProgramToSave.setCreatedBy(userPrinciple.getUserName());
        if(adminProgramRequest.getBudget() != null){
            adminProgramToSave.setBudget(adminProgramRequest.getBudget());
        }

        if(!StringUtils.isEmpty(adminProgramRequest.getStatus())){
            adminProgramToSave.setStatus(adminProgramRequest.getStatus());
        }

        if(!StringUtils.isEmpty(adminProgramRequest.getGrantingAuthorityName())){
            GrantingAuthority grantingAuthority = gaRepository.findByGrantingAuthorityName(adminProgramRequest.getGrantingAuthorityName().trim());

            log.error("{} :: Public Authority and PAName ::{}", grantingAuthority,adminProgramRequest.getGrantingAuthorityName());

            if (Objects.isNull(grantingAuthority) ||
                    "Inactive".equals(grantingAuthority.getStatus())) {

                log.error("{} :: Public Authority is Inactive for the scheme");
                throw new InvalidRequestException("Public Authority is Inactive");
            }
            adminProgramToSave.setGrantingAuthority(grantingAuthority);
        }

        if(!StringUtils.isEmpty(adminProgramRequest.getAdminProgramName())){
            adminProgramToSave.setAdminProgramName(adminProgramRequest.getAdminProgramName());
        }

        if(!StringUtils.isEmpty(adminProgramRequest.getScNumber())){
            SubsidyMeasure sm = smRepository.findById(adminProgramRequest.getScNumber()).orElse(null);
            if(sm != null) {
                adminProgramToSave.setSubsidyMeasure(sm);
            }
        }
        adminProgramToSave.setCreatedTimestamp(LocalDateTime.now());
        adminProgramToSave.setLastModifiedTimestamp(LocalDateTime.now());


        AdminProgram savedAdminProgram = adminProgramRepository.save(adminProgramToSave);
        log.info("Scheme added successfully with Id : " + savedAdminProgram.getApNumber());
        return savedAdminProgram;
    }

    @Override
    public AdminProgramResultsResponse findMatchingAdminProgramDetails(SchemeSearchInput searchInput, UserPrinciple userPrinciple) {
        log.info("Inside findMatchingAdminProgramDetails searchName : " + searchInput.getSearchName());

        Specification<AdminProgram> adminProgramSpecification = getSpecificationAdminProgramDetails(searchInput);
        Specification<AdminProgram> schemeSpecificationsWithout = getSpecificationAdminProgramDetailsWithoutStatus(searchInput);
        List<AdminProgram> totalAdminProgramList = new ArrayList<>();
        List<AdminProgram> results = null;
        Page<AdminProgram> pageResults = null;
        List<Sort.Order> orders = getOrderByCondition(searchInput.getSortBy());

        Pageable pagingSort = PageRequest.of(searchInput.getPageNumber() - 1,
                searchInput.getTotalRecordsPerPage(), Sort.by(orders));


        if (AccessManagementConstant.BEIS_ADMIN_ROLE.equals(userPrinciple.getRole().trim())) {

            pageResults = adminProgramRepository.findAll(adminProgramSpecification, pagingSort);

            results = pageResults.getContent();

            if(!StringUtils.isEmpty(searchInput.getSearchName())){
                totalAdminProgramList = adminProgramRepository.findAll(schemeSpecificationsWithout);
            } else {
                totalAdminProgramList = adminProgramRepository.findAll();
            }

        } else {
            if (!StringUtils.isEmpty(searchInput.getSearchName())
                    || !StringUtils.isEmpty(searchInput.getStatus())) {

                adminProgramSpecification = getSpecificationAdminProgramDetailsForGARoles(searchInput,userPrinciple.getGrantingAuthorityGroupName());
                pageResults = adminProgramRepository.findAll(adminProgramSpecification, pagingSort);

                results = pageResults.getContent();
                totalAdminProgramList = adminProgramRepository.findAll(adminProgramSpecification);

            } else {

                Long gaId = getGrantingAuthorityIdByName(userPrinciple.getGrantingAuthorityGroupName());
                if(gaId == null || gaId <= 0){
                    throw new UnauthorisedAccessException("Invalid public authority name");
                }
                pageResults = adminProgramRepository.
                        findAll(adminProgramsByGrantingAuthority(gaId),pagingSort);
                results = pageResults.getContent();
                totalAdminProgramList = adminProgramRepository.findAll(adminProgramsByGrantingAuthority(gaId));

            }
        }

        AdminProgramResultsResponse searchResults = null;

        if (!results.isEmpty()) {
            searchResults = new AdminProgramResultsResponse(results, pageResults.getTotalElements(),
                    pageResults.getNumber() + 1, pageResults.getTotalPages(), adminProgramCounts(totalAdminProgramList));
        } else {
            log.info("{}::Scheme results not found");
            throw new SearchResultNotFoundException("Scheme Results NotFound");
        }
        return searchResults;
    }

    @Override
    public AdminProgram updateAdminProgramDetails(AdminProgramDetailsRequest adminProgramDetailsRequest, String adminProgramNumber, UserPrinciple userPrincipleObj) {
        AdminProgram update = adminProgramRepository.findById(adminProgramNumber).orElse(null);

        if(update == null){
            return null;
        }

        if (!StringUtils.isEmpty(adminProgramDetailsRequest.getAdminProgramName())){
            update.setAdminProgramName(adminProgramDetailsRequest.getAdminProgramName());
        }

        if(adminProgramDetailsRequest.getBudget() != null) {
            update.setBudget(adminProgramDetailsRequest.getBudget());
        }

        if(!StringUtils.isEmpty(adminProgramDetailsRequest.getStatus())) {
            // if admin program is being deleted
            if(adminProgramDetailsRequest.getStatus().equalsIgnoreCase("deleted") && !update.getStatus().equalsIgnoreCase("deleted")){
                update.setDeletedBy(userPrincipleObj.getUserName());
                update.setDeletedTimestamp(LocalDateTime.now());

                // remove association with awards if any exist
                if(update.getAwardList().size() > 0){
                    List<Long> awardNumberList = update.getAwardList().stream().map(Award::getAwardNumber)
                            .collect(Collectors.toList());
                    log.info("Removing association AP {} from awards {}", update.getApNumber(), awardNumberList);
                    List<Award> awards = awardRepository.findByAdminProgram(update);
                    for (Award award : awards) {
                        award.setAdminProgram(null);
                        award.setLastModifiedTimestamp(LocalDate.now());
                    }
                    awardRepository.saveAll(awards);
                }
            }
            update.setStatus(adminProgramDetailsRequest.getStatus());
        }

        update.setLastModifiedTimestamp(LocalDateTime.now());
        return adminProgramRepository.save(update);
    }

    public Specification<AdminProgram>  getSpecificationAdminProgramDetails(SchemeSearchInput searchInput) {
        String searchName = searchInput.getSearchName();
        Specification<AdminProgram> specifications = Specification
                .where(
                        SearchUtils.checkNullOrEmptyString(searchName)
                                ? null : AdminProgramSpecificationUtils.adminProgramByName(searchName.trim())
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null : AdminProgramSpecificationUtils.adminProgramByScNumber(searchName.trim()))
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null :AdminProgramSpecificationUtils.grantingAuthorityName(searchName.trim()))
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null :AdminProgramSpecificationUtils.adminProgramByApNumber(searchName.trim()))
                )
                .and(SearchUtils.checkNullOrEmptyString(searchInput.getStatus())
                        ? null : AdminProgramSpecificationUtils.adminProgramByStatus(searchInput.getStatus().trim()));
        return specifications;
    }
    public Specification<AdminProgram>  getSpecificationAdminProgramDetailsWithoutStatus(SchemeSearchInput searchInput) {
        String searchName = searchInput.getSearchName();
        Specification<AdminProgram> specifications = Specification
                .where(
                        SearchUtils.checkNullOrEmptyString(searchName)
                                ? null : AdminProgramSpecificationUtils.adminProgramByName(searchName.trim())
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null : AdminProgramSpecificationUtils.adminProgramByScNumber(searchName.trim()))
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null :AdminProgramSpecificationUtils.grantingAuthorityName(searchName.trim()))
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null :AdminProgramSpecificationUtils.adminProgramByApNumber(searchName.trim()))
                );
        return specifications;
    }

    public Specification<AdminProgram>  getSpecificationAdminProgramDetailsForGARoles(SchemeSearchInput searchInput, String gaName) {
        String searchName = searchInput.getSearchName();
        Specification<AdminProgram> specifications = Specification
                .where(
                        SearchUtils.checkNullOrEmptyString(searchName)
                                ? null : AdminProgramSpecificationUtils.adminProgramByName(searchName.trim())
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null : AdminProgramSpecificationUtils.adminProgramByScNumber(searchName.trim()))
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null :AdminProgramSpecificationUtils.grantingAuthorityName(searchName.trim()))
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null :AdminProgramSpecificationUtils.adminProgramByApNumber(searchName.trim()))
                )
                .and(SearchUtils.checkNullOrEmptyString(gaName)
                        ? null :AdminProgramSpecificationUtils.grantingAuthorityName(gaName.trim()))
                .and(SearchUtils.checkNullOrEmptyString(searchInput.getStatus())
                        ? null :AdminProgramSpecificationUtils.adminProgramByStatus(searchInput.getStatus().trim()));
        return specifications;
    }

    private List<Sort.Order> getOrderByCondition(String[] sortBy) {
        List<Sort.Order> orders = new ArrayList<Sort.Order>();
        if (sortBy != null && sortBy.length > 0 && sortBy[0].contains(",")) {
            for (String sortOrder : sortBy) {
                String[] _sort = sortOrder.split(",");
                orders.add(new Sort.Order(getSortDirection(_sort[1]), _sort[0]));
            }
        } else {
            orders.add(new Sort.Order(getSortDirection("desc"), "apNumber"));
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

    private Map<String, Long> adminProgramCounts(List<AdminProgram> adminPrograms) {
        long all = adminPrograms.size();
        long active = 0;
        long deleted = 0;

        if(adminPrograms.size() > 0){
            for(AdminProgram adminProgram : adminPrograms){
                if(adminProgram.getStatus().equalsIgnoreCase(AccessManagementConstant.SM_ACTIVE)){
                    active++;
                }
                if(adminProgram.getStatus().equalsIgnoreCase(AccessManagementConstant.SM_DELETED)){
                    deleted++;
                }
            }
        }
        Map<String, Long> adminProgramUserActivityCount = new HashMap<>();
        adminProgramUserActivityCount.put("all",all);
        adminProgramUserActivityCount.put("active",active);
        adminProgramUserActivityCount.put("deleted",deleted);
        return adminProgramUserActivityCount;
    }

    private Specification<AdminProgram> adminProgramsByGrantingAuthority(Long gaId) {
        return (root, query, builder) -> builder.equal(root.get("grantingAuthority").get("gaId"), gaId);
    }

    private Long getGrantingAuthorityIdByName(String gaName) {
        GrantingAuthority gaObj = gaRepository.findByGrantingAuthorityName(gaName);
        if (gaObj != null) {
            return gaObj.getGaId();
        }
        return null;
    }
}
