package org.openflights.angular.backend.lufthansa.model;

import java.io.Serializable;
import java.util.Date;

public class LufthansaOAuthToken implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7641512415480499422L;

	protected String access_token;
	
	protected String token_type;
	
	protected long expires_in;
	
	protected Date  expirationDate = new Date();
	
	

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getToken_type() {
		return token_type;
	}

	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}

	public long getExpires_in() {
		
		return expires_in;
	}

	public void setExpires_in(long expires_in) {
		long time = new Date().getTime();		
		
		this.expirationDate= new Date(time + (expires_in*1000));
		this.expires_in = expires_in;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}
	
}