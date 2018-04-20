package queue.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.helpers.ExponentialBackoffCalculator;
import queue.helpers.JobToQueueAdder;
import queue.proxies.Job;
import queue.repositories.JobRepository;
import queue.repositories.QueueRepository;
import queue.repositories.ScheduledJobRepository;
import queue.usecases.QueueHandler;

public class TestQueueHandler {
	
	IMendixIdentifier jobId = mock(IMendixIdentifier.class);
	ILogNode logger = mock(ILogNode.class);
	JobToQueueAdder jobToQueueAdder = mock(JobToQueueAdder.class);
	ScheduledJobRepository scheduledJobRepository = mock(ScheduledJobRepository.class);
	QueueRepository queueRepository = mock(QueueRepository.class);
	JobRepository jobRepository = mock(JobRepository.class);
	IContext context = mock(IContext.class);
	IMendixObject jobObject = mock(IMendixObject.class);
	ExponentialBackoffCalculator exponentialBackoffCalculator = mock(ExponentialBackoffCalculator.class);
	Job job = mock(Job.class);
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void run() throws CoreException {
		int retry = 1;
		
		QueueHandler queueHandler = new QueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, jobId);
		
		when(queueRepository.getSystemContext()).thenReturn(context);
		when(jobRepository.getJob(context, jobId)).thenReturn(jobObject);
		when(jobToQueueAdder.getExponentialBackoffCalculator()).thenReturn(exponentialBackoffCalculator);
		when(exponentialBackoffCalculator.calculate(200, 0)).thenReturn(0);
		when(jobRepository.initialize(context, jobObject)).thenReturn(job);
		when(job.getRetry(context)).thenReturn(retry);
		
		queueHandler.run();
	}
	
	@Test
	public void runRetrieveRetry() throws CoreException {
		int retry = 1;
		
		QueueHandler queueHandler = new QueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, jobId);
		
		when(queueRepository.getSystemContext()).thenReturn(context);
		when(jobRepository.getJob(context, jobId)).thenReturn(null).thenReturn(jobObject);
		when(jobToQueueAdder.getExponentialBackoffCalculator()).thenReturn(exponentialBackoffCalculator);
		when(exponentialBackoffCalculator.calculate(200, 0)).thenReturn(0);
		when(jobRepository.initialize(context, jobObject)).thenReturn(job);
		when(job.getRetry(context)).thenReturn(retry);
		
		queueHandler.run();
	}
	
	@Test
	public void runJobNotFound() throws CoreException {
		int retry = 1;
		
		QueueHandler queueHandler = new QueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, jobId);
		
		when(queueRepository.getSystemContext()).thenReturn(context);
		when(jobRepository.getJob(context, jobId)).thenReturn(null);
		when(jobToQueueAdder.getExponentialBackoffCalculator()).thenReturn(exponentialBackoffCalculator);
		when(exponentialBackoffCalculator.calculate(200, 0)).thenReturn(0);
		when(jobRepository.initialize(context, jobObject)).thenReturn(job);
		when(job.getRetry(context)).thenReturn(retry);
		
		queueHandler.run();
		verify(logger, times(1)).error("Could not retrieve job object. Job will not be executed.");
	}

	@Test
	public void runThreadInterupted() throws CoreException, InterruptedException {
		int retry = 1;
		
		QueueHandler queueHandler = new QueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, jobId);
		
		when(queueRepository.getSystemContext()).thenReturn(context);
		when(jobRepository.getJob(context, jobId)).thenReturn(null);
		when(jobToQueueAdder.getExponentialBackoffCalculator()).thenReturn(exponentialBackoffCalculator);
		when(exponentialBackoffCalculator.calculate(200, 0)).thenReturn(0);
		when(jobRepository.initialize(context, jobObject)).thenReturn(job);
		doThrow(InterruptedException.class).when(jobRepository).sleep(0);
		when(job.getRetry(context)).thenReturn(retry);
		
		queueHandler.run();
		verify(logger, times(9)).error("While executing job, could bring Thread to sleep when retrieving job object.");
	}
}
