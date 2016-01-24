package org.openflights.angular.model;

import java.time.ZonedDateTime;

public class Flight {
	private ZonedDateTime departure;
	private ZonedDateTime arrival;
	private String from;
	private Airport aptFrom;
	private String to;
	private Airport aptTo;
	public Airport getAptFrom() {
		return aptFrom;
	}

	public void setAptFrom(Airport aptFrom) {
		this.aptFrom = aptFrom;
	}

	public Airport getAptTo() {
		return aptTo;
	}

	public void setAptTo(Airport aptTo) {
		this.aptTo = aptTo;
	}

	private String flightNo;
	private String seat;
	private String seatType;
	private String bookingClass;
	private String reason;
	private Airline carrier;
	private String acType;
	private String acTailsign;

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

	public String getFlightNo() {
		return flightNo;
	}

	public void setFlightNo(String flightNo) {
		this.flightNo = flightNo;
	}

	public String getSeat() {
		return seat;
	}

	public void setSeat(String seat) {
		this.seat = seat;
	}

	public String getSeatType() {
		return seatType;
	}

	public void setSeatType(String seatType) {
		this.seatType = seatType;
	}

	public String getBookingClass() {
		return bookingClass;
	}

	public void setBookingClass(String bookingClass) {
		this.bookingClass = bookingClass;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Airline getCarrier() {
		return carrier;
	}

	public void setCarrier(Airline carrier) {
		this.carrier = carrier;
	}

	public String getAcType() {
		return acType;
	}

	public void setAcType(String acType) {
		this.acType = acType;
	}

	public String getAcTailsign() {
		return acTailsign;
	}

	public void setAcTailsign(String acTailsign) {
		this.acTailsign = acTailsign;
	}

	@Override
	public String toString() {
		return "Flight [departure=" + departure + ", arrival=" + arrival + ", from=" + from + ", aptFrom=" + aptFrom
				+ ", to=" + to + ", aptTo=" + aptTo + ", flightNo=" + flightNo + ", seat=" + seat + ", seatType="
				+ seatType + ", bookingClass=" + bookingClass + ", reason=" + reason + ", carrier=" + carrier
				+ ", acType=" + acType + ", acTailsign=" + acTailsign + "]";
	}

	
	
	
}
