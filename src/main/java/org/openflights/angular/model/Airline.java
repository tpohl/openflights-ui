package org.openflights.angular.model;

public class Airline {
	private String code;

	private String name;
	private String openflightsId;

	public Airline() {

	}

	public Airline(String name) {
		super();
		this.name = name;
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return "Airline [code=" + code + ", name=" + name + ", openflightsId=" + openflightsId + "]";
	}

}
