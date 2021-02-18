package com.beis.subsidy.ga.schemes.dbpublishingservice.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPrinciple {
  private String userName;
  private String password;
  private String role;
  private int grantingAuthorityGroupId;
  private String grantingAuthorityGroupName;
}
