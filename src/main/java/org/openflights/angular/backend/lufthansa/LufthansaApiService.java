package org.openflights.angular.backend.lufthansa;

import static org.openflights.angular.backend.JSONUtils.extractDate;
import static org.openflights.angular.backend.JSONUtils.getObject;
import static org.openflights.angular.backend.JSONUtils.getString;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;
import javax.json.JsonObject;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.openflights.angular.backend.JSONUtils;
import org.openflights.angular.backend.lufthansa.model.FlightStatus;
import org.openflights.angular.backend.lufthansa.model.LhApiAircraft;
import org.openflights.angular.backend.lufthansa.model.LhApiAirport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class LufthansaApiService {
	public static final String LH_API_DATEFORMAT = "yyyy-MM-dd'T'HH:mm";
	private static final Logger LOG = LoggerFactory.getLogger(LufthansaApiService.class);
	@Inject
	LufthansaAPI lhApi;

	/**
	 * Gets the Flight Status from the Lufthansa API.
	 * 
	 * @param flightNo
	 *            the flight-number you are interested in.
	 * @param date
	 *            optional Date of Flight (will default to today).
	 * @return a FlightStatus which might be empty, if nothing was found found.
	 */
	public FlightStatus getFlightStatus(final String flightNo, final Date date) {
		final FlightStatus status = new FlightStatus();
		try {

			DateFormat parameterFormat = new SimpleDateFormat("yyyy-MM-dd");
			String dateParameter = parameterFormat.format(date != null ? date : new Date());
			Client client = ClientBuilder.newClient();
			WebTarget target = client.target(
					UriBuilder.fromPath("https://api.lufthansa.com/v1/operations/flightstatus/{flightNo}/{date}")
							.build(flightNo, dateParameter));
			JsonObject jsonStatus = lhApi.authenticateBuilder(target.request(MediaType.APPLICATION_JSON))
					.get(JsonObject.class);
			LOG.info("Flight-Status JSON: {}", jsonStatus);

			if (jsonStatus != null) {
				try {
					final JsonObject flight = getObject(jsonStatus, "FlightStatusResource.Flights.Flight");

					status.setFrom(getString(flight, "Departure.AirportCode"));
					status.setDeparture(JSONUtils.extractDate(flight, LH_API_DATEFORMAT,
							"Departure.ActualTimeUTC.DateTime", "Departure.ScheduledTimeUTC.DateTime"));

					status.setTo(getString(flight, "Arrival.AirportCode"));
					status.setArrival(extractDate(flight, LH_API_DATEFORMAT, "Arrival.ActualTimeUTC.DateTime",
							"Arrival.ScheduledTimeUTC.DateTime"));
					status.setAirlineCode(getString(flight, "OperatingCarrier.AirlineID"));
					status.setAircraftCode(getString(flight, "Equipment.AircraftCode"));
					status.setFlightNo(flightNo);
					LOG.debug("Status {}", status);
				} catch (NullPointerException npe) {
					LOG.warn("Cannot extract status from JSON {}", jsonStatus, npe);
				}
			} else {
				LOG.info("Not found: Flight No: {} @ {}", flightNo, date);
			}

		} catch (ServerErrorException e) {
			LOG.info("Server-Error in LH-API", e);
		} catch (Exception e) {
			LOG.warn("Unexpected-Error in LH-API", e);
		}
		return status;
	}

	/**
	 * Gets Airport Information.
	 * 
	 * @param airportCode
	 *            the Airport Code
	 * @return the Airport - might be empty, if not found.
	 */
	public LhApiAirport getAirportInfo(final String airportCode) {
		final LhApiAirport airport = new LhApiAirport();
		try {
			Client client = ClientBuilder.newClient();

			WebTarget target = client
					.target(UriBuilder.fromPath("https://api.lufthansa.com/v1/references/airports/{airportCode}")
							.queryParam("lang", "en").queryParam("LHoperated", "false").build(airportCode));

			JsonObject json = lhApi.authenticateBuilder(target.request(MediaType.APPLICATION_JSON))
					.get(JsonObject.class);
			LOG.info("Airport JSON: {}", json);

			if (json != null) {
				try {
					final JsonObject jsonAirport = getObject(json, "AirportResource.Airports.Airport");
					airport.setCode(getString(jsonAirport, "AirportCode"));
					airport.setName(getString(jsonAirport, "Names.Name.$"));
					// TODO latitude & longitude
					LOG.info("Airport {}", airport);
				} catch (NullPointerException npe) {
					LOG.warn("Cannot extract airport from JSON {}", airport, npe);
				}
			}

		} catch (ServerErrorException e) {
			LOG.info("Server-Error in LH-API", e);
		} catch (Exception e) {
			LOG.warn("Unexpected-Error in LH-API", e);
		}
		return airport;
	}

	public LhApiAircraft getAircraftInfo(final String aircraftCode) {
		final LhApiAircraft aircraft = new LhApiAircraft();
		try {
			Client client = ClientBuilder.newClient();

			WebTarget target = client
					.target(UriBuilder.fromPath("https://api.lufthansa.com/v1/references/aircraft/{aircraftCode}")
							.queryParam("lang", "en").build(aircraftCode));

			JsonObject json = lhApi.authenticateBuilder(target.request(MediaType.APPLICATION_JSON))
					.get(JsonObject.class);
			LOG.info("Aircraft JSON: {}", json);

			if (json != null) {
				try {
					final JsonObject jsonAircraft = getObject(json,
							"AircraftResource.AircraftSummaries.AircraftSummary");
					aircraft.setCode(getString(jsonAircraft, "AircraftCode"));
					aircraft.setName(getString(jsonAircraft, "Names.Name.$"));

					LOG.info("Found Aircraft {}", aircraft);
				} catch (NullPointerException npe) {
					LOG.warn("Cannot extract airport from JSON {}", aircraft, npe);
				}
			}

		} catch (ServerErrorException e) {
			LOG.info("Server-Error in LH-API", e);
		} catch (Exception e) {
			LOG.warn("Unexpected-Error in LH-API", e);
		}
		return aircraft;
	}
}
