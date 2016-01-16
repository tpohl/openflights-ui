package org.openflights.angular.backend.openflights.model;

public class LoginPrerequisites {
	private String sessionId;
	private String challenge;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getChallenge() {
		return challenge;
	}

	public void setChallenge(String challenge) {
		this.challenge = challenge;
	}

}
