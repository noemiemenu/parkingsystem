package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

/**
 * Data Access Object of a Ticket
 * TicketDAO is a Ticket interface between the program and the database
 */
public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    /**
     * save Ticket in database
     * @param ticket an instance of a ticket to be saved
     * @return boolean
     */
    public boolean saveTicket(Ticket ticket) {
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET);
            PreparedStatement recurrentStatement = con.prepareStatement(DBConstants.IS_RECURRENT);
            recurrentStatement.setString(1, ticket.getVehicleRegNumber());
            ResultSet rs = recurrentStatement.executeQuery();
            if (rs.next()) {
                ticket.setRecurrent(rs.getInt(1) >= 1);
            }

            ps.setInt(1, ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(5, (ticket.getOutTime() == null) ? null : (new Timestamp(ticket.getOutTime().getTime())));
            ps.setBoolean(6, ticket.isRecurrent());
            ps.execute();
            return true;
        }
        catch (ClassNotFoundException ex) {
            logger.error("Class not found: ", ex);
        }
        catch (SQLException ex) {
            logger.error("Error fetching next available slot", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }

        return false;
    }

    /**
     * transforms a Ticket in database in an instance of a Ticket
     * @param vehicleRegNumber string
     * @return ticket
     */
    public Ticket getTicket(String vehicleRegNumber) {
        Connection con = null;
        Ticket ticket = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET);
            ps.setString(1, vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)), false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
                ticket.setRecurrent(rs.getBoolean(7));
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        } catch (ClassNotFoundException ex) {
            logger.error("Class not found: ", ex);
        } catch (SQLException ex) {
            logger.error("Error fetching next available slot", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
            return ticket;
        }
    }

    /**
     * update a ticket with user information and save it in the database
     * @param ticket an instance of a ticket to be updated
     * @return boolean
     */
    public boolean updateTicket(Ticket ticket) {
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
            PreparedStatement recurrentStatement = con.prepareStatement(DBConstants.IS_RECURRENT);

            recurrentStatement.setString(1, ticket.getVehicleRegNumber());
            ResultSet rs = recurrentStatement.executeQuery();
            if (rs.next()) {
                ticket.setRecurrent(rs.getInt(1) > 1);
            }

            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            ps.setBoolean(3, ticket.isRecurrent());
            ps.setInt(4, ticket.getId());
            ps.execute();
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }

        return false;
    }
}
