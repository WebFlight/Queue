package queue.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.helpers.ExponentialBackoffCalculator;
import queue.helpers.JobToQueueAdder;
import queue.helpers.JobValidator;
import queue.helpers.TimeUnitConverter;
import queue.proxies.ENU_TimeUnit;
import queue.proxies.Job;
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
	ENU_TimeUnit timeUnit = mock(ENU_TimeUnit.class);
	
	@SuppressWarnings("unchecked")
	@Test
	public void addJob() throws CoreException {
		JobToQueueAdder jobToQueueAdder = new JobToQueueAdder(jobValidator, exponentialBackoffCalculator, timeUnitConverter);
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
		when(job.getDelayUnit(context)).thenReturn(timeUnit.Milliseconds);
		when(timeUnit.getCaption()).thenReturn("milliseconds");
		when(timeUnitConverter.getTimeUnit("milliseconds")).thenReturn(TimeUnit.MILLISECONDS);
		when(queue.schedule(queueHandler, currentDelay, TimeUnit.MILLISECONDS)).thenReturn(future);
		
		jobToQueueAdder.add(context, logger, queueRepository, jobRepository, scheduledJobRepository, job);
	}

}
