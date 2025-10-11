package com.team.codejam.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UserProfileResponseDto {
    private Long id;
    private String email;
    private String displayName;
    private String currency;
    private String distanceUnit;
    private String volumeUnit;
    private String timeZone;
    private List<VehicleDto> vehicles;
}
