package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.model.ParkingSpot;

import java.sql.SQLException;

public interface IParkingService {
    void processIncomingVehicle() throws IllegalArgumentException;
    ParkingSpot getNextParkingNumberIfAvailable() throws SQLException;
    void processExitingVehicle();
}
