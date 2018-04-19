package queue.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ScheduledExecutorService;

import org.junit.Test;

import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;

import queue.helpers.ExponentialBackoffCalculator;
import queue.helpers.JobValidator;
import queue.helpers.TimeUnitConverter;
import queue.proxies.Job;
import queue.repositories.QueueRepository;
import queue.repositories.ScheduledJobRepository;

public class TestJobToQueueAdder {

	JobValidator jobValidator = mock(JobValidator.class);
	ExponentialBackoffCalculator exponentialBackoffCalculator = mock(ExponentialBackoffCalculator.class);
	TimeUnitConverter timeUnitConverter = mock(TimeUnitConverter.class);
	IContext context = mock(IContext.class);
	ILogNode logger = mock(ILogNode.class);
	QueueRepository queueRepository = mock(QueueRepository.class);
	ScheduledJobRepository ScheduledJobRepository = mock(ScheduledJobRepository.class);
	Job job = mock(Job.class);
	ScheduledExecutorService queue = mock(ScheduledExecutorService.class);
	
	@Test
	public void addJob() {
		fail("Not yet implemented");
	}

}
