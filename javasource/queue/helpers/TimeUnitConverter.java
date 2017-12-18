package queue.helpers;

import java.util.concurrent.TimeUnit;

public class TimeUnitConverter {
	
	public static TimeUnit getTimeUnit (String timeUnitString) {
		switch(timeUnitString) {
		case "Days":
			return TimeUnit.DAYS;
		case "Hours":
			return TimeUnit.HOURS;
		case "Minutes":
			return TimeUnit.MINUTES;
		case "Seconds":
			return TimeUnit.SECONDS;
		case "Milliseconds":
			return TimeUnit.MILLISECONDS;
		case "Microseconds":
			return TimeUnit.MICROSECONDS;
			
		default: return TimeUnit.MILLISECONDS;	
		}
	}

}
