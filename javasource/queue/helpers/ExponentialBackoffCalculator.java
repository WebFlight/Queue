package queue.helpers;

public class ExponentialBackoffCalculator {

	public int calculate(int base, int retry) {
		Double exponentialBackOff = new Double(Math.round(base * Math.pow(2, retry)));
		int exponentialBackOffInt = exponentialBackOff.intValue();
		return exponentialBackOffInt;
	}
	
}