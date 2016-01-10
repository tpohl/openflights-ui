package org.openflights.angular.backend;

import java.time.Duration;

import javax.ejb.Stateless;

import org.openflights.angular.model.Flight;
import org.opensextant.geodesy.Angle;
import org.opensextant.geodesy.Geodetic2DArc;
import org.opensextant.geodesy.Geodetic2DPoint;
import org.opensextant.geodesy.Latitude;
import org.opensextant.geodesy.Longitude;

@Stateless
public class Calculation {
	public Duration calculateDuration(final Flight flight) {
		Duration durationWithoutTimezone = Duration.between(flight.getDeparture().toInstant(),
				flight.getArrival().toInstant());
		double timezoneDifference = (flight.getAptFrom().getTimezone() + 12.0)
				- (flight.getAptTo().getTimezone() + 12.0);
		Duration duration = durationWithoutTimezone.plusHours(Math.round(timezoneDifference));
		
		return duration;
	}

	public double calculateDistance(final Flight flight) {

		Geodetic2DPoint departure = new Geodetic2DPoint(
				new Longitude(flight.getAptFrom().getLongitude(), Angle.DEGREES),
				new Latitude(flight.getAptFrom().getLatitude(), Angle.DEGREES));
		Geodetic2DPoint destination = new Geodetic2DPoint(
				new Longitude(flight.getAptTo().getLongitude(), Angle.DEGREES),
				new Latitude(flight.getAptTo().getLatitude(), Angle.DEGREES));

		Geodetic2DArc route = new Geodetic2DArc(departure, destination);

		double distance = route.getDistanceInMeters() / 1000.0 * 0.62137;
		return distance;
	}

	public static String formatDuration(Duration duration) {
		long seconds = duration.getSeconds();
		long absSeconds = Math.abs(seconds);
		String positive = String.format("%d:%02d", absSeconds / 3600, (absSeconds % 3600) / 60);
		return seconds < 0 ? "-" + positive : positive;
	}
}
