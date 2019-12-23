package queue.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import queue.helpers.ExponentialBackoffCalculator;

public class TestExponentialBackoffCalculator {

	@Test
	public void testCalculate() {
		ExponentialBackoffCalculator exponentialBackoffCalculator = new ExponentialBackoffCalculator();
		int base = 500;
		int retry = 3;
		int expectedResult = Math.toIntExact(Math.round(base * Math.pow(2, retry))); 
		int actualResult = exponentialBackoffCalculator.calculate(base, retry);
		assertEquals(expectedResult, actualResult);
	}

}
