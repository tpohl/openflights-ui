package org.openflights.angular.model;

public class Airline {
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

	@Override
	public String toString() {
		return "Airline [name=" + name + ", openflightsId=" + openflightsId + "]";
	}

}
