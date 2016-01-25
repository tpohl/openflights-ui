package org.openflights.angular.backend.lufthansa.model;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class FlightStatus {
	private String from;
	private String to;
	private ZonedDateTime departure;
	private ZonedDateTime arrival;
	private LocalDateTime departureLocal;

	private LocalDateTime arrivalLocal;
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

	public ZonedDateTime getDeparture() {
		return departure;
	}

	public void setDeparture(ZonedDateTime departure) {
		this.departure = departure;
	}

	public ZonedDateTime getArrival() {
		return arrival;
	}

	public void setArrival(ZonedDateTime arrival) {
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

	public LocalDateTime getDepartureLocal() {
		return departureLocal;
	}

	public void setDepartureLocal(LocalDateTime departureLocal) {
		this.departureLocal = departureLocal;
	}

	public LocalDateTime getArrivalLocal() {
		return arrivalLocal;
	}

	public void setArrivalLocal(LocalDateTime arrivalLocal) {
		this.arrivalLocal = arrivalLocal;
	}

	@Override
	public String toString() {
		return "FlightStatus [from=" + from + ", to=" + to + ", departure=" + departure + ", arrival=" + arrival
				+ ", airlineCode=" + airlineCode + ", flightNo=" + flightNo + ", aircraftCode=" + aircraftCode + "]";
	}

}
