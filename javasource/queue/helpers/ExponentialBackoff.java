package queue.helpers;

public class ExponentialBackoff {

	public static int getExponentialBackOff(int base, int retry) {
		Double exponentialBackOff = new Double(Math.round(base * Math.pow(2, retry)));
		int exponentialBackOffInt = exponentialBackOff.intValue();
		return exponentialBackOffInt;
	}
	
}
