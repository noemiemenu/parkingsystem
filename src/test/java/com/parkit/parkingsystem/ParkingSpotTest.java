package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParkingSpotTest {


    @Test
    public void ParkingSpotEqualsTrueTest() {
        ParkingSpot parkingSpot = new ParkingSpot(0, ParkingType.BIKE, true);
        assertTrue(parkingSpot.equals(parkingSpot));

    }

    @Test
    public void ParkingSpotEqualsFalseTest() {
        ParkingSpot parkingSpotBike = new ParkingSpot(0, ParkingType.BIKE, true);
        ParkingSpot parkingSpotCar = new ParkingSpot(1, ParkingType.CAR, true);
        assertFalse(parkingSpotBike.equals(parkingSpotCar));
    }


}
