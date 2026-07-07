package com.skylink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportResponse {

    private Long id;

    private String iataCode;

    private String icaoCode;

    private String airportName;

    private String city;

    private String countryCode;

    private String airportType;

    private Double latitude;

    private Double longitude;

    private Double elevation;
}