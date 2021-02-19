package com.beis.subsidy.ga.schemes.dbpublishingservice.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GAResponse {

    @JsonProperty
    private Long gaId;

    @JsonProperty
    private String message;

   
  }
