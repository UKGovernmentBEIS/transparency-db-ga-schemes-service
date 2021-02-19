package com.beis.subsidy.ga.schemes.dbpublishingservice.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordProfile {

    private boolean forceChangePasswordNextSignIn;
    private String  password;

    @JsonCreator
    public PasswordProfile(
            @JsonProperty("forceChangePasswordNextSignIn") boolean forceChangePasswordNextSignIn,
            @JsonProperty("password") String password) {

        this.forceChangePasswordNextSignIn = forceChangePasswordNextSignIn;
        this.password = password;
    }
}
