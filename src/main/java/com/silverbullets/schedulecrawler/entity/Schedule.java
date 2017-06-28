package com.silverbullets.schedulecrawler.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "schedule")
public class Schedule {

    @Id
    @Column(name = "schedule_id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int scheduleId;

    @Column(name = "flight_no")
    private String flightNo;

    private String airline;
    private String departure;

    @Column(name = "departure_iata")
    private String departureIata;

    private String destination;

    @Column(name = "destination_iata")
    private String destinationIata;

    @Temporal(TemporalType.TIME)
    private Date etd;

    @Temporal(TemporalType.TIME)
    private Date eta;

    @Column(name = "aircraft_type")
    private String aircraftType;

    @Column(name = "aircraft_reg")
    private String aircraftReg;

    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "last_flown_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastFlownDate;

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getFlightNo() {
        return flightNo;
    }

    public void setFlightNo(String flightNo) {
        this.flightNo = flightNo;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getDepartureIata() {
        return departureIata;
    }

    public void setDepartureIata(String departureIata) {
        this.departureIata = departureIata;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDestinationIata() {
        return destinationIata;
    }

    public void setDestinationIata(String destinationIata) {
        this.destinationIata = destinationIata;
    }

    public Date getEtd() {
        return etd;
    }

    public void setEtd(Date etd) {
        this.etd = etd;
    }

    public Date getEta() {
        return eta;
    }

    public void setEta(Date eta) {
        this.eta = eta;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    public String getAircraftReg() {
        return aircraftReg;
    }

    public void setAircraftReg(String aircraftReg) {
        this.aircraftReg = aircraftReg;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastFlownDate() {
        return lastFlownDate;
    }

    public void setLastFlownDate(Date lastFlownDate) {
        this.lastFlownDate = lastFlownDate;
    }
}
