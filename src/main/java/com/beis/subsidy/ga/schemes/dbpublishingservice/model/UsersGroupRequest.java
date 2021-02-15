package com.beis.subsidy.ga.schemes.dbpublishingservice.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsersGroupRequest {

	private List<String> userId;
	
	String name;

}
