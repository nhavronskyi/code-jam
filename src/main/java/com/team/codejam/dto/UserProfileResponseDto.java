package com.team.codejam.dto;

public class UserProfileResponseDto {
    private Long id;
    private String email;
    private String displayName;
    private String currency;
    private String distanceUnit;
    private String volumeUnit;
    private String timeZone;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getDistanceUnit() { return distanceUnit; }
    public void setDistanceUnit(String distanceUnit) { this.distanceUnit = distanceUnit; }
    public String getVolumeUnit() { return volumeUnit; }
    public void setVolumeUnit(String volumeUnit) { this.volumeUnit = volumeUnit; }
    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
}
