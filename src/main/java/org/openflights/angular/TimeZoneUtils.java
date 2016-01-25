package org.openflights.angular;

import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.openflights.angular.model.Airport;
import org.openflights.angular.model.Flight;

public class TimeZoneUtils {
	public static ZoneId getZoneIdFromOffset(double offset) {
		DecimalFormat tzFormat = new DecimalFormat("+#0;-#0");
		String tz = tzFormat.format(offset);
		return ZoneId.of(tz);
	}

	public static ZoneId getZoneId(Airport airport) {
		return getZoneIdFromOffset(airport.getTimezone());
	}

	public static void updateTimezones(Flight flight) {
		if (flight.getAptFrom() != null && flight.getDeparture() != null) {
			flight.setDeparture(flight.getDeparture().withZoneSameInstant(getZoneId(flight.getAptFrom())));
		}
		if (flight.getAptTo() != null && flight.getArrival() != null) {
			flight.setArrival(flight.getArrival().withZoneSameInstant(getZoneId(flight.getAptTo())));
		}
		if (flight.getDepartureLocal() != null && flight.getDeparture() == null && flight.getAptFrom() != null) {
			flight.setDeparture(ZonedDateTime.of(flight.getDepartureLocal(), getZoneId(flight.getAptFrom())));
		}
		if (flight.getArrivalLocal() != null && flight.getArrival() == null && flight.getAptTo() != null) {
			flight.setArrival(ZonedDateTime.of(flight.getArrivalLocal(), getZoneId(flight.getAptTo())));
		}
	}
}
