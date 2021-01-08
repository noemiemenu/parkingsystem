package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.commons.lang.time.DateUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    private static final String VehicleRegistrationNumber = "ABCDEF";

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VehicleRegistrationNumber);
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        // check that a ticket is actually saved in DB
        Ticket ticket = ticketDAO.getTicket(VehicleRegistrationNumber);
        assertNotNull(ticket);
        assertTrue(ticket.getId() > 0);

        // Check Parking table is updated with availability=false
        ParkingSpot parkingSpot = parkingSpotDAO.getParkingSpot(ticket.getParkingSpot().getId());
        assertNotNull(parkingSpot);
        assertFalse(parkingSpot.isAvailable());
    }


    private void createTicketInDatabase(ParkingService parkingService, Date inTime) {
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        parkingSpot.setAvailable(false);
        parkingSpotDAO.updateParking(parkingSpot);//allot this parking space and mark it's availability as false

        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(VehicleRegistrationNumber);
        ticket.setPrice(0);
        ticket.setInTime(inTime);
        ticket.setOutTime(null);
        ticketDAO.saveTicket(ticket);
    }

    @Test
    public void testParkingLotExit() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        Date inTime = new Date();
        createTicketInDatabase(parkingService, DateUtils.setHours(inTime, inTime.getHours() - 1));

        parkingService.processExitingVehicle();

        // check that the fare generated and out time are populated correctly in the database
        Ticket ticket = ticketDAO.getTicket(VehicleRegistrationNumber);
        assertNotNull(ticket);
        assertTrue(ticket.getId() > 0);
        assertTrue(ticket.getPrice() > 0);
        assertNotNull(ticket.getOutTime());

        // Check Parking table is updated with availability=true
        ParkingSpot parkingSpot = parkingSpotDAO.getParkingSpot(ticket.getParkingSpot().getId());
        assertNotNull(parkingSpot);
        assertTrue(parkingSpot.isAvailable());
    }

}
