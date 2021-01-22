package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.time.Duration;
import java.util.Date;

/**
 * Manages the price of a Ticket
 */
public class FareCalculatorService {

/**
     * calculate the price according to the time to stay and the type of vehicle.
     * @param ticket instance of a Ticket.
     */
    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        Date inHour = ticket.getInTime();
        Date outHour = ticket.getOutTime();

        double duration = Duration.between(inHour.toInstant(), outHour.toInstant()).getSeconds(); // get seconds between inHour & outHour
        duration = duration / 3600.0f; // divide by 3600.0f for 1 hour

        if (duration <= 0.5f) {
            ticket.setPrice(0);
            return;
        }


        duration = Math.ceil(duration);

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }

        applyDiscountOnRecurringVisits(ticket);
    }

/**
     * applyDiscountOnRecurringVisits applies 15% to recurring visitors
     * @param ticket instance of a Ticket.
     */
    private void applyDiscountOnRecurringVisits(Ticket ticket) {
        if (ticket.isRecurrent()) {
            double price = ticket.getPrice() * 0.95;
            price = price*10;
            price = Math.round(price);
            price = price /10;
            ticket.setPrice(price);
        }
    }
}