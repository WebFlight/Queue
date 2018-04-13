package queue.helpers;

import java.util.concurrent.TimeUnit;

public class TimeUnitConverter {
	
	public TimeUnit getTimeUnit (String timeUnitString) {
		if (timeUnitString.equals("Days"))
			return TimeUnit.DAYS;
		if (timeUnitString.equals("Hours"))
			return TimeUnit.HOURS;
		if (timeUnitString.equals("Minutes"))
			return TimeUnit.MINUTES;
		if (timeUnitString.equals("Seconds"))
			return TimeUnit.SECONDS;
		if (timeUnitString.equals("Milliseconds"))
			return TimeUnit.MILLISECONDS;
		if (timeUnitString.equals("Microseconds"))
			return TimeUnit.MICROSECONDS;
			
		return TimeUnit.MILLISECONDS;	
	}

}
