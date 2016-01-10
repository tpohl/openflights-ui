package org.openflights.angular.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
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
	public boolean saveFlight(final Flight flight) {

		LOG.info("Flight to be saved {}", flight);
		return openflightsApiService.persistFlight(flight);
	}

	@GET
	public Flight getExample() {
		Flight f = new Flight();
		f.setFrom("FROM");
		f.setTo("TO");
		return f;
	}
}
