package com.beis.subsidy.ga.schemes.dbpublishingservice.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorResult {

    public String row;
    public String columns;
    public String errorMessages;

    public ValidationErrorResult(String errorMessages) {
        this.errorMessages=errorMessages;

    }
}
