package org.openflights.angular.backend.lufthansa.model;

import java.util.Date;

public class FlightStatus {
	private String from;
	private String to;
	private Date departure;
	private Date arrival;
	private String airlineCode;
	private String flightNo;
	private String aircraftCode;
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public Date getDeparture() {
		return departure;
	}
	public void setDeparture(Date departure) {
		this.departure = departure;
	}
	public Date getArrival() {
		return arrival;
	}
	public void setArrival(Date arrival) {
		this.arrival = arrival;
	}
	public String getAirlineCode() {
		return airlineCode;
	}
	public void setAirlineCode(String airlineCode) {
		this.airlineCode = airlineCode;
	}
	public String getFlightNo() {
		return flightNo;
	}
	public void setFlightNo(String flightNo) {
		this.flightNo = flightNo;
	}
	public String getAircraftCode() {
		return aircraftCode;
	}
	public void setAircraftCode(String aircraftCode) {
		this.aircraftCode = aircraftCode;
	}
	@Override
	public String toString() {
		return "FlightStatus [from=" + from + ", to=" + to + ", departure=" + departure + ", arrival=" + arrival
				+ ", airlineCode=" + airlineCode + ", flightNo=" + flightNo + ", aircraftCode=" + aircraftCode + "]";
	}
	
	
}
