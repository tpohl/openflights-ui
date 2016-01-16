package org.openflights.angular.rest;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.openflights.angular.backend.openflights.OpenflightsApiService;
import org.openflights.angular.model.Credentials;
import org.openflights.angular.model.SessionId;

@Path("login")
public class LoginEndpoint {
	@Inject
	OpenflightsApiService openflightsApiService;
	@POST
	public SessionId doLogin(Credentials cred) {
		
		String sessionId = openflightsApiService.login(cred);
		return new SessionId(sessionId);
	}
}
