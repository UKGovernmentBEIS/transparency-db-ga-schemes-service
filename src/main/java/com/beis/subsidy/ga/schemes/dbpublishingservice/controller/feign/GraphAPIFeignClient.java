package com.beis.subsidy.ga.schemes.dbpublishingservice.controller.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.beis.subsidy.ga.schemes.dbpublishingservice.request.AddGroupRequest;

import feign.Headers;
import feign.RequestLine;
import feign.Response;

@FeignClient(name = "GraphAPIFeignClient", url = "${graphApiUrl}")
public interface GraphAPIFeignClient {

    @GetMapping(value = "/v1.0/groups")
    @RequestLine("GET v1.0/users")
    @Headers({"Authorization: {authorization}","Content-Type: application/json"})
    Response getGroups(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation);

    @PostMapping(value = "/v1.0/groups")
    @RequestLine("POST v1.0/groups")
     Response addGroup(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                         @RequestBody AddGroupRequest request);

    @DeleteMapping(value = "v1.0/groups/{id}")
    @RequestLine("DELETE v1.0/groups")
    Response deleteGroup(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                        @PathVariable("id")  String groupId);
    
    @DeleteMapping(value = "v1.0/users/{userId}")
    @RequestLine("DELETE v1.0/users")
    Response deleteUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                        @PathVariable("userId")  String userId);
   
    @GetMapping(value = "/v1.0/groups/{groupId}/members")
    @RequestLine("GET /v1.0/groups/{groupId}/members")
    @Headers({"Authorization: {authorization}","Content-Type: application/json"})
    Response getUsersByGroupId(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                               @PathVariable("groupId")  String groupId);
}
