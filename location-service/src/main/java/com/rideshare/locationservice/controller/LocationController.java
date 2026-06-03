package com.rideshare.locationservice.controller;



import com.rideshare.locationservice.dto.NearByDriverResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Driver;
import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@Slf4j
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/drivers/update")
    public ResponseEntity<String> updateDriverLocation(
            @RequestBody Driver driver) {
        return ResponseEntity.ok("driver location updated");
    }


    @GetMapping("/drivers/nearby")
    public ResponseEntity<List<NearByDriverResponse>> getNearByDriverResponse(
            @RequestParam double latitude, @RequestParam double longitude, @RequestParam (defaultValue = "5.0") double radius) {

        return  ResponseEntity.ok(locationService.findNeaarbyDrvers(latitude, longitude, radius))
    }

    @DeleteMapping("/drivers/{driverID}")
    public ResponseEntity<String > removeDriver(@PathVariable String driverID) {
        locationService.removeDriver(driverID);
        return ResponseEntity.ok("driver removed");
    }
}
