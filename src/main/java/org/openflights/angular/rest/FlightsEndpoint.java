package org.openflights.angular.rest;

import java.util.List;

import javax.inject.Inject;
import javax.websocket.server.PathParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.openflights.angular.TimeZoneUtils;
import org.openflights.angular.backend.openflights.OpenflightsApiService;
import org.openflights.angular.model.Airline;
import org.openflights.angular.model.Flight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("flight")
public class FlightsEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(FlightsEndpoint.class);


	@Inject
	OpenflightsApiService openflightsApiService;

	@POST
	public boolean saveFlight(final Flight flight, @HeaderParam("openflightssessionid") String sessionId) {
		TimeZoneUtils.updateTimezones(flight);
		
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
		
		
		LOG.info("Flight to be saved {} with session {}", flight, sessionId);
		return openflightsApiService.persistFlight(flight,sessionId);
	}

	@GET
	@Path("{flightId}")
	public Flight getFlight(@PathParam("flightId")  String flightId,  @HeaderParam("openflightssessionid") String sessionId) {
		Flight f = openflightsApiService.loadFlight(flightId, sessionId);
		return f;
	}
	@GET
	@Path("list")
	public List<Flight> listFlights(@HeaderParam("openflightssessionid") String sessionId){
		return openflightsApiService.loadAllFlights(sessionId);
	}
}
