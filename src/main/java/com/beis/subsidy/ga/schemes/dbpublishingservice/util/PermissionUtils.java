package com.beis.subsidy.ga.schemes.dbpublishingservice.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.InvalidRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Objects;

@Slf4j
public class PermissionUtils {

    public static DecodedJWT decodeJwt (String jwtString){
        DecodedJWT jwt;
        try {
            jwt = JWT.decode(jwtString);
        } catch (JWTDecodeException exception){
            throw new JWTDecodeException("Invalid JWT Token given: " + jwtString);
        }
        return jwt;
    }

    public static List<String> getRoleFromJwt(DecodedJWT jwt){
        return jwt.getClaim("roles").asList(String.class);
    }

    public static String getJwtStringFromHeaders(HttpHeaders userPrinciple){
        String jwtString = null;
        if (!userPrinciple.getOrEmpty("x-ms-token-aad-id-token").isEmpty()){
            jwtString = Objects.requireNonNull(userPrinciple.get("x-ms-token-aad-id-token")).get(0);
        }
        if (jwtString != null){
            return jwtString;
        }else{
            log.error("No x-ms-token-aad-id-token present");
            throw new InvalidRequestException("No x-ms-token-aad-id-token present");
        }
    }

    public static Boolean userPrincipleContainsId(HttpHeaders userPrinciple, String gaId) {
        String jwtString = getJwtStringFromHeaders(userPrinciple);
        DecodedJWT jwt = decodeJwt(jwtString);

        return getRoleFromJwt(jwt).contains(gaId);
    }

    public static Boolean userHasRole(UserPrinciple userPrincipleObj, String role) {
        return role.equals(userPrincipleObj.getRole().trim());
    }
}
