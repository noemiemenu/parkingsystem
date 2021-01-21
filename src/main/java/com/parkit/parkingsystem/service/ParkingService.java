package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.Date;

/**
 * Manages the Parking
 */
public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private InputReaderUtil inputReaderUtil;
    private ParkingSpotDAO parkingSpotDAO;
    private TicketDAO ticketDAO;

    /**
     * ParkingService Constructor
     * @param inputReaderUtil instance of InputReaderUtil
     * @param parkingSpotDAO ParkingSpot database interface
     * @param ticketDAO Ticket database interface
     */
    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }

    /**
     * processIncomingVehicle records information when a vehicle arrives
     */
    public void processIncomingVehicle() {
        try {
            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
            if (parkingSpot != null && parkingSpot.getId() > 0) {
                String vehicleRegNumber = getVehichleRegNumber();
                parkingSpot.setAvailable(false);

                Date inTime = new Date();
                Ticket ticket = new Ticket();
                //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
                //ticket.setId(ticketID);
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(0);
                ticket.setInTime(inTime);
                ticket.setOutTime(null);

                if (!ticketDAO.saveTicket(ticket)) {
                    System.out.println("Your vehicle registration number is incorrect");
                    return;
                } else {
                    //allocate this parking space and mark it's availability as false
                    parkingSpotDAO.updateParking(parkingSpot);
                }
                if (ticket.isRecurrent()) {
                    System.out.println("Welcome back! As a recurring user of our parking lot, you'll benefit from a 5% discount.");
                }
                System.out.println("Generated Ticket and saved in DB");
                System.out.println("Please park your vehicle in spot number:" + parkingSpot.getId());
                System.out.println("Recorded in-time for vehicle number:" + vehicleRegNumber + " is:" + inTime);
            }
        } catch (SQLException sqlException) {
            logger.error("Unable to process incoming vehicle", sqlException);
        }
    }

    /**
     * getVehichleRegNumber asks for the vehicle registration number
     * @return the vehicle registration number
     */
    private String getVehichleRegNumber() {
        System.out.println("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

    /**
     * call getVehicleType & instantiate a ParkingSpot
     * @return ParkingSpot
     */
    public ParkingSpot getNextParkingNumberIfAvailable() throws SQLException {
        int parkingNumber = 0;
        ParkingSpot parkingSpot = null;
        try {
            ParkingType parkingType = getVehicleType();
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if (parkingNumber > 0) {
                parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
            } else {
                throw new SQLException("Error fetching parking number from DB. Parking slots might be full");
            }
        } catch (IllegalArgumentException ie) {
            logger.error("Error parsing user input for type of vehicle", ie);

        }
        return parkingSpot;
    }

    /**
     * transform user input to ParkingType
     * @return vehicle type (ParkingType)
     */
    private ParkingType getVehicleType() {
        System.out.println("Please select vehicle type from menu");
        System.out.println("1 CAR");
        System.out.println("2 BIKE");
        int input = inputReaderUtil.readSelection();
        switch (input) {
            case 1: {
                return ParkingType.CAR;
            }
            case 2: {
                return ParkingType.BIKE;
            }
            default: {
                System.out.println("Incorrect input provided");
                throw new IllegalArgumentException("Entered input is invalid");
            }
        }
    }

    /**
     * processExitingVehicle register and generate an exit ticket with the price of parking
     */
    public void processExitingVehicle() {
        String vehicleRegNumber = getVehichleRegNumber();
        Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
        if (ticket == null) {
            System.out.println("Your vehicle registration number is incorrect");
            return;
        }

        Date outTime = new Date();
        ticket.setOutTime(outTime);
        fareCalculatorService.calculateFare(ticket);
        if (ticketDAO.updateTicket(ticket)) {
            ParkingSpot parkingSpot = ticket.getParkingSpot();
            parkingSpot.setAvailable(true);
            if (!parkingSpotDAO.updateParking(parkingSpot)) {
                return;
            }
            System.out.println("Please pay the parking fare:" + ticket.getPrice());
            System.out.println("Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
        } else {
            System.out.println("Unable to update ticket information. Error occurred");
        }
    }
}
