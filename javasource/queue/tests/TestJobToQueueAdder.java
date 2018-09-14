package queue.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import queue.helpers.JobValidator;
import queue.helpers.TimeUnitConverter;
import queue.proxies.ENU_JobStatus;
import queue.proxies.ENU_TimeUnit;
import queue.proxies.Job;
import queue.repositories.ConstantsRepository;
import queue.repositories.JobRepository;
import queue.repositories.QueueRepository;
import queue.repositories.ScheduledJobRepository;
import queue.usecases.QueueHandler;

public class TestJobToQueueAdder {

	JobValidator jobValidator = mock(JobValidator.class);
	ExponentialBackoffCalculator exponentialBackoffCalculator = mock(ExponentialBackoffCalculator.class);
	TimeUnitConverter timeUnitConverter = mock(TimeUnitConverter.class);
	IContext context = mock(IContext.class);
	ILogNode logger = mock(ILogNode.class);
	QueueRepository queueRepository = mock(QueueRepository.class);
	JobRepository jobRepository = mock(JobRepository.class);
	ScheduledJobRepository scheduledJobRepository = mock(ScheduledJobRepository.class);
	Job job = mock(Job.class);
	ScheduledExecutorService queue = mock(ScheduledExecutorService.class);
	@SuppressWarnings("rawtypes")
	ScheduledFuture future = mock(ScheduledFuture.class);
	IMendixObject jobObject = mock(IMendixObject.class);
	IMendixIdentifier jobIdentifier = mock(IMendixIdentifier.class);
	QueueHandler queueHandler = mock(QueueHandler.class);
	ConstantsRepository constantsRepository = mock(ConstantsRepository.class);
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@SuppressWarnings({ "unchecked" })
	@Test
	public void addJob() throws CoreException {
		JobToQueueAdder jobToQueueAdder = new JobToQueueAdder(jobValidator, exponentialBackoffCalculator, timeUnitConverter, constantsRepository);
		String name = "NewQueue";
		int currentDelay = 500;
		
		when(jobValidator.isValid(context, queueRepository, job)).thenReturn(true);
		when(job.getQueue(context)).thenReturn(name);
		when(queueRepository.getQueue(name)).thenReturn(queue);
		when(queue.isShutdown()).thenReturn(false);
		when(queue.isTerminated()).thenReturn(false);
		when(job.getMendixObject()).thenReturn(jobObject);
		when(jobObject.getId()).thenReturn(jobIdentifier);
		when(queueRepository.getQueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, jobIdentifier)).thenReturn(queueHandler);
		when(job.getCurrentDelay(context)).thenReturn(currentDelay);
		when(job.getDelayUnit(context)).thenReturn(ENU_TimeUnit.Milliseconds);
		when(timeUnitConverter.getTimeUnit("Milliseconds")).thenReturn(TimeUnit.MILLISECONDS);
		when(queue.schedule(queueHandler, currentDelay, TimeUnit.MILLISECONDS)).thenReturn(future);
		
		jobToQueueAdder.add(context, logger, queueRepository, jobRepository, scheduledJobRepository, job);
		verify(jobValidator, times(1)).isValid(context, queueRepository, job);
		verify(job, times(1)).getQueue(context);
		verify(job, times(1)).setStatus(context, ENU_JobStatus.Queued);
		verify(job, times(1)).commit(context);
		verify(queue, times(1)).schedule(queueHandler, currentDelay, TimeUnit.MILLISECONDS);
		verify(job, times(1)).getCurrentDelay(context);
		verify(job, times(1)).getDelayUnit(context);
		verify(job, times(1)).getMendixObject();
	}
	
	@SuppressWarnings({ "unchecked" })
	@Test
	public void addJobNotValid() throws CoreException {
		JobToQueueAdder jobToQueueAdder = new JobToQueueAdder(jobValidator, exponentialBackoffCalculator, timeUnitConverter, constantsRepository);
		String name = "NewQueue";
		int currentDelay = 500;
		
		when(jobValidator.isValid(context, queueRepository, job)).thenReturn(false);
		when(job.getQueue(context)).thenReturn(name);
		when(queueRepository.getQueue(name)).thenReturn(queue);
		when(queue.isShutdown()).thenReturn(false);
		when(queue.isTerminated()).thenReturn(false);
		when(job.getMendixObject()).thenReturn(jobObject);
		when(jobObject.getId()).thenReturn(jobIdentifier);
		when(queueRepository.getQueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, jobIdentifier)).thenReturn(queueHandler);
		when(job.getCurrentDelay(context)).thenReturn(currentDelay);
		when(job.getDelayUnit(context)).thenReturn(ENU_TimeUnit.Milliseconds);
		when(timeUnitConverter.getTimeUnit("Milliseconds")).thenReturn(TimeUnit.MILLISECONDS);
		when(queue.schedule(queueHandler, currentDelay, TimeUnit.MILLISECONDS)).thenReturn(future);
		
		expectedException.expect(CoreException.class);
		expectedException.expectMessage("Job is not added, because it could not be validated.");
		
		jobToQueueAdder.add(context, logger, queueRepository, jobRepository, scheduledJobRepository, job);
	}
	
	@SuppressWarnings({ "unchecked" })
	@Test
	public void addJobExecutorNull() throws CoreException {
		JobToQueueAdder jobToQueueAdder = new JobToQueueAdder(jobValidator, exponentialBackoffCalculator, timeUnitConverter, constantsRepository);
		String name = "NewQueue";
		int currentDelay = 500;
		
		when(jobValidator.isValid(context, queueRepository, job)).thenReturn(true);
		when(job.getQueue(context)).thenReturn(name);
		when(queueRepository.getQueue(name)).thenReturn(null);
		when(queue.isShutdown()).thenReturn(false);
		when(queue.isTerminated()).thenReturn(false);
		when(job.getMendixObject()).thenReturn(jobObject);
		when(jobObject.getId()).thenReturn(jobIdentifier);
		when(queueRepository.getQueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, jobIdentifier)).thenReturn(queueHandler);
		when(job.getCurrentDelay(context)).thenReturn(currentDelay);
		when(job.getDelayUnit(context)).thenReturn(ENU_TimeUnit.Milliseconds);
		when(timeUnitConverter.getTimeUnit("Milliseconds")).thenReturn(TimeUnit.MILLISECONDS);
		when(queue.schedule(queueHandler, currentDelay, TimeUnit.MILLISECONDS)).thenReturn(future);
		
		expectedException.expect(CoreException.class);
		expectedException.expectMessage("Queue with name " + job.getQueue(context) + " could not be found. Job has not been added.");
		
		jobToQueueAdder.add(context, logger, queueRepository, jobRepository, scheduledJobRepository, job);
	}
	
	@SuppressWarnings({ "unchecked" })
	@Test
	public void addJobExecutorShutdown() throws CoreException {
		JobToQueueAdder jobToQueueAdder = new JobToQueueAdder(jobValidator, exponentialBackoffCalculator, timeUnitConverter, constantsRepository);
		String name = "NewQueue";
		int currentDelay = 500;
		
		when(jobValidator.isValid(context, queueRepository, job)).thenReturn(true);
		when(job.getQueue(context)).thenReturn(name);
		when(queueRepository.getQueue(name)).thenReturn(queue);
		when(queue.isShutdown()).thenReturn(true);
		when(queue.isTerminated()).thenReturn(false);
		when(job.getMendixObject()).thenReturn(jobObject);
		when(jobObject.getId()).thenReturn(jobIdentifier);
		when(queueRepository.getQueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, jobIdentifier)).thenReturn(queueHandler);
		when(job.getCurrentDelay(context)).thenReturn(currentDelay);
		when(job.getDelayUnit(context)).thenReturn(ENU_TimeUnit.Milliseconds);
		when(timeUnitConverter.getTimeUnit("Milliseconds")).thenReturn(TimeUnit.MILLISECONDS);
		when(queue.schedule(queueHandler, currentDelay, TimeUnit.MILLISECONDS)).thenReturn(future);
		
		expectedException.expect(CoreException.class);
		expectedException.expectMessage("Queue with name " + job.getQueue(context) + " has already been shut down or terminated. Job has not been added.");
		
		jobToQueueAdder.add(context, logger, queueRepository, jobRepository, scheduledJobRepository, job);
	}
	
	@SuppressWarnings({ "unchecked" })
	@Test
	public void addJobExecutorTerminated() throws CoreException {
		JobToQueueAdder jobToQueueAdder = new JobToQueueAdder(jobValidator, exponentialBackoffCalculator, timeUnitConverter, constantsRepository);
		String name = "NewQueue";
		int currentDelay = 500;
		
		when(jobValidator.isValid(context, queueRepository, job)).thenReturn(true);
		when(job.getQueue(context)).thenReturn(name);
		when(queueRepository.getQueue(name)).thenReturn(queue);
		when(queue.isShutdown()).thenReturn(false);
		when(queue.isTerminated()).thenReturn(true);
		when(job.getMendixObject()).thenReturn(jobObject);
		when(jobObject.getId()).thenReturn(jobIdentifier);
		when(queueRepository.getQueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, jobIdentifier)).thenReturn(queueHandler);
		when(job.getCurrentDelay(context)).thenReturn(currentDelay);
		when(job.getDelayUnit(context)).thenReturn(ENU_TimeUnit.Milliseconds);
		when(timeUnitConverter.getTimeUnit("Milliseconds")).thenReturn(TimeUnit.MILLISECONDS);
		when(queue.schedule(queueHandler, currentDelay, TimeUnit.MILLISECONDS)).thenReturn(future);
		
		expectedException.expect(CoreException.class);
		expectedException.expectMessage("Queue with name " + job.getQueue(context) + " has already been shut down or terminated. Job has not been added.");
		
		jobToQueueAdder.add(context, logger, queueRepository, jobRepository, scheduledJobRepository, job);
	}
	
	@SuppressWarnings({ "unchecked" })
	@Test
	public void addJobCommitException() throws CoreException {
		JobToQueueAdder jobToQueueAdder = new JobToQueueAdder(jobValidator, exponentialBackoffCalculator, timeUnitConverter, constantsRepository);
		String name = "NewQueue";
		int currentDelay = 500;
		
		when(jobValidator.isValid(context, queueRepository, job)).thenReturn(true);
		when(job.getQueue(context)).thenReturn(name);
		when(queueRepository.getQueue(name)).thenReturn(queue);
		when(queue.isShutdown()).thenReturn(false);
		when(queue.isTerminated()).thenReturn(false);
		when(job.getMendixObject()).thenReturn(jobObject);
		when(jobObject.getId()).thenReturn(jobIdentifier);
		when(queueRepository.getQueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, jobIdentifier)).thenReturn(queueHandler);
		when(job.getCurrentDelay(context)).thenReturn(currentDelay);
		when(job.getDelayUnit(context)).thenReturn(ENU_TimeUnit.Milliseconds);
		when(timeUnitConverter.getTimeUnit("Milliseconds")).thenReturn(TimeUnit.MILLISECONDS);
		when(queue.schedule(queueHandler, currentDelay, TimeUnit.MILLISECONDS)).thenReturn(future);
		doThrow(new CoreException()).when(job).commit(context);
		
		expectedException.expect(CoreException.class);
		expectedException.expectMessage("Could not commit job.");
		
		jobToQueueAdder.add(context, logger, queueRepository, jobRepository, scheduledJobRepository, job);
	}
	
	@SuppressWarnings({ "unchecked" })
	@Test
	public void addJobRetry() throws CoreException {
		JobToQueueAdder jobToQueueAdder = new JobToQueueAdder(jobValidator, exponentialBackoffCalculator, timeUnitConverter, constantsRepository);
		String name = "NewQueue";
		int currentDelay = 500;
		int newDelay = 1000;
		int baseDelay = 200;
		int retry = 2;
		
		when(jobValidator.isValid(context, queueRepository, job)).thenReturn(true);
		when(job.getQueue(context)).thenReturn(name);
		when(queueRepository.getQueue(name)).thenReturn(queue);
		when(queue.isShutdown()).thenReturn(false);
		when(queue.isTerminated()).thenReturn(false);
		when(job.getMendixObject()).thenReturn(jobObject);
		when(jobObject.getId()).thenReturn(jobIdentifier);
		when(queueRepository.getQueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, jobIdentifier)).thenReturn(queueHandler);
		when(job.getCurrentDelay(context)).thenReturn(currentDelay);
		when(job.getDelayUnit(context)).thenReturn(ENU_TimeUnit.Milliseconds);
		when(timeUnitConverter.getTimeUnit("Milliseconds")).thenReturn(TimeUnit.MILLISECONDS);
		when(queue.schedule(queueHandler, currentDelay, TimeUnit.MILLISECONDS)).thenReturn(future);
		when(job.getBaseDelay(context)).thenReturn(baseDelay);
		when(job.getRetry(context)).thenReturn(retry);
		when(exponentialBackoffCalculator.calculate(baseDelay, retry)).thenReturn(newDelay);
		
		
		jobToQueueAdder.addRetry(context, logger, queueRepository, jobRepository, scheduledJobRepository, job);
		verify(jobValidator, times(1)).isValid(context, queueRepository, job);
		verify(job, times(1)).getQueue(context);
		verify(job, times(1)).setStatus(context, ENU_JobStatus.Queued);
		verify(job, times(1)).commit(context);
		verify(queue, times(1)).schedule(queueHandler, currentDelay, TimeUnit.MILLISECONDS);
		verify(job, times(1)).getCurrentDelay(context);
		verify(job, times(1)).getDelayUnit(context);
		verify(job, times(1)).getMendixObject();
		verify(job, times(1)).getBaseDelay(context);
		verify(job, times(1)).getRetry(context);
		verify(job, times(1)).setCurrentDelay(context, newDelay);
		verify(job, times(1)).setRetry(context, retry + 1);
		verify(exponentialBackoffCalculator, times(1)).calculate(job.getBaseDelay(context), job.getRetry(context));
	}
	
	@Test
	public void getExponentialBackoffCalculator() throws CoreException {
		JobToQueueAdder jobToQueueAdder = new JobToQueueAdder(jobValidator, exponentialBackoffCalculator, timeUnitConverter, constantsRepository);
		
		assertEquals(exponentialBackoffCalculator, jobToQueueAdder.getExponentialBackoffCalculator());
	}

}
