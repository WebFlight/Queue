package queue.tests;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import queue.helpers.TimeUnitConverter;

public class TestTimeUnitConverter {

	@Test
	public void testGetTimeUnitDays() {
		TimeUnitConverter timeUnitConverter = new TimeUnitConverter();
		TimeUnit actualTimeUnit = timeUnitConverter.getTimeUnit("Days");
		assertEquals(TimeUnit.DAYS, actualTimeUnit);
	}
	
	@Test
	public void testGetTimeUnitHours() {
		TimeUnitConverter timeUnitConverter = new TimeUnitConverter();
		TimeUnit actualTimeUnit = timeUnitConverter.getTimeUnit("Hours");
		assertEquals(TimeUnit.HOURS, actualTimeUnit);
	}

	@Test
	public void testGetTimeUnitMinutes() {
		TimeUnitConverter timeUnitConverter = new TimeUnitConverter();
		TimeUnit actualTimeUnit = timeUnitConverter.getTimeUnit("Minutes");
		assertEquals(TimeUnit.MINUTES, actualTimeUnit);
	}
	
	@Test
	public void testGetTimeUnitSeconds() {
		TimeUnitConverter timeUnitConverter = new TimeUnitConverter();
		TimeUnit actualTimeUnit = timeUnitConverter.getTimeUnit("Seconds");
		assertEquals(TimeUnit.SECONDS, actualTimeUnit);
	}
	
	@Test
	public void testGetTimeMilliSeconds() {
		TimeUnitConverter timeUnitConverter = new TimeUnitConverter();
		TimeUnit actualTimeUnit = timeUnitConverter.getTimeUnit("Milliseconds");
		assertEquals(TimeUnit.MILLISECONDS, actualTimeUnit);
	}
	
	@Test
	public void testGetTimeMicroSeconds() {
		TimeUnitConverter timeUnitConverter = new TimeUnitConverter();
		TimeUnit actualTimeUnit = timeUnitConverter.getTimeUnit("Microseconds");
		assertEquals(TimeUnit.MICROSECONDS, actualTimeUnit);
	}
	
	@Test
	public void testGetTimeDefault() {
		TimeUnitConverter timeUnitConverter = new TimeUnitConverter();
		TimeUnit actualTimeUnit = timeUnitConverter.getTimeUnit("Era");
		assertEquals(TimeUnit.MILLISECONDS, actualTimeUnit);
	}
}
