package queue.tests;

import static org.mockito.Mockito.*;

import java.util.HashMap;

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
import queue.proxies.ENU_JobStatus;
import queue.proxies.Job;
import queue.repositories.JobRepository;
import queue.repositories.MicroflowRepository;
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
	@SuppressWarnings("rawtypes")
	HashMap jobInput = mock(HashMap.class);
	CoreException e = mock(CoreException.class);
	CoreException t = mock(CoreException.class);
	MicroflowRepository microflowRepository = mock(MicroflowRepository.class);
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@SuppressWarnings("unchecked")
	@Test
	public void run() throws CoreException, InterruptedException {
		int retry = 1;
		String microflowName = "Microflow";
		
		QueueHandler queueHandler = new QueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, microflowRepository, jobId);
		
		when(queueRepository.getSystemContext()).thenReturn(context);
		when(jobRepository.getJob(context, jobId)).thenReturn(jobObject);
		when(jobToQueueAdder.getExponentialBackoffCalculator()).thenReturn(exponentialBackoffCalculator);
		when(exponentialBackoffCalculator.calculate(200, 0)).thenReturn(0);
		when(jobRepository.initialize(context, jobObject)).thenReturn(job);
		when(job.getRetry(context)).thenReturn(retry);
		when(microflowRepository.getJobInput(jobObject, microflowName)).thenReturn(jobInput);
		when(job.getMicroflowName(context)).thenReturn(microflowName);
		
		queueHandler.run();
		
		verify(jobToQueueAdder, times(1)).setTimeZone(context, logger);
		verify(jobRepository, times(1)).getJob(context, jobId);
		verify(jobRepository, times(1)).initialize(context, jobObject);
		verify(job, times(1)).setStatus(context, ENU_JobStatus.Running);
		verify(job, times(1)).setStatus(context, ENU_JobStatus.Done);
		verify(job, times(2)).commit(context);
		verify(jobRepository, times(0)).sleep(anyLong());
		verify(job, times(1)).getRetry(context);
		verify(job, times(1)).getMicroflowName(context);
		verify(microflowRepository, times(1)).getJobInput(jobObject, microflowName);
		verify(jobRepository, times(1)).executeJob(context, microflowName, true, jobInput);
		verify(scheduledJobRepository, times(1)).remove(context, jobObject, retry);
		verify(logger, times(1)).debug("Job object found.");
		verify(logger, times(1)).debug("Trying to retrieve job object. Attempt 1 of 10.");
		verify(logger, times(1)).debug("Job status set to Running.");
		verify(logger, times(1)).debug("Job status set to Done.");
		verify(logger, times(1)).debug("Starting execution of microflow " + job.getMicroflowName(context) + ".");
		verify(logger, times(1)).debug("Finished execution of microflow " + job.getMicroflowName(context) + ".");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void runRetrieveRetry() throws CoreException, InterruptedException {
		int retry = 1;
		String microflowName = "Microflow";
		
		QueueHandler queueHandler = new QueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, microflowRepository, jobId);
		
		when(queueRepository.getSystemContext()).thenReturn(context);
		when(jobRepository.getJob(context, jobId)).thenReturn(null).thenReturn(jobObject);
		when(jobToQueueAdder.getExponentialBackoffCalculator()).thenReturn(exponentialBackoffCalculator);
		when(exponentialBackoffCalculator.calculate(200, 0)).thenReturn(0);
		when(jobRepository.initialize(context, jobObject)).thenReturn(job);
		when(job.getRetry(context)).thenReturn(retry);
		when(microflowRepository.getJobInput(jobObject, microflowName)).thenReturn(jobInput);
		when(job.getMicroflowName(context)).thenReturn(microflowName);
		
		queueHandler.run();
		
		verify(jobToQueueAdder, times(1)).setTimeZone(context, logger);
		verify(jobRepository, times(2)).getJob(context, jobId);
		verify(jobRepository, times(1)).initialize(context, jobObject);
		verify(job, times(1)).setStatus(context, ENU_JobStatus.Running);
		verify(job, times(1)).setStatus(context, ENU_JobStatus.Done);
		verify(job, times(2)).commit(context);
		verify(jobRepository, times(1)).sleep(anyLong());
		verify(job, times(1)).getRetry(context);
		verify(microflowRepository, times(1)).getJobInput(jobObject, microflowName);
		verify(jobRepository, times(1)).executeJob(context, microflowName, true, jobInput);
		verify(scheduledJobRepository, times(1)).remove(context, jobObject, retry);
		verify(logger, times(1)).debug("Job object found.");
		verify(logger, times(1)).debug("Job object not found.");
		verify(logger, times(1)).debug("Trying to retrieve job object. Attempt 1 of 10.");
		verify(logger, times(1)).debug("Trying to retrieve job object. Attempt 2 of 10.");
		verify(logger, times(1)).debug("Job status set to Running.");
		verify(logger, times(1)).debug("Job status set to Done.");
		verify(logger, times(1)).debug("Starting execution of microflow " + job.getMicroflowName(context) + ".");
		verify(logger, times(1)).debug("Finished execution of microflow " + job.getMicroflowName(context) + ".");
	}
	
	@Test
	public void runJobNotFound() throws CoreException {		
		QueueHandler queueHandler = new QueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, microflowRepository, jobId);
		
		when(queueRepository.getSystemContext()).thenReturn(context);
		when(jobRepository.getJob(context, jobId)).thenReturn(null);
		when(jobToQueueAdder.getExponentialBackoffCalculator()).thenReturn(exponentialBackoffCalculator);
		when(exponentialBackoffCalculator.calculate(200, 0)).thenReturn(0);
		
		queueHandler.run();
		verify(jobToQueueAdder, times(1)).setTimeZone(context, logger);
		verify(logger, times(10)).debug("Job object not found.");
		verify(logger, times(1)).error("Could not retrieve job object. Job will not be executed.");
		verify(logger, times(1)).debug("Trying to retrieve job object. Attempt 1 of 10.");
		verify(logger, times(1)).debug("Trying to retrieve job object. Attempt 2 of 10.");
		verify(logger, times(1)).debug("Trying to retrieve job object. Attempt 3 of 10.");
		verify(logger, times(1)).debug("Trying to retrieve job object. Attempt 4 of 10.");
		verify(logger, times(1)).debug("Trying to retrieve job object. Attempt 5 of 10.");
		verify(logger, times(1)).debug("Trying to retrieve job object. Attempt 6 of 10.");
		verify(logger, times(1)).debug("Trying to retrieve job object. Attempt 7 of 10.");
		verify(logger, times(1)).debug("Trying to retrieve job object. Attempt 8 of 10.");
		verify(logger, times(1)).debug("Trying to retrieve job object. Attempt 9 of 10.");
		verify(logger, times(1)).debug("Trying to retrieve job object. Attempt 10 of 10.");
	}

	@Test
	public void runThreadInterupted() throws CoreException, InterruptedException {
		int retry = 1;
		
		QueueHandler queueHandler = new QueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, microflowRepository, jobId);
		
		when(queueRepository.getSystemContext()).thenReturn(context);
		when(jobRepository.getJob(context, jobId)).thenReturn(null).thenReturn(jobObject);
		when(jobToQueueAdder.getExponentialBackoffCalculator()).thenReturn(exponentialBackoffCalculator);
		when(exponentialBackoffCalculator.calculate(200, 0)).thenReturn(0);
		when(jobRepository.initialize(context, jobObject)).thenReturn(job);
		doThrow(InterruptedException.class).when(jobRepository).sleep(0);
		when(job.getRetry(context)).thenReturn(retry);
		
		queueHandler.run();
		verify(logger, times(1)).error("While executing job, could bring Thread to sleep when retrieving job object.");		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void runExceptionWhileExecutingMicroflow() throws CoreException, InterruptedException {
		int retry = 1;
		String microflowName = "MicroflowToRun";
		
		QueueHandler queueHandler = new QueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, microflowRepository, jobId);
		
		when(queueRepository.getSystemContext()).thenReturn(context);
		when(jobRepository.getJob(context, jobId)).thenReturn(jobObject);
		when(jobToQueueAdder.getExponentialBackoffCalculator()).thenReturn(exponentialBackoffCalculator);
		when(exponentialBackoffCalculator.calculate(200, 0)).thenReturn(0);
		when(jobRepository.initialize(context, jobObject)).thenReturn(job);
		when(job.getRetry(context)).thenReturn(retry);
		when(microflowRepository.getJobInput(jobObject, microflowName)).thenReturn(jobInput);
		when(job.getMicroflowName(context)).thenReturn(microflowName);
		doThrow(e).when(jobRepository).executeJob(context, microflowName, true, jobInput);
		when(e.getCause()).thenReturn(t);
		when(t.getCause()).thenReturn(null);
		
		
		queueHandler.run();
		
		verify(jobRepository, times(1)).getJob(context, jobId);
		verify(jobRepository, times(1)).initialize(context, jobObject);
		verify(job, times(1)).setStatus(context, ENU_JobStatus.Running);
		verify(job, times(1)).setStatus(context, ENU_JobStatus.Error);
		verify(job, times(2)).commit(context);
		verify(jobRepository, times(0)).sleep(anyLong());
		verify(job, times(1)).getRetry(context);
		verify(microflowRepository, times(1)).getJobInput(jobObject, microflowName);
		verify(jobRepository, times(1)).executeJob(context, microflowName, true, jobInput);
		verify(scheduledJobRepository, times(1)).remove(context, jobObject, retry);
		verify(logger, times(1)).debug("Job object found.");
		verify(logger, times(1)).debug("Trying to retrieve job object. Attempt 1 of 10.");
		verify(logger, times(1)).debug("Job status set to Running.");
		verify(logger, times(0)).debug("Job status set to Done.");
		verify(logger, times(1)).debug("Starting execution of microflow " + job.getMicroflowName(context) + ".");
		verify(logger, times(0)).debug("Finished execution of microflow " + job.getMicroflowName(context) + ".");
		verify(logger, times(1)).debug("Max retries reached, status is set to Error.");
		verify(logger, times(1)).error("Job " + job.getIdJob(context) + ": Error during execution of microflow " + microflowName + ".", e);
		verify(e, times(1)).getCause();
		verify(t, times(1)).getCause();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void runAnotherExceptionWhileExecutingMicroflow() throws CoreException, InterruptedException {
		int retry = 1;
		String microflowName = "MicroflowToRun";
		
		QueueHandler queueHandler = new QueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, microflowRepository, jobId);
		
		when(queueRepository.getSystemContext()).thenReturn(context);
		when(jobRepository.getJob(context, jobId)).thenReturn(jobObject);
		when(jobToQueueAdder.getExponentialBackoffCalculator()).thenReturn(exponentialBackoffCalculator);
		when(exponentialBackoffCalculator.calculate(200, 0)).thenReturn(0);
		when(jobRepository.initialize(context, jobObject)).thenReturn(job);
		when(job.getRetry(context)).thenReturn(retry);
		when(microflowRepository.getJobInput(jobObject, microflowName)).thenReturn(jobInput);
		when(job.getMicroflowName(context)).thenReturn(microflowName);
		doThrow(e).when(jobRepository).executeJob(context, microflowName, true, jobInput);
		when(e.getCause()).thenReturn(t);
		when(t.getCause()).thenReturn(new CoreException());
		
		
		queueHandler.run();
		
		verify(jobRepository, times(1)).getJob(context, jobId);
		verify(jobRepository, times(1)).initialize(context, jobObject);
		verify(job, times(1)).setStatus(context, ENU_JobStatus.Running);
		verify(job, times(1)).setStatus(context, ENU_JobStatus.Error);
		verify(job, times(2)).commit(context);
		verify(jobRepository, times(0)).sleep(anyLong());
		verify(job, times(1)).getRetry(context);
		verify(microflowRepository, times(1)).getJobInput(jobObject, microflowName);
		verify(jobRepository, times(1)).executeJob(context, microflowName, true, jobInput);
		verify(scheduledJobRepository, times(1)).remove(context, jobObject, retry);
		verify(logger, times(1)).debug("Job object found.");
		verify(logger, times(1)).debug("Trying to retrieve job object. Attempt 1 of 10.");
		verify(logger, times(1)).debug("Job status set to Running.");
		verify(logger, times(0)).debug("Job status set to Done.");
		verify(logger, times(1)).debug("Starting execution of microflow " + job.getMicroflowName(context) + ".");
		verify(logger, times(0)).debug("Finished execution of microflow " + job.getMicroflowName(context) + ".");
		verify(logger, times(1)).debug("Max retries reached, status is set to Error.");
		verify(logger, times(1)).error("Job " + job.getIdJob(context) + ": Error during execution of microflow " + microflowName + ".", e);
		verify(e, times(1)).getCause();
		verify(t, times(1)).getCause();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void runInterruptedWhileExecutingMicroflow() throws CoreException, InterruptedException {
		int retry = 1;
		String microflowName = "MicroflowToRun";
		
		QueueHandler queueHandler = new QueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, microflowRepository, jobId);
		
		when(queueRepository.getSystemContext()).thenReturn(context);
		when(jobRepository.getJob(context, jobId)).thenReturn(jobObject);
		when(jobToQueueAdder.getExponentialBackoffCalculator()).thenReturn(exponentialBackoffCalculator);
		when(exponentialBackoffCalculator.calculate(200, 0)).thenReturn(0);
		when(jobRepository.initialize(context, jobObject)).thenReturn(job);
		when(job.getRetry(context)).thenReturn(retry);
		when(microflowRepository.getJobInput(jobObject, microflowName)).thenReturn(jobInput);
		when(job.getMicroflowName(context)).thenReturn(microflowName);
		doThrow(e).when(jobRepository).executeJob(context, microflowName, true, jobInput);
		when(e.getCause()).thenReturn(t);
		when(t.getCause()).thenReturn(new InterruptedException()).thenReturn(null);
		
		
		queueHandler.run();
		verify(jobRepository, times(1)).getJob(context, jobId);
		verify(jobRepository, times(1)).initialize(context, jobObject);
		verify(job, times(1)).setStatus(context, ENU_JobStatus.Running);
		verify(job, times(1)).setStatus(context, ENU_JobStatus.Cancelled);
		verify(job, times(2)).commit(context);
		verify(jobRepository, times(0)).sleep(anyLong());
		verify(job, times(1)).getRetry(context);
		verify(microflowRepository, times(1)).getJobInput(jobObject, microflowName);
		verify(jobRepository, times(1)).executeJob(context, microflowName, true, jobInput);
		verify(scheduledJobRepository, times(1)).remove(context, jobObject, retry);
		verify(logger, times(1)).debug("Job object found.");
		verify(logger, times(1)).debug("Trying to retrieve job object. Attempt 1 of 10.");
		verify(logger, times(1)).debug("Job status set to Running.");
		verify(logger, times(0)).debug("Job status set to Done.");
		verify(logger, times(1)).debug("Starting execution of microflow " + job.getMicroflowName(context) + ".");
		verify(logger, times(0)).debug("Finished execution of microflow " + job.getMicroflowName(context) + ".");
		verify(logger, times(1)).warn("Microflow " + microflowName + " has been interrupted. Status will be set to Cancelled.");
		verify(e, times(1)).getCause();
		verify(t, times(1)).getCause();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void runExceptionWhileExecutingMicroflowRetry() throws CoreException, InterruptedException {
		int retry = 1;
		int maxRetries = 2;
		String microflowName = "MicroflowToRun";
		
		QueueHandler queueHandler = new QueueHandler(logger, jobToQueueAdder, scheduledJobRepository, queueRepository, jobRepository, microflowRepository, jobId);
		
		when(queueRepository.getSystemContext()).thenReturn(context);
		when(jobRepository.getJob(context, jobId)).thenReturn(jobObject);
		when(jobToQueueAdder.getExponentialBackoffCalculator()).thenReturn(exponentialBackoffCalculator);
		when(exponentialBackoffCalculator.calculate(200, 0)).thenReturn(0);
		when(jobRepository.initialize(context, jobObject)).thenReturn(job);
		when(job.getRetry(context)).thenReturn(retry);
		when(job.getMaxRetries(context)).thenReturn(maxRetries);
		when(microflowRepository.getJobInput(jobObject, microflowName)).thenReturn(jobInput);
		when(job.getMicroflowName(context)).thenReturn(microflowName);
		doThrow(e).when(jobRepository).executeJob(context, microflowName, true, jobInput);
		when(e.getCause()).thenReturn(t);
		when(t.getCause()).thenReturn(new CoreException()).thenReturn(null);
		
		queueHandler.run();
		verify(jobRepository, times(1)).getJob(context, jobId);
		verify(jobRepository, times(1)).initialize(context, jobObject);
		verify(job, times(1)).setStatus(context, ENU_JobStatus.Running);
		verify(job, times(1)).commit(context);
		verify(jobRepository, times(0)).sleep(anyLong());
		verify(job, times(1)).getRetry(context);
		verify(microflowRepository, times(1)).getJobInput(jobObject, microflowName);
		verify(jobRepository, times(1)).executeJob(context, microflowName, true, jobInput);
		verify(scheduledJobRepository, times(1)).remove(context, jobObject, retry);
		verify(logger, times(1)).debug("Job object found.");
		verify(logger, times(1)).debug("Trying to retrieve job object. Attempt 1 of 10.");
		verify(logger, times(1)).debug("Job status set to Running.");
		verify(logger, times(0)).debug("Job status set to Done.");
		verify(logger, times(1)).debug("Starting execution of microflow " + job.getMicroflowName(context) + ".");
		verify(logger, times(0)).debug("Finished execution of microflow " + job.getMicroflowName(context) + ".");
		verify(logger, times(1)).error("Job " + job.getIdJob(context) + ": Error during execution of microflow " + microflowName + ".", e);
		verify(logger, times(1)).debug("Retry " + (retry + 1) + " of " + job.getMaxRetries(context) + " will be scheduled for job with microflow " + job.getMicroflowName(context) + ".");
		verify(logger, times(1)).debug("Job rescheduled and status set to Queued.");
		verify(e, times(1)).getCause();
		verify(t, times(1)).getCause();
	}
}
