package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParkingSpotTest {


    @Test
    public void ParkingSpotEqualsTrueTest() {
        ParkingSpot parkingSpot = new ParkingSpot(0, ParkingType.BIKE, true);
        assertEquals(parkingSpot, parkingSpot);

    }

    @Test
    public void ParkingSpotEqualsFalseTest() {
        ParkingSpot parkingSpotBike = new ParkingSpot(0, ParkingType.BIKE, true);
        ParkingSpot parkingSpotCar = new ParkingSpot(1, ParkingType.CAR, true);
        assertNotEquals(parkingSpotCar, parkingSpotBike);
    }


}
