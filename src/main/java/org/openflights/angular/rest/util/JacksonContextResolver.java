package org.openflights.angular.rest.util;

import java.time.LocalDateTime;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;

/**
 * This fixes the issue that {@link LocalDateTime} is not working in
 * Rest-Services. I do not like that this assumes we are using jackson..
 * 
 * @author thorsten
 *
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonContextResolver implements ContextResolver<ObjectMapper> {
	private static final ObjectMapper om = init();

	@Override
	public ObjectMapper getContext(Class<?> objectType) {
		return om;
	}

	private static ObjectMapper init() {
		ObjectMapper om = new ObjectMapper();
		om.registerModule(new JSR310Module());
		om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		
		return om;
	}
}