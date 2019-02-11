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
import queue.repositories.ConstantsRepository;
import queue.repositories.QueueRepository;
import queue.utilities.CoreUtility;

public class TestQueueInitializer {
	
	ILogNode logger = mock(ILogNode.class);
	QueueConfiguration configuration = mock(QueueConfiguration.class);
	QueueThreadPoolFactory threadPoolFactory = mock(QueueThreadPoolFactory.class);
	QueueThreadFactory threadFactory = mock(QueueThreadFactory.class);
	QueueValidator queueValidator = mock(QueueValidator.class);
	QueueRepository queueRepository = mock(QueueRepository.class);
	ConstantsRepository constantsRepository = mock(ConstantsRepository.class);
	CoreUtility coreUtility = mock(CoreUtility.class);

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
		when(constantsRepository.isClusterSupport()).thenReturn(false);
		when(coreUtility.getInstanceIndex()).thenReturn(-1L);
		boolean actualResult = queueInitializer.initialize(logger, configuration, threadPoolFactory, threadFactory, queueValidator, queueRepository, coreUtility, constantsRepository);
		assertTrue(actualResult);
		verify(logger, times(1)).info("Queue " + configuration.getName() + " has been initialized with " + configuration.getCorePoolSize() + " threads and priority " + configuration.getPriority() + ".");
		verify(logger, times(0)).warn("Your Mendix application runs on multiple instances. Please consider to set CLUSTER_SUPPORT to true.");
	}
	
	@Test
	public void initializeIsValidWarningClusterSupportEnable() {
		QueueInitializer queueInitializer = new QueueInitializer();
		String name = "TestQueue";
		int poolSize = 1;
		int priority = 5;
		when(queueValidator.isValid(queueRepository, name, poolSize, priority)).thenReturn(true);
		when(configuration.getName()).thenReturn(name);
		when(configuration.getCorePoolSize()).thenReturn(poolSize);
		when(configuration.getPriority()).thenReturn(priority);
		when(constantsRepository.isClusterSupport()).thenReturn(false);
		when(coreUtility.getInstanceIndex()).thenReturn(1L);
		boolean actualResult = queueInitializer.initialize(logger, configuration, threadPoolFactory, threadFactory, queueValidator, queueRepository, coreUtility, constantsRepository);
		assertTrue(actualResult);
		verify(logger, times(1)).info("Queue " + configuration.getName() + " has been initialized with " + configuration.getCorePoolSize() + " threads and priority " + configuration.getPriority() + ".");
		verify(logger, times(1)).warn("Your Mendix application runs on multiple instances. Please consider to set CLUSTER_SUPPORT to true.");
	}
	
	@Test
	public void initializeIsValidWarningCouldNotDetectInstanceIndex() {
		QueueInitializer queueInitializer = new QueueInitializer();
		String name = "TestQueue";
		int poolSize = 1;
		int priority = 5;
		when(queueValidator.isValid(queueRepository, name, poolSize, priority)).thenReturn(true);
		when(configuration.getName()).thenReturn(name);
		when(configuration.getCorePoolSize()).thenReturn(poolSize);
		when(configuration.getPriority()).thenReturn(priority);
		when(constantsRepository.isClusterSupport()).thenReturn(true);
		when(coreUtility.getInstanceIndex()).thenReturn(-1L);
		boolean actualResult = queueInitializer.initialize(logger, configuration, threadPoolFactory, threadFactory, queueValidator, queueRepository, coreUtility, constantsRepository);
		assertTrue(actualResult);
		verify(logger, times(1)).info("Queue " + configuration.getName() + " has been initialized with " + configuration.getCorePoolSize() + " threads and priority " + configuration.getPriority() + ".");
		verify(logger, times(1)).warn("Could not detect instance index. Please consider to disable CLUSTER_SUPPORT.");
	}
	
	@Test
	public void initializeIsValidClusterSupportEnabled() {
		QueueInitializer queueInitializer = new QueueInitializer();
		String name = "TestQueue";
		int poolSize = 1;
		int priority = 5;
		when(queueValidator.isValid(queueRepository, name, poolSize, priority)).thenReturn(true);
		when(configuration.getName()).thenReturn(name);
		when(configuration.getCorePoolSize()).thenReturn(poolSize);
		when(configuration.getPriority()).thenReturn(priority);
		when(constantsRepository.isClusterSupport()).thenReturn(true);
		when(coreUtility.getInstanceIndex()).thenReturn(1L);
		boolean actualResult = queueInitializer.initialize(logger, configuration, threadPoolFactory, threadFactory, queueValidator, queueRepository, coreUtility, constantsRepository);
		assertTrue(actualResult);
		verify(logger, times(1)).info("Queue " + configuration.getName() + " has been initialized with " + configuration.getCorePoolSize() + " threads and priority " + configuration.getPriority() + ".");
		verify(logger, times(0)).warn("Could not detect instance index. Please consider to disable CLUSTER_SUPPORT.");
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
		boolean actualResult = queueInitializer.initialize(logger, configuration, threadPoolFactory, threadFactory, queueValidator, queueRepository, coreUtility, constantsRepository);
		assertFalse(actualResult);
		verify(logger, times(1)).error("QueueValidator returned false. Queue "+ configuration.getName() + " will not be initialized.");
	}

}
