package com.beis.subsidy.ga.schemes.dbpublishingservice.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class UserDetailsResponse {

    @JsonProperty
    private List<UserResponse> value = new ArrayList<>();

    @JsonGetter("value")
    public List<UserResponse> getUserProfiles() {
        return value;
    }

    @JsonSetter("value")
    public void setUserProfiles(List<UserResponse> value) {
        this.value = value;
    }

  }
