package org.openflights.angular.rest;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.openflights.angular.TimeZoneUtils;
import org.openflights.angular.backend.lufthansa.LufthansaApiService;
import org.openflights.angular.backend.lufthansa.model.FlightStatus;
import org.openflights.angular.backend.openflights.OpenflightsApiService;
import org.openflights.angular.model.Aircraft;
import org.openflights.angular.model.Airline;
import org.openflights.angular.model.Flight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("flight-autocomplete")
public class FlightAutocompleteEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(FlightAutocompleteEndpoint.class);
	@Inject
	LufthansaApiService lufthansaApiService;
	@Inject
	OpenflightsApiService openflightsApiService;

	@POST
	@Path("flightNo")
	public Flight autocompleteByFlightNumber(Flight flight) {
		if (flight.getFlightNo() != null) {
			try {
				// TODO better. Date handling.

				// Find the date of the flight.
				final String dateOfFlight;
				if (flight.getDeparture() != null) {
					dateOfFlight = flight.getDeparture().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
				} else if (flight.getDepartureLocal() != null) {
					dateOfFlight = flight.getDepartureLocal().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
				} else {
					dateOfFlight = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
				}

				final FlightStatus lhStatus = lufthansaApiService.getFlightStatus(flight.getFlightNo(), dateOfFlight);
				if (lhStatus.getFrom() != null) {
					// Only if we found something
					flight.setDeparture(lhStatus.getDeparture());
					flight.setDepartureLocal(lhStatus.getDepartureLocal());
					flight.setFrom(lhStatus.getFrom());
					flight.setTo(lhStatus.getTo());
					flight.setArrival(lhStatus.getArrival());
					flight.setArrivalLocal(lhStatus.getArrivalLocal());
					final Aircraft acInfo = lufthansaApiService.getAircraftInfo(lhStatus.getAircraftCode());
					flight.setAcType(acInfo.getName());
				}
			} catch (Exception e) {
				LOG.info("Problem in LH-API", e);
			}
		}

		if (flight.getFlightNo() != null) {
			String airlineId = flight.getFlightNo().substring(0, 2);
			Airline airline = openflightsApiService.loadAirline(airlineId);

			flight.setCarrier(airline);
		}

		if (flight.getFrom() != null) {
			flight.setAptFrom(openflightsApiService.loadAirport(flight.getFrom()));
		}
		if (flight.getTo() != null) {
			flight.setAptTo(openflightsApiService.loadAirport(flight.getTo()));
		}

		TimeZoneUtils.updateTimezones(flight);
		return flight;
	}
}
