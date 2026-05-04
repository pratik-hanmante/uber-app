package com.rideshare.locationservice.dto;

/**
 * DTO (Data Transfer Object) representing a nearby driver response.
 * This class is used to send driver location and distance details
 
 */
public class NearByDriverResponse {


    private String driverId;

    // Latitude of the driver's current location (as a string)
    private String latitude;

    // Longitude of the driver's current location (as a string)
    private String longitude;

    // Distance of the driver from the user in kilometers (as a string)
    private String distanceInKm;

}
