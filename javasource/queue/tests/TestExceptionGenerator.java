package queue.tests;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mendix.systemwideinterfaces.MendixRuntimeException;
import com.mendix.systemwideinterfaces.core.UserException;

import queue.helpers.ExceptionGenerator;

public class TestExceptionGenerator {

	private ExceptionGenerator exceptionGenerator = new ExceptionGenerator();
	private String message = "This is a test error message";
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	
	@Test
	public void testWithStackTrace() {
		expectedException.expect(MendixRuntimeException.class);
		expectedException.expectMessage(this.message);
		exceptionGenerator.throwException(true, this.message);
	}
	
	@Test
	public void testWithoutStackTrace() {
		expectedException.expect(UserException.class);
		expectedException.expectMessage(this.message);
		exceptionGenerator.throwException(false, this.message);
	}

}
