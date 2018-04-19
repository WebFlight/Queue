package queue.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import com.mendix.logging.ILogNode;

import queue.helpers.QueueValidator;
import queue.repositories.QueueRepository;

public class TestQueueValidator {
	
	ILogNode logger = mock(ILogNode.class);
	QueueRepository queueRepository = mock(QueueRepository.class);
	

	@Test
	public void valid() {
		QueueValidator queueValidator = new QueueValidator(logger);
		String name = "NewQueue";
		int poolSize = 3;
		int priority = 5;
		
		when(queueRepository.queueExists(name)).thenReturn(false);
		
		boolean isValid = queueValidator.isValid(queueRepository, name, poolSize, priority);
		
		assertTrue(isValid);
		verify(queueRepository, times(1)).queueExists(name);
		verify(logger, times(1)).debug("Pool size " + poolSize + " is a valid number.");
		verify(logger, times(1)).debug("Thread priority " + priority + " is a valid number.");
	}
	
	@Test
	public void notValidNameEmpty() {
		QueueValidator queueValidator = new QueueValidator(logger);
		String name = "";
		int poolSize = 3;
		int priority = 5;
		
		when(queueRepository.queueExists(name)).thenReturn(false);
		
		boolean isValid = queueValidator.isValid(queueRepository, name, poolSize, priority);
		
		assertFalse(isValid);
		verify(queueRepository, times(0)).queueExists(name);
		verify(logger, times(1)).error("Queue name is missing.");
	}
	
	@Test
	public void notValidNameNull() {
		QueueValidator queueValidator = new QueueValidator(logger);
		String name = null;
		int poolSize = 3;
		int priority = 5;
		
		when(queueRepository.queueExists(name)).thenReturn(false);
		
		boolean isValid = queueValidator.isValid(queueRepository, name, poolSize, priority);
		
		assertFalse(isValid);
		verify(queueRepository, times(0)).queueExists(name);
		verify(logger, times(1)).error("Queue name is missing.");
	}
	
	@Test
	public void notValidQueueExists() {
		QueueValidator queueValidator = new QueueValidator(logger);
		String name = "NewQueue";
		int poolSize = 3;
		int priority = 5;
		
		when(queueRepository.queueExists(name)).thenReturn(true);
		
		boolean isValid = queueValidator.isValid(queueRepository, name, poolSize, priority);
		
		assertFalse(isValid);
		verify(queueRepository, times(1)).queueExists(name);
		verify(logger, times(1)).error("Queue " + name + " has already been initialized.");
	}
	
	@Test
	public void notValidPoolSize() {
		QueueValidator queueValidator = new QueueValidator(logger);
		String name = "NewQueue";
		int poolSize = -1;
		int priority = 5;
		
		when(queueRepository.queueExists(name)).thenReturn(false);
		
		boolean isValid = queueValidator.isValid(queueRepository, name, poolSize, priority);
		
		assertFalse(isValid);
		verify(logger, times(1)).error("Pool size of " + poolSize + " is not valid. Number should be greater than 0.");
	}
	
	@Test
	public void notValidPriorityMin() {
		QueueValidator queueValidator = new QueueValidator(logger);
		String name = "NewQueue";
		int poolSize = 3;
		int priority = 0;
		
		when(queueRepository.queueExists(name)).thenReturn(false);
		
		boolean isValid = queueValidator.isValid(queueRepository, name, poolSize, priority);
		
		assertFalse(isValid);
		verify(logger, times(1)).error("Thread priority " + priority + " is not valid, should be a number between " + Thread.MIN_PRIORITY + " and " + Thread.MAX_PRIORITY + ".");
	}
	
	@Test
	public void notValidPriorityMax() {
		QueueValidator queueValidator = new QueueValidator(logger);
		String name = "NewQueue";
		int poolSize = 3;
		int priority = 11;
		
		when(queueRepository.queueExists(name)).thenReturn(false);
		
		boolean isValid = queueValidator.isValid(queueRepository, name, poolSize, priority);
		
		assertFalse(isValid);
		verify(logger, times(1)).error("Thread priority " + priority + " is not valid, should be a number between " + Thread.MIN_PRIORITY + " and " + Thread.MAX_PRIORITY + ".");
	}
	
	

}
