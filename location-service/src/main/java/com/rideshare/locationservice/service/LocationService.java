package com.rideshare.locationservice.service;

import com.rideshare.locationservice.dto.NearByDriverResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {

    public List<NearByDriverResponse> findNearbyDrivers(double latitude, double longitude, double radius) {
        return List.of();
    }

    public void removeDriver(String driverID) {
    }
}
