package com.beis.subsidy.ga.schemes.dbpublishingservice.service;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.AdminProgram;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.AdminProgramDetailsRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.UserPrinciple;

public interface AdminProgramService {

    AdminProgram addAdminProgram(AdminProgramDetailsRequest adminProgramRequest, UserPrinciple userPrinciple);
}
