package com.beis.subsidy.ga.schemes.dbpublishingservice.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AccessManagementConstant {
	public static String SM_ACTIVE="Active";
	public static String SM_INACTIVE="Inactive";
    public static String SM_DELETED="Deleted";
    public static String BEIS_ADMIN_ROLE="BEIS Administrator";
    public static String ROLES[]= {"BEIS Administrator","Granting Authority Administrator","Granting Authority Approver","Granting Authority Encoder"};
    public static String ADMIN_ROLES[]= {"BEIS Administrator","Granting Authority Administrator"};
    public static String GA_ADMIN_ROLE="Granting Authority Administrator";
    public static String GA_APPROVER_ROLE="Granting Authority Approver";
    public static int GA_ALREADY_EXISTS = 491;
    public static List<String> SPENDING_SECTORS = new ArrayList<String>(Arrays.asList("accommodation and food service activities",
            "activities of extraterritorial organisations and bodies",
            "activities of households as employers; undifferentiated goods- and services-producing activities of households for own use",
            "administrative and support service activities", "agriculture, forestry and fishing",
            "arts, entertainment and recreation", "construction", "education",
            "electricity, gas, steam and air conditioning supply", "financial and insurance activities",
            "human health and social work activities", "information and communication", "manufacturing",
            "mining and quarrying", "other service activities", "professional, scientific and technical activities",
            "public administration and defence; compulsory social security", "real estate activities",
            "transportation and storage", "water supply; sewerage, waste management and remediation activities",
            "wholesale and retail trade; repair of motor vehicles and motorcycles"));

    public static List<String> PURPOSES = new ArrayList<String>(Arrays.asList("culture or heritage", "employment", "energy efficiency",
            "environmental protection", "infrastructure", "regional development", "rescue subsidy", "research and development",
            "sme (small/medium-sized enterprise) support", "training","other"));
}