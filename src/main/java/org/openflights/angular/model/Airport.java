package org.openflights.angular.model;

public class Airport {
	private String code;
	private String name;
	private String openflightsId;

	private double latitude;
	private double longitude;
	private double timezone;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOpenflightsId() {
		return openflightsId;
	}

	public void setOpenflightsId(String openflightsId) {
		this.openflightsId = openflightsId;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getTimezone() {
		return timezone;
	}

	public void setTimezone(double timezone) {
		this.timezone = timezone;
	}

	@Override
	public String toString() {
		return "Airport [code=" + code + ", name=" + name + ", openflightsId=" + openflightsId + ", latitude="
				+ latitude + ", longitude=" + longitude + ", timezone=" + timezone + "]";
	}

}
