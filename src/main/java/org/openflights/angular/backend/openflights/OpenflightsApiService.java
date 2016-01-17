package org.openflights.angular.backend.openflights;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.openflights.angular.backend.Calculation;
import org.openflights.angular.backend.openflights.model.LoginPrerequisites;
import org.openflights.angular.model.Airline;
import org.openflights.angular.model.Airport;
import org.openflights.angular.model.Credentials;
import org.openflights.angular.model.Flight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@Named
public class OpenflightsApiService implements Serializable {
	private static final String OPENFLIGHTS_SESSION_COOKIE = "PHPSESSID";
	private static final Logger LOG = LoggerFactory.getLogger(OpenflightsApiService.class);
	@Inject
	Calculation calculation;

	private Map<String, Airline> airlineCache = new HashMap<>();

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

	public Airline loadAirline(final String airline) {
		if (airline == null || airline.length() < 2) {
			
			return null;
		}
		Airline al = this.airlineCache.get(airline);
		if (al == null) {
			LOG.info("loading airline {}", airline);
			try {
				Client client = ClientBuilder.newClient();
				WebTarget target = client.target(UriBuilder.fromPath("http://openflights.org/php/autocomplete.php"));

				Form form = new Form();
				form.param("airline", airline);
				form.param("mode", "F");
				form.param("quick", "true");

				String response = target.request(MediaType.APPLICATION_XML)
						.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);

				al = new Airline();

				int sep1 = response.indexOf(";");

				String fullName = response.substring(sep1 + 1,response.length()-1);
				Pattern p =Pattern.compile("(.*)\\((..)\\).*?");
				Matcher m = p.matcher(fullName);
				if (m.matches()){
					al.setCode(m.group(2));
					al.setName(m.group(1));
					LOG.info("Matched airline name {}", fullName);
				} else {
					LOG.info("Not matched airline name {}", fullName);
					al.setName(fullName);
				}
				al.setOpenflightsId(response.substring(0, sep1));

				this.airlineCache.put(airline, al);
			} catch (Exception e) {
				LOG.warn("problem loading airline {}", airline, e);
				return null;
			}
		}
		return al;
	}

	/**
	 * Performes the openflightsHash as in the frontend.
	 * <code>var hash = hex_md5(challenge
										+ hex_md5(pw + name.toLowerCase()));</code>
	 * 
	 * @param name
	 * @param password
	 * @param challenge
	 * @return
	 */
	public static String openflightsPasswordHash(String name, String password, String challenge) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			byte[] pwName = (password + name.toLowerCase()).getBytes();
			m.update(pwName, 0, pwName.length);
			String pwNameHash = toHex(m.digest());
			MessageDigest m2 = MessageDigest.getInstance("MD5");
			byte[] pass2 = (challenge + pwNameHash).getBytes();
			m2.update(pass2, 0, pass2.length);
			String md5hex = toHex(m2.digest());
			return md5hex;
		} catch (NoSuchAlgorithmException e) {
			// This cannot happen. Hopefully.
			LOG.error("No MD5 Algorothm", e);
			return null;
		}

	}

	/**
	 * Get a Hex from the bytearray.
	 * 
	 * @param bytes
	 *            the bytearray.
	 * @return a Hexstring.
	 */
	public static String toHex(byte[] bytes) {
		BigInteger bi = new BigInteger(1, bytes);
		return String.format("%0" + (bytes.length << 1) + "x", bi);
	}

	public LoginPrerequisites getChallengeAndSession() {
		Client client = ClientBuilder.newClient();

		WebTarget target = client.target(UriBuilder.fromPath("http://openflights.org/php/map.php"));

		Form form = new Form();

		form.param("user", "0");
		form.param("trid", "0");
		form.param("alid", "");
		form.param("year", "");
		form.param("param", "true");

		Response response = target.request(MediaType.APPLICATION_XML)
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		final String sessionId = response.getCookies().get(OPENFLIGHTS_SESSION_COOKIE).getValue();
		final String responseString = response.readEntity(String.class);
		LOG.debug("Response {}", responseString);
		final String firstLine = responseString.substring(0, responseString.indexOf("\n"));
		LOG.debug("Firstline {}", firstLine);
		final String challenge = firstLine.substring(firstLine.lastIndexOf(";") + 1);
		LOG.info("Challenge {}, Session ID {}", challenge, sessionId);
		final LoginPrerequisites result = new LoginPrerequisites();
		result.setSessionId(sessionId);
		result.setChallenge(challenge);
		return result;
	}

	/**
	 * Performes a login on the openflights website.
	 * 
	 * @param cred
	 *            the credentials to use.
	 * @return the Session ID.
	 * @throws NoSuchAlgorithmException
	 */
	public String login(Credentials cred) {

		final LoginPrerequisites preReq = this.getChallengeAndSession();
		final String sessionId = preReq.getSessionId();

		final String challenge = preReq.getChallenge();
		final String passwordHash = openflightsPasswordHash(cred.getUsername(), cred.getPassword(), challenge);

		Client client = ClientBuilder.newClient();

		WebTarget target = client.target(UriBuilder.fromPath("http://openflights.org/php/login.php"));

		Form form = new Form();
		form.param("name", cred.getUsername());
		form.param("pw", passwordHash);
		form.param("lpw", passwordHash);
		form.param("challenge", challenge);
		Response response = target.request(MediaType.APPLICATION_XML).cookie(OPENFLIGHTS_SESSION_COOKIE, sessionId)
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		final String responseText = response.readEntity(String.class);
		LOG.info("Login Response {}", responseText);
		return sessionId;

	}

	/**
	 * Persists a flight.
	 * 
	 * @param flight
	 *            the flight to persist.
	 * @param sessionId
	 *            the session id of the user.
	 * @return if the save was successful.
	 */
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
		String response = target.request(MediaType.APPLICATION_XML).cookie(OPENFLIGHTS_SESSION_COOKIE, sessionId)
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);
		LOG.info("Response of persisting Flight {}", response);
		return response.startsWith("1;");
	}

	/**
	 * lists all flights of the user.
	 * 
	 * @param sessionId
	 *            the sessionId at openflights.
	 * @return a List of Flight objects.
	 */
	public List<Flight> loadAllFlights(String sessionId) {

		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(UriBuilder.fromPath("http://openflights.org/php/flights.php"));

		Form form = new Form();
		form.param("user", "0");
		form.param("trid", "0");
		form.param("alid", "0");
		form.param("year", "0");
		form.param("param", "MAP");
		form.param("id", "0");

		LOG.info("Using Session ID {}", sessionId);
		String response = target.request(MediaType.APPLICATION_XML).cookie(OPENFLIGHTS_SESSION_COOKIE, sessionId)
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);
		LOG.info("Response of listing Flights {}", response);

		CSVFormat format = CSVFormat.DEFAULT.withDelimiter('\t').withHeader("from", "from_code", "to", "to_code",
				"flightNo", "date", "distance", "duration", "seat", "reason", "bookingclass", "ka", "ID", "acType",
				"acTailsign", "ka2", "note", "tripId", "ka4", "airline", "departureTime", "ka5");
		try {
			final CSVParser parser = new CSVParser(new StringReader(response), format);
			List<Flight> flights = new ArrayList<>();
			try {
				for (final CSVRecord record : parser) {
//LOG.info("ka4{} airline{} departureTime{} ka5{}",record.get("ka4"),record.get("airline"),record.get("departureTime"),record.get("ka5"));
					final Flight f = new Flight();

					f.setAcTailsign(record.get("acTailsign"));
					f.setAcType(record.get("acType"));
					f.setArrival(null);
					f.setBookingClass(record.get("bookingclass"));
					f.setCarrier(this.loadAirline(record.get("airline")));
					f.setDeparture(null);
					f.setFlightNo(record.get("flightNo"));
					f.setFrom(record.get("from"));
					// f.setAptFrom(this.loadAirport(f.getFrom()));
					f.setReason(record.get("reason"));
					f.setSeat(record.get("seat"));
					f.setSeatType("");
					f.setTo(record.get("to"));
					// f.setAptTo(this.loadAirport(f.getTo()));

					flights.add(f);
					LOG.debug("Parsed flight {}", f);
				}

				return flights;
			} finally {
				parser.close();

			}
		} catch (IOException e) {
			LOG.error("Problem parsing flights {}", response, e);
			return null;
		}

	}
}
