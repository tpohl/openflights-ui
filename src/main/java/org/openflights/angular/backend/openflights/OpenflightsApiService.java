package org.openflights.angular.backend.openflights;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.openflights.angular.backend.Calculation;
import org.openflights.angular.model.Airline;
import org.openflights.angular.model.Airport;
import org.openflights.angular.model.Credentials;
import org.openflights.angular.model.Flight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@Named
public class OpenflightsApiService implements Serializable {
	private static final Logger LOG = LoggerFactory.getLogger(OpenflightsApiService.class);
	@Inject
	Calculation calculation;

	public Airport loadAirport(String searchTerm) {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(UriBuilder.fromPath("http://openflights.org/php/autocomplete.php"));

		Form form = new Form();
		form.param("dst_ap", searchTerm);
		form.param("mode", "F");
		form.param("quick", "true");

		String response = target.request(MediaType.APPLICATION_XML)
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);

		final Airport ap = new Airport();

		int sep1 = response.indexOf(";");

		ap.setName(response.substring(sep1 + 1));

		String idPart = response.substring(0, sep1);
		String[] idParts = idPart.split(":");
		ap.setCode(idParts[0]);
		ap.setOpenflightsId(idParts[1]);
		ap.setLatitude(Double.parseDouble(idParts[3]));
		ap.setLongitude(Double.parseDouble(idParts[2]));
		ap.setTimezone(Double.parseDouble(idParts[4]));

		return ap;
	}

	public Airline loadAirline(String airline) {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(UriBuilder.fromPath("http://openflights.org/php/autocomplete.php"));

		Form form = new Form();
		form.param("airline", airline);
		form.param("mode", "F");
		form.param("quick", "true");

		String response = target.request(MediaType.APPLICATION_XML)
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);

		final Airline al = new Airline();

		int sep1 = response.indexOf(";");

		al.setName(response.substring(sep1 + 1));

		al.setOpenflightsId(response.substring(0, sep1));

		return al;
	}

	public String login(Credentials cred) {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(UriBuilder.fromPath("http://openflights.org/php/submit.php"));

		Form form = new Form();
		form.param("name", cred.getUsername());
		form.param("pw", cred.getPasswordHash());
		form.param("lpw", cred.getPasswordHash());
		form.param("challenge", cred.getChallenge());
		Response response = target.request(MediaType.APPLICATION_XML)
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		return response.getCookies().get("PHPSESSID").getValue();
		
	}

	public boolean persistFlight(Flight flight, String sessionId) {
		LOG.info("Persisting Flight {}", flight);
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(UriBuilder.fromPath("http://openflights.org/php/submit.php"));

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat timeFormat = new SimpleDateFormat("HH:mm");
		Form form = new Form();
		form.param("alid", flight.getCarrier().getOpenflightsId());// alid:2548
		form.param("src_date", dateFormat.format(flight.getDeparture()));// src_date:2016-01-10
		form.param("src_time", timeFormat.format(flight.getDeparture()));// src_time:18:40
		// Airports
		form.param("src_apid", flight.getAptFrom().getOpenflightsId());// src_apid:342
		form.param("dst_apid", flight.getAptTo().getOpenflightsId());// dst_apid:1382
		// Stats
		String duration = Calculation.formatDuration(calculation.calculateDuration(flight));
		form.param("duration", duration);// duration:01:24

		double distance = calculation.calculateDistance(flight);
		form.param("distance", String.valueOf(Math.round(distance)));// distance:452
		// Flight Data
		form.param("number", flight.getFlightNo());// number:4U7406
		form.param("seat", flight.getSeat());// seat:12A
		form.param("type", flight.getSeatType());// type:W
		form.param("class", flight.getBookingClass());// class:Y
		form.param("reason", flight.getReason());// reason:B
		form.param("registration", flight.getAcTailsign());// registration:REG
		form.param("note", "");// note:
		form.param("plane", flight.getAcType());// plane:Airbus A320
		form.param("trid", "NULL");// trid:NULL
		form.param("mode", "F");// mode:F

		// TODO What is this
		form.param("fid", "4824043");// fid:4824043
		form.param("param", "ADD");// param:ADD
		LOG.info("Using Session ID {}", sessionId);
		String response = target.request(MediaType.APPLICATION_XML).cookie("PHPSESSID", sessionId)
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);
		LOG.info("Response of persisting Flight {}", response);
		return response.startsWith("1;");
	}
}
