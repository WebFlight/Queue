package queue.tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import queue.helpers.ExponentialBackoffCalculator;

class TestExponentialBackoffCalculator {

	@Test
	void testCalculate() {
		ExponentialBackoffCalculator exponentialBackoffCalculator = new ExponentialBackoffCalculator();
		int base = 500;
		int retry = 3;
		int expectedResult = new Double(Math.round(base * Math.pow(2, retry))).intValue(); 
		int actualResult = exponentialBackoffCalculator.calculate(base, retry);
		assertEquals(expectedResult, actualResult);
	}

}
