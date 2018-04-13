package queue.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;

import queue.helpers.JobValidator;
import queue.helpers.MicroflowValidator;
import queue.proxies.ENU_TimeUnit;
import queue.proxies.Job;
import queue.repositories.QueueRepository;

public class TestJobValidator {

	ILogNode logger = mock(ILogNode.class);
	MicroflowValidator microflowValidator = mock(MicroflowValidator.class);
	IContext context = mock(IContext.class);
	QueueRepository queueRepository = mock(QueueRepository.class);
	Job job = mock(Job.class);
	JobValidator jobValidator = new JobValidator(logger, microflowValidator);
	
	@Test
	public void validateTrue() {
		String validQueueName = "ValidQueueName";
		String validMicroflowName = "ValidMicroflowName";
		when(job.getQueue(context)).thenReturn(validQueueName);
		when(job.getBaseDelay(context)).thenReturn(500);
		when(job.getCurrentDelay(context)).thenReturn(0);
		when(job.getDelayUnit(context)).thenReturn(ENU_TimeUnit.Milliseconds);
		when(job.getMaxRetries(context)).thenReturn(5);
		when(job.getMicroflowName(context)).thenReturn("ValidMicroflowName");
		when(job.getRetry(context)).thenReturn(0);
		
		when(queueRepository.queueExists(validQueueName)).thenReturn(true);
		when(microflowValidator.validate(validMicroflowName)).thenReturn(true);
		
		boolean actualResult = jobValidator.isValid(context, queueRepository, job);
		assertTrue(actualResult);
		verify(job, times(1)).getQueue(context);
		verify(job, times(1)).getBaseDelay(context);
		verify(job, times(1)).getCurrentDelay(context);
		verify(job, times(1)).getDelayUnit(context);
		verify(job, times(1)).getMaxRetries(context);
		verify(job, times(1)).getMicroflowName(context);
		verify(job, times(1)).getRetry(context);
	}

}
