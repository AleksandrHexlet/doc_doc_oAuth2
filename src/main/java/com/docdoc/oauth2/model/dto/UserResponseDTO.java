package com.docdoc.oauth2.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import com.docdoc.oauth2.configuration.RoleType;

@Getter
@Setter
@Builder
public class UserResponseDTO {
    private String login;
    private String name;
    private String lastName;
    private String metroStationName;
    private String areaName;
    private String city;
    private String userId;
    private RoleType role;
}
