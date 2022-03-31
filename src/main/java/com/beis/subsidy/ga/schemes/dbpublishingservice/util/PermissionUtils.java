package com.beis.subsidy.ga.schemes.dbpublishingservice.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.InvalidRequestException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.SubsidyMeasure;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.SubsidyMeasureRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Objects;

@Slf4j
public class PermissionUtils {

    @Autowired
    private SubsidyMeasureRepository subsidyMeasureRepository;

    public Boolean ownsScheme(HttpHeaders userPrinciple, String scNumber) {
        String jwtString = null;
        if (!userPrinciple.getOrEmpty("x-ms-token-aad-id-token").isEmpty()){
            jwtString = Objects.requireNonNull(userPrinciple.get("x-ms-token-aad-id-token")).get(0);
        }

        if (jwtString != null){
            DecodedJWT jwt;
            try {
                jwt = JWT.decode(jwtString);
            } catch (JWTDecodeException exception){
                throw new JWTDecodeException("Invalid JWT Token given: " + jwtString);
            }
            List<String> rolesFromJwt = jwt.getClaim("roles").asList(String.class);

            SubsidyMeasure scheme = subsidyMeasureRepository.findById(scNumber).get();
            String schemeGaID = scheme.getGrantingAuthority().getAzureGroupId();

            return rolesFromJwt.contains(schemeGaID);
        }else{
            log.error("No x-ms-token-aad-id-token present");
            throw new InvalidRequestException("No x-ms-token-aad-id-token present");
        }
    }
}
