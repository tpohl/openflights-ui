package org.openflights.angular.backend;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONUtils {
	private static final Logger LOG = LoggerFactory.getLogger(JSONUtils.class);

	// FIXME this needs a timezone parameter.
	public static ZonedDateTime extractDate(final JsonObject obj, final String format, final String... properties)
			throws ParseException {
		DateFormat jsonDateFormat = new SimpleDateFormat(format);

		for (String property : properties) {
			try {
				String value = getString(obj, property);
				if (value != null) {
					final Date utilDate = jsonDateFormat.parse(value);
					
					// FIXME Get proper zoneId!
					final ZonedDateTime ldt = ZonedDateTime.ofInstant(utilDate.toInstant(), ZoneId.of("Z"));
					return ldt;
				}
			} catch (NullPointerException e) {
				// Nothing.
				LOG.debug("Property {} not found in {}. Trying next one.", property, obj, e);
			
			}
		}

		return null;

	}

	public static String getString(final JsonObject root, final String path) {
		if (path.contains(".")) {
			final String[] pathElements = path.split("\\.");
			final String lastElement = pathElements[pathElements.length - 1];

			final String[] prefixPathElements = Arrays.copyOf(pathElements, pathElements.length - 1);

			JsonObject obj = getObjectByPath(root, prefixPathElements);
			try {
				return getStringValue(obj, lastElement);
			} catch (NullPointerException e) {
				// Nothing.
				LOG.info("Property {} not found in {}", lastElement, obj, e);
				throw e;
			}

		} else {
			return getStringValue(root, path);
		}
	}

	protected static String getStringValue(JsonObject obj, final String property) {
		JsonValue val = obj.get(property);
		if (val instanceof JsonString) {
			return ((JsonString) val).getString();

		} else {
			return val.toString();
		}
	}

	public static JsonObject getObject(final JsonObject root, final String path) {
		String[] pathElements = path.split("\\.");
		return getObjectByPath(root, pathElements);
	}

	public static JsonObject getObjectByPath(final JsonObject root, final String... path) {
		JsonObject obj = root;
		for (String property : path) {
			try {
				obj = obj.getJsonObject(property);
			} catch (NullPointerException e) {
				// Nothing.
				LOG.info("Property {} not found in {}", property, obj, e);
				throw e;
			}
		}
		return obj;
	}
}
