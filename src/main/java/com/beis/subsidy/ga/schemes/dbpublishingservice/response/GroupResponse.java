package com.beis.subsidy.ga.schemes.dbpublishingservice.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupResponse {

    @JsonProperty
    private String id;

    @JsonProperty
    private String displayName;

    @JsonProperty
    private String description;

     @JsonProperty
    private String mailNickname;
  
  }
