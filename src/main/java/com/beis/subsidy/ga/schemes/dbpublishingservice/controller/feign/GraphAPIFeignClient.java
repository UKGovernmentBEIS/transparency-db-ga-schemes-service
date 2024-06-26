package com.beis.subsidy.ga.schemes.dbpublishingservice.controller.feign;

import com.beis.subsidy.ga.schemes.dbpublishingservice.request.UpdateGroupRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(value = "/v1.0/groups/$count?$filter=displayName eq '{groupName}'")
    @RequestLine("GET /v1.0/groups/$count?$filter=displayName eq '{groupName}'")
    @Headers({"Authorization: {authorization}","Content-Type: application/json"})
    Response getGroupCountByName(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                               @PathVariable("groupName")  String groupName,
                                 @RequestHeader("consistencyLevel") String consistencyLevel);


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

    @GetMapping(value = "/v1.0/groups/{groupId}")
    @RequestLine("GET /v1.0/groups/{groupId}")
    @Headers({"Authorization: {authorization}","Content-Type: application/json"})
    Response getGroupByID(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                               @PathVariable("groupId")  String groupId);

    @PatchMapping(value = "v1.0/groups/{id}")
    @RequestLine("PATCH v1.0/groups")
    Response updateGroup(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                         @PathVariable("id") String groupId, @RequestBody UpdateGroupRequest request);

 }
