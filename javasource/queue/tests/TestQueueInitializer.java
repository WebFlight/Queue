package queue.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import com.mendix.logging.ILogNode;

import queue.entities.QueueConfiguration;
import queue.factories.QueueThreadFactory;
import queue.factories.QueueThreadPoolFactory;
import queue.helpers.QueueInitializer;
import queue.helpers.QueueValidator;
import queue.repositories.QueueRepository;

public class TestQueueInitializer {
	
	ILogNode logger = mock(ILogNode.class);
	QueueConfiguration configuration = mock(QueueConfiguration.class);
	QueueThreadPoolFactory threadPoolFactory = mock(QueueThreadPoolFactory.class);
	QueueThreadFactory threadFactory = mock(QueueThreadFactory.class);
	QueueValidator queueValidator = mock(QueueValidator.class);
	QueueRepository queueRepository = mock(QueueRepository.class);

	@Test
	public void initializeIsValid() {
		QueueInitializer queueInitializer = new QueueInitializer();
		String name = "TestQueue";
		int poolSize = 1;
		int priority = 5;
		when(queueValidator.isValid(queueRepository, name, poolSize, priority)).thenReturn(true);
		when(configuration.getName()).thenReturn(name);
		when(configuration.getCorePoolSize()).thenReturn(poolSize);
		when(configuration.getPriority()).thenReturn(priority);
		boolean actualResult = queueInitializer.initialize(logger, configuration, threadPoolFactory, threadFactory, queueValidator, queueRepository);
		assertTrue(actualResult);
		verify(logger, times(1)).info("Queue " + configuration.getName() + " has been initialized with " + configuration.getCorePoolSize() + " threads and priority " + configuration.getPriority() + ".");
	}
	
	@Test
	public void initializeIsNotValid() {
		QueueInitializer queueInitializer = new QueueInitializer();
		String name = "TestQueue";
		int poolSize = 1;
		int priority = 5;
		when(queueValidator.isValid(queueRepository, name, poolSize, priority)).thenReturn(false);
		when(configuration.getName()).thenReturn(name);
		when(configuration.getCorePoolSize()).thenReturn(poolSize);
		when(configuration.getPriority()).thenReturn(priority);
		boolean actualResult = queueInitializer.initialize(logger, configuration, threadPoolFactory, threadFactory, queueValidator, queueRepository);
		assertFalse(actualResult);
		verify(logger, times(1)).error("QueueValidator returned false. Queue "+ configuration.getName() + " will not be initialized.");
	}

}
