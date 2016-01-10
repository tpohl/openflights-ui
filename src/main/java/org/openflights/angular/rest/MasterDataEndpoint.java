package org.openflights.angular.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.openflights.angular.backend.lufthansa.LufthansaApiService;
import org.openflights.angular.backend.openflights.OpenflightsApiService;
import org.openflights.angular.model.Aircraft;
import org.openflights.angular.model.Airport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("masterdata")
public class MasterDataEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(MasterDataEndpoint.class);
	@Inject
	LufthansaApiService lufthansaApiService;
	@Inject
	OpenflightsApiService openflightsApiService;
	

	@GET
	@Path("airport/{id}")
	public Airport getAirport(final @PathParam("id") String airportCode) {
		//final Airport airport = lufthansaApiService.getAirportInfo(airportCode);
		final Airport airport = openflightsApiService.loadAirport(airportCode);
		return airport;
	}
	
	@GET
	@Path("aircraft/{id}")
	public Aircraft getAircraft(final @PathParam("id") String aircraftCode) {
		final Aircraft ac = lufthansaApiService.getAircraftInfo(aircraftCode);
		
		return ac;
	}
}
