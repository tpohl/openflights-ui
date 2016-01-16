package org.openflights.angular.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.openflights.angular.backend.openflights.OpenflightsApiService;
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

		LOG.info("Flight to be saved {} with session {}", flight, sessionId);
		return openflightsApiService.persistFlight(flight,sessionId);
	}

	@GET
	public Flight getExample() {
		Flight f = new Flight();
		f.setFrom("FROM");
		f.setTo("TO");
		return f;
	}
	@GET
	@Path("list")
	public List<Flight> listFlights(@HeaderParam("openflightssessionid") String sessionId){
		return openflightsApiService.loadAllFlights(sessionId);
	}
}
