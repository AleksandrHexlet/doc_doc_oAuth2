package com.docdoc.oauth2.model.dto;

import com.docdoc.oauth2.configuration.RoleType;
import lombok.Getter;

@Getter
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
