package queue.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.mendix.logging.ILogNode;

import queue.helpers.QueueController;
import queue.repositories.QueueRepository;

public class TestQueueController {

	ILogNode logger = mock(ILogNode.class);
	QueueRepository queueRepository = mock(QueueRepository.class);
	ScheduledExecutorService queue = mock(ScheduledExecutorService.class);
	
	
	@Test
	public void shutdownQueueDoesNotExist() {
		String name = "NewQueue";
		QueueController queueController = new QueueController(logger);
		when(queueRepository.queueExists(name)).thenReturn(false);
		
		boolean shutdown = queueController.shutdown(queueRepository, name, true, true, 1000);
		
		assertFalse(shutdown);
		verify(queueRepository, times(1)).queueExists(name);
		verify(logger, times(1)).error("Queue with name " + name + " has not been initialized and therefore cannot be terminated.");
	}
	
	@Test
	public void shutdownQueueGracefullyAwaitTermination() throws InterruptedException {
		String name = "NewQueue";
		int terminationTimeout = 1000;
		QueueController queueController = new QueueController(logger);
		when(queueRepository.queueExists(name)).thenReturn(true);
		when(queueRepository.getQueue(name)).thenReturn(queue);
		when(queue.awaitTermination(terminationTimeout, TimeUnit.SECONDS)).thenReturn(true);
		
		boolean shutdown = queueController.shutdown(queueRepository, name, true, true, terminationTimeout);
		
		assertTrue(shutdown);
		verify(queueRepository, times(1)).queueExists(name);
		verify(queue, times(1)).shutdown();
		verify(queue, times(1)).awaitTermination(terminationTimeout, TimeUnit.SECONDS);
	}
	
	@Test
	public void shutdownQueueGracefullyAwaitTerminationReturnsFalse() throws InterruptedException {
		String name = "NewQueue";
		int terminationTimeout = 1000;
		QueueController queueController = new QueueController(logger);
		when(queueRepository.queueExists(name)).thenReturn(true);
		when(queueRepository.getQueue(name)).thenReturn(queue);
		when(queue.awaitTermination(terminationTimeout, TimeUnit.SECONDS)).thenReturn(false);
		
		boolean shutdown = queueController.shutdown(queueRepository, name, true, true, terminationTimeout);
		
		assertFalse(shutdown);
		verify(queueRepository, times(1)).queueExists(name);
		verify(queue, times(1)).shutdown();
		verify(queue, times(1)).awaitTermination(terminationTimeout, TimeUnit.SECONDS);
		verify(logger, times(1)).error("Queue could not be terminated within timeout of " + terminationTimeout + " seconds.");
	}
	
	@Test
	public void shutdownQueueGracefullyAwaitTerminationThrowsException() throws InterruptedException {
		String name = "NewQueue";
		int terminationTimeout = 1000;
		QueueController queueController = new QueueController(logger);
		when(queueRepository.queueExists(name)).thenReturn(true);
		when(queueRepository.getQueue(name)).thenReturn(queue);
		when(queue.awaitTermination(terminationTimeout, TimeUnit.SECONDS)).thenThrow(InterruptedException.class);
		
		boolean shutdown = queueController.shutdown(queueRepository, name, true, true, terminationTimeout);
		
		assertTrue(shutdown);
		verify(queueRepository, times(1)).queueExists(name);
		verify(queue, times(1)).shutdown();
		verify(queue, times(1)).awaitTermination(terminationTimeout, TimeUnit.SECONDS);
		verify(logger, times(1)).error("Queue has been shutdown unexpectedly (thread interrupted).");
	}
	
	@Test
	public void shutdownQueueGracefullyDontAwaitTermination() throws InterruptedException {
		String name = "NewQueue";
		int terminationTimeout = 1000;
		QueueController queueController = new QueueController(logger);
		when(queueRepository.queueExists(name)).thenReturn(true);
		when(queueRepository.getQueue(name)).thenReturn(queue);
		when(queue.awaitTermination(terminationTimeout, TimeUnit.SECONDS)).thenReturn(true);
		
		boolean shutdown = queueController.shutdown(queueRepository, name, true, false, terminationTimeout);
		
		assertTrue(shutdown);
		verify(queueRepository, times(1)).queueExists(name);
		verify(queue, times(1)).shutdown();
		verify(queue, times(0)).awaitTermination(terminationTimeout, TimeUnit.SECONDS);
	}
	
	@Test
	public void shutdownQueueForcedAwaitTermination() throws InterruptedException {
		String name = "NewQueue";
		int terminationTimeout = 1000;
		QueueController queueController = new QueueController(logger);
		when(queueRepository.queueExists(name)).thenReturn(true);
		when(queueRepository.getQueue(name)).thenReturn(queue);
		when(queue.awaitTermination(terminationTimeout, TimeUnit.SECONDS)).thenReturn(true);
		
		boolean shutdown = queueController.shutdown(queueRepository, name, false, true, terminationTimeout);
		
		assertTrue(shutdown);
		verify(queueRepository, times(1)).queueExists(name);
		verify(queue, times(1)).shutdownNow();
		verify(queue, times(1)).awaitTermination(terminationTimeout, TimeUnit.SECONDS);
	}
	
	@Test
	public void shutdownQueueForcedDontAwaitTermination() throws InterruptedException {
		String name = "NewQueue";
		int terminationTimeout = 1000;
		QueueController queueController = new QueueController(logger);
		when(queueRepository.queueExists(name)).thenReturn(true);
		when(queueRepository.getQueue(name)).thenReturn(queue);
		when(queue.awaitTermination(terminationTimeout, TimeUnit.SECONDS)).thenReturn(true);
		
		boolean shutdown = queueController.shutdown(queueRepository, name, false, false, terminationTimeout);
		
		assertTrue(shutdown);
		verify(queueRepository, times(1)).queueExists(name);
		verify(queue, times(1)).shutdownNow();
		verify(queue, times(0)).awaitTermination(terminationTimeout, TimeUnit.SECONDS);
	}

}
