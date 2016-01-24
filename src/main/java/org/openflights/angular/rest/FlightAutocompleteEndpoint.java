package org.openflights.angular.rest;

import java.text.DecimalFormat;
import java.time.ZoneId;
import java.util.Date;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

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
				final FlightStatus lhStatus = lufthansaApiService.getFlightStatus(flight.getFlightNo(),
						flight.getDeparture()==null?null:Date.from(flight.getDeparture().toInstant()));
				if (lhStatus.getFrom() != null) {
					// Only if we found something
					flight.setDeparture(lhStatus.getDeparture());
					flight.setFrom(lhStatus.getFrom());
					flight.setTo(lhStatus.getTo());
					flight.setArrival(lhStatus.getArrival());
					final Aircraft acInfo = lufthansaApiService.getAircraftInfo(lhStatus.getAircraftCode());
					flight.setAcType(acInfo.getName());
				}
			} catch (Exception e) {
				LOG.info("Problem in LH-API", e);
			}
		}

		if (flight.getFlightNo() != null) {
			String airlineId = flight.getFlightNo().substring(0, 2);
			System.out.println("AL=" + airlineId);
			Airline airline = openflightsApiService.loadAirline(airlineId);

			flight.setCarrier(airline);
		}

		if (flight.getFrom() != null) {
			flight.setAptFrom(openflightsApiService.loadAirport(flight.getFrom()));
			
			// FIXME remove.
			DecimalFormat tzFormat = new DecimalFormat("+#0;-#0");
			
			String tz = tzFormat.format(flight.getAptFrom().getTimezone());
			LOG.info("Timzone {} -> {}",flight.getAptFrom().getTimezone(), tz);
			flight.setDeparture(flight.getDeparture().withZoneSameInstant(ZoneId.of(tz)));
		}
		if (flight.getTo() != null) {
			flight.setAptTo(openflightsApiService.loadAirport(flight.getTo()));
		}
		
		return flight;
	}
}
