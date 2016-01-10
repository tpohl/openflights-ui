package org.openflights.angular.backend.lufthansa;

import java.util.Date;

import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;

import org.openflights.angular.backend.lufthansa.model.LufthansaOAuthToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LufthansaAPI {
	private static final String PROPERTY_API_KEY = "org.openflights.lufthansa.api.key";
	private static final String PROPERTY_API_SECRET = "org.openflights.lufthansa.api.secret";
	private static final Logger LOG = LoggerFactory.getLogger(LufthansaAPI.class);
	/**
	 * One shared token for all calls until it expires.
	 */
	private LufthansaOAuthToken token = null;

	protected void retrieveToken() throws Exception {
		LOG.debug("Retrieving new Lufthansa Token.");

		final String apiKey = System.getProperty(PROPERTY_API_KEY);
		final String apiSecret = System.getProperty(PROPERTY_API_SECRET);

		System.out.println(apiKey);
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("https://api.lufthansa.com/v1/oauth/token");
		Form form = new Form();
		form.param("client_id", apiKey);
		form.param("client_secret", apiSecret);
		form.param("grant_type", "client_credentials");

		this.token = target.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), LufthansaOAuthToken.class);

	}

	public Builder authenticateBuilder(final Builder builder) {
		try {
			builder.header("Authorization", "Bearer " + (this.getToken().getAccess_token()));
		} catch (Exception e) {
			LOG.error("Cannot Authenticate with token {}", this.token);
		}
		return builder;
	}

	public LufthansaOAuthToken getToken() throws Exception {
		if (token == null || token.getExpirationDate().before(new Date())) {
			retrieveToken();
		}
		return this.token;
	}
}
