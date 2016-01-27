package org.openflights.angular.backend.openflights;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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
import org.openflights.angular.TimeZoneUtils;
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
	private static final String CSV_HEADER_DEPARTURE_TIME = "departureTime";
	private static final String CSV_HEADER_AIRLINE = "airline";
	private static final String CSV_HEADER_TRIP_ID = "tripId";
	private static final String CSV_HEADER_NOTE = "note";
	private static final String CSV_HEADER_AC_TAILSIGN = "acTailsign";
	private static final String CSV_HEADER_AC_TYPE = "acType";
	private static final String CSV_HEADER_ID = "ID";
	private static final String CSV_HEADER_BOOKINGCLASS = "bookingclass";
	private static final String CSV_HEADER_REASON = "reason";
	private static final String CSV_HEADER_SEAT = "seat";
	private static final String CSV_HEADER_DURATION = "duration";
	private static final String CSV_HEADER_DISTANCE = "distance";
	private static final String CSV_HEADER_DEPARTURE_DATE = "date";
	private static final String CSV_HEADER_FLIGHT_NO = "flightNo";
	private static final String CSV_HEADER_TO_CODE = "to_code";
	private static final String CSV_HEADER_TO = "to";
	private static final String CSV_HEADER_FROM_CODE = "from_code";
	private static final String CSV_HEADER_FROM = "from";
	private static final String OPENFLIGHTS_SESSION_COOKIE = "PHPSESSID";
	private static final Logger LOG = LoggerFactory.getLogger(OpenflightsApiService.class);
	private static final String CSV_HEADER_SEATTYPE = "seattype";
	@Inject
	Calculation calculation;

	private Map<String, Airline> airlineCache = new HashMap<>();

	public Airport loadAirport(String searchTerm) {
		LOG.info("Loading Airport {}", searchTerm);
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
				form.param(CSV_HEADER_AIRLINE, airline);
				form.param("mode", "F");
				form.param("quick", "true");

				String response = target.request(MediaType.APPLICATION_XML)
						.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);

				al = new Airline();

				int sep1 = response.indexOf(";");

				String fullName = response.substring(sep1 + 1, response.length() - 1);
				Pattern p = Pattern.compile("(.*)\\((..)\\).*?");
				Matcher m = p.matcher(fullName);
				if (m.matches()) {
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
		form.param(CSV_HEADER_DURATION, duration);// duration:01:24

		double distance = calculation.calculateDistance(flight);
		form.param(CSV_HEADER_DISTANCE, String.valueOf(Math.round(distance)));// distance:452
		// Flight Data
		form.param("number", flight.getFlightNo());// number:4U7406
		form.param(CSV_HEADER_SEAT, flight.getSeat());// seat:12A
		form.param("type", flight.getSeatType());// type:W
		form.param("class", flight.getBookingClass());// class:Y
		form.param(CSV_HEADER_REASON, flight.getReason());// reason:B
		form.param("registration", flight.getAcTailsign());// registration:REG
		form.param(CSV_HEADER_NOTE, "");// note:
		form.param("plane", flight.getAcType());// plane:Airbus A320
		form.param("trid", "NULL");// trid:NULL
		form.param("mode", "F");// mode:F

		// TODO What is this
		form.param("fid", "0");
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

		/*
		 * 
		 * FRA 340 HAM 342 LH020 2016-01-17 414 00:57 8B M Y L 4857094 Airbus
		 * A321-100/200 D-AIRT 3320 3786 LH 15:21:00 F HAM 342 FRA 340 LH009
		 * 2016-01-17 414 01:04 35C A Y L 4857093 Airbus A321-100/200 D-AIRU
		 * 3320 3786 LH 10:21:00 F CDG 1382 HAM 342 4U7407 2016-01-12 727 01:24
		 * 12F W Y B 4836940 Airbus A320 D-AIPU 2548 4 4U 20:20:00 F HAM 342 CDG
		 * 1382 4U7406 2016-01-10 727 01:35 12A W Y B 4824047 Airbus A320 D-AIQN
		 * 2548 4 4U 18:05:00 F FRA 340 HAM 342 LH024 2015-11-20 412 01:01 29A W
		 * Y B 4687364 Airbus A321 D-AISO 3320 5 LH 17:00:00 F HAM 342 FRA 340
		 * LH027 2015-11-17 412 01:01 30A W Y B 4678153 Airbus A321 D-AIRS 3320
		 * 5 LH 17:00:00 F DUS 345 HAM 342 4U 7069 2015-10-28 340 00:55 14F W Y
		 * B 4643797 Airbus A319-100 D-AGWQ 2548 76 4U 17:45:00 F HAM 342 DUS
		 * 345 4U9031 2015-10-28 340 00:55 15F W Y B 4643519 Airbus A319 D-AKNV
		 * 2548 9 4U 08:20:00 F FRA 340 HAM 342 LH028 2015-08-26 412 01:01 25C A
		 * Y B 4527153 Airbus A321 D-AIS? 3320 5 LH 18:00:00 F HAM 342 FRA 340
		 * LH027 2015-08-23 412 01:01 11F W Y B 4520490 Airbus A321-200 D-AISG
		 * 3320 66 LH 17:00:00 F FRA 340 HAM 342 LH028 2015-07-10 412 01:01 24F
		 * W Y B 4368085 Airbus A321 D-AIRK 3320 5 LH 18:00:00 F HAM 342 FRA 340
		 * LH029 2015-07-07 412 01:01 10A W Y B 4364233 Airbus A319 D-AILL 3320
		 * 9 LH 18:00:00 F MUC 346 HAM 342 LH2080 2015-06-02 599 01:15 23F W Y B
		 * 4294050 Airbus A319 D-AIBE 3320 9 LH 18:00:00 F HAM 342 MUC 346 LH
		 * 2083 2015-06-01 599 01:15 34A W Y B 4292387 Airbus A321-200 D-AIDU
		 * 3320 66 LH 06:30:00 F CGN 344 HAM 342 4U 030 2015-05-12 362 00:57 18A
		 * Y B 4260504 Airbus A319-100 D-AGWI 2548 76 4U 19:05:00 F HAM 342 CGN
		 * 344 4U 35 2015-05-11 362 00:57 12F Y B 4259799 Airbus A319-100 D-AGWH
		 * 2548 76 4U 17:25:00 F FRA 340 HAM 342 LH024 2015-03-06 412 01:01 24A
		 * W Y B 4148835 Airbus A321-200 D-AISH 3320 Inflight Entertainment 66
		 * LH 17:00:00 F
		 * 
		 */

		CSVFormat format = CSVFormat.DEFAULT.withDelimiter('\t').withHeader(CSV_HEADER_FROM, CSV_HEADER_FROM_CODE,
				CSV_HEADER_TO, CSV_HEADER_TO_CODE, CSV_HEADER_FLIGHT_NO, CSV_HEADER_DEPARTURE_DATE, CSV_HEADER_DISTANCE,
				CSV_HEADER_DURATION, CSV_HEADER_SEAT, CSV_HEADER_REASON, CSV_HEADER_BOOKINGCLASS, "ka", CSV_HEADER_ID,
				CSV_HEADER_AC_TYPE, CSV_HEADER_AC_TAILSIGN, "ka2", CSV_HEADER_NOTE, CSV_HEADER_TRIP_ID, "ka4",
				CSV_HEADER_AIRLINE, CSV_HEADER_DEPARTURE_TIME, "ka5");
		try {
			final CSVParser parser = new CSVParser(new StringReader(response), format);
			List<Flight> flights = new ArrayList<>();

			try {
				for (final CSVRecord record : parser) {
					// LOG.info("ka4{} airline{} departureTime{}
					// ka5{}",record.get("ka4"),record.get("airline"),record.get("departureTime"),record.get("ka5"));
					final Flight f = new Flight();

					f.setId(record.get(CSV_HEADER_ID));
					f.setAcTailsign(record.get(CSV_HEADER_AC_TAILSIGN));
					f.setAcType(record.get(CSV_HEADER_AC_TYPE));
					f.setArrival(null); // How can we get this.
					f.setBookingClass(record.get(CSV_HEADER_BOOKINGCLASS));
					f.setCarrier(this.loadAirline(record.get(CSV_HEADER_AIRLINE)));

					f.setDepartureLocal(
							parseDate(record.get(CSV_HEADER_DEPARTURE_DATE), record.get(CSV_HEADER_DEPARTURE_TIME)));

					f.setFlightNo(record.get(CSV_HEADER_FLIGHT_NO));
					f.setFrom(record.get(CSV_HEADER_FROM));
					// f.setAptFrom(this.loadAirport(f.getFrom()));
					f.setReason(record.get(CSV_HEADER_REASON));
					f.setSeat(record.get(CSV_HEADER_SEAT));
					f.setSeatType("");
					f.setTo(record.get(CSV_HEADER_TO));
					// f.setAptTo(this.loadAirport(f.getTo()));

					TimeZoneUtils.updateTimezones(f);
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

	public Flight loadFlight(String flightId, String sessionId) {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(UriBuilder.fromPath("http://openflights.org/php/flights.php"));
		LOG.info("Loading Flight with id {}", flightId);
		Form form = new Form();
		form.param("user", "0");
		form.param("trid", "0");
		form.param("alid", "0");
		form.param("year", "0");
		form.param("param", "EDIT");
		form.param("fid", flightId);

		LOG.info("Using Session ID {}", sessionId);
		String response = target.request(MediaType.APPLICATION_XML).cookie(OPENFLIGHTS_SESSION_COOKIE, sessionId)
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);
		LOG.info("Response of getting Flight {}", response);
		CSVFormat format = CSVFormat.DEFAULT.withDelimiter('\t').withHeader(CSV_HEADER_FROM, CSV_HEADER_FROM_CODE,
				CSV_HEADER_TO, CSV_HEADER_TO_CODE, CSV_HEADER_FLIGHT_NO, CSV_HEADER_DEPARTURE_DATE, CSV_HEADER_DISTANCE,
				CSV_HEADER_DURATION, CSV_HEADER_SEAT, CSV_HEADER_SEATTYPE, CSV_HEADER_BOOKINGCLASS, CSV_HEADER_REASON,
				CSV_HEADER_ID, CSV_HEADER_AC_TYPE, CSV_HEADER_AC_TAILSIGN, "airlineId", "e1", "e2", "5",
				CSV_HEADER_AIRLINE, CSV_HEADER_DEPARTURE_TIME, "mode");
		try {
			final CSVParser parser = new CSVParser(new StringReader(response), format);
			CSVRecord record = parser.iterator().next();
			// FRA 340 HAM 342 LH024 2015-11-20 256 01:01 29A W Y B 4687364
			// Airbus
			// A321 D-AISO 3320 5 LH 17:00:00 F
			Flight f = parseFlight(record);

			TimeZoneUtils.updateTimezones(f);
			parser.close();
			return f;
		} catch (Exception e) {
			LOG.error("Could not parse Flight {}", response, e);
			return null;
		}
	}

	protected Flight parseFlight(CSVRecord record) {
		Flight f = new Flight();
		f.setId(record.get(CSV_HEADER_ID));
		f.setAcTailsign(record.get(CSV_HEADER_AC_TAILSIGN));
		f.setAcType(record.get(CSV_HEADER_AC_TYPE));
		f.setArrival(null); // How can we get this.
		f.setBookingClass(record.get(CSV_HEADER_BOOKINGCLASS));
		f.setCarrier(this.loadAirline(record.get(CSV_HEADER_AIRLINE)));

		f.setDepartureLocal(parseDate(record.get(CSV_HEADER_DEPARTURE_DATE), record.get(CSV_HEADER_DEPARTURE_TIME)));

		f.setFlightNo(record.get(CSV_HEADER_FLIGHT_NO));
		f.setFrom(record.get(CSV_HEADER_FROM));
		// f.setAptFrom(this.loadAirport(f.getFrom()));
		f.setReason(record.get(CSV_HEADER_REASON));
		f.setSeat(record.get(CSV_HEADER_SEAT));
		f.setSeatType("");
		f.setTo(record.get(CSV_HEADER_TO));
		// f.setAptTo(this.loadAirport(f.getTo()));
		return f;
	}

	/**
	 * Parses the local date from the openflights api.
	 * 
	 * @param date
	 *            the date formatted dd.MM.yyyy
	 * @param time
	 *            the time formatted hh:mm:ss
	 * @return a {@link LocalDateTime} or null.
	 */
	private LocalDateTime parseDate(String date, String time) {
		try {
			Integer day = Integer.valueOf(date.substring(0, 4));
			Integer month = Integer.valueOf(date.substring(5, 7));
			Integer year = Integer.valueOf(date.substring(8, 10));

			Integer hour = 0;
			Integer minute = 0;
			if (time != null && time.length() == 8) {
				hour = Integer.valueOf(time.substring(0, 2));
				minute = Integer.valueOf(time.substring(3, 5));
			}
			return LocalDateTime.of(day, month, year, hour, minute);
		} catch (Exception e) {
			LOG.info("Problem parsing Date {} {}", date, time, e);
			return null;
		}

	}

}
