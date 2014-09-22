package io.boxcar.publisher.client.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtils {

	public static Date getExpiryDate(long timeToLiveMillis) {
		long unixDate = System.currentTimeMillis() + timeToLiveMillis;
		return convertLocalTimestampToUTC(unixDate);
	}
	
	public static Date convertLocalTimestampToUTC(long millis) {
		TimeZone tz = TimeZone.getDefault();
		Calendar c = Calendar.getInstance(tz);
		long localMillis = millis;
		int offset, time;

		c.set(1970, Calendar.JANUARY, 1, 0, 0, 0);

		// Add milliseconds
		while (localMillis > Integer.MAX_VALUE) {
			c.add(Calendar.MILLISECOND, Integer.MAX_VALUE);
			localMillis -= Integer.MAX_VALUE;
		}
		c.add(Calendar.MILLISECOND, (int) localMillis);

		// Stupidly, the Calendar will give us the wrong result if we use
		// getTime() directly.
		// Instead, we calculate the offset and do the math ourselves.
		time = c.get(Calendar.MILLISECOND);
		time += c.get(Calendar.SECOND) * 1000;
		time += c.get(Calendar.MINUTE) * 60 * 1000;
		time += c.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000;
		offset = tz.getOffset(c.get(Calendar.ERA), c.get(Calendar.YEAR),
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
				c.get(Calendar.DAY_OF_WEEK), time);

		return new Date(millis - offset);
	}

}
