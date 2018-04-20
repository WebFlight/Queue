package queue.helpers;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import queue.proxies.ENU_JobStatus;
import queue.proxies.Job;
import queue.repositories.ScheduledJobRepository;
import queue.repositories.JobRepository;
import queue.repositories.QueueRepository;

public class JobToQueueAdder {
	
	private JobValidator jobValidator;
	private ExponentialBackoffCalculator exponentialBackoffCalculator;
	private TimeUnitConverter timeUnitConverter;
	
	public JobToQueueAdder(JobValidator jobValidator, ExponentialBackoffCalculator exponentialBackoffCalculator, TimeUnitConverter timeUnitConverter) {
		this.jobValidator = jobValidator;
		this.exponentialBackoffCalculator = exponentialBackoffCalculator;
		this.timeUnitConverter = timeUnitConverter;
	}
	
	public void add(IContext context, ILogNode logger, QueueRepository queueRepository, JobRepository jobRepository, ScheduledJobRepository scheduledJobRepository, Job job) throws CoreException {
		boolean valid = this.jobValidator.isValid(context, queueRepository, job);
		
		if (valid == false) {
			throw new CoreException("Job is not added, because it could not be validated.");
		}
		
		ScheduledExecutorService executor = queueRepository.getQueue(job.getQueue(context));
		
		if(executor == null) {
			throw new CoreException("Queue with name " + job.getQueue(context) + " could not be found. Job has not been added.");
		}
		
		if(executor.isShutdown() || executor.isTerminated()) {
			throw new CoreException("Queue with name " + job.getQueue(context) + " has already been shut down or terminated. Job has not been added.");
		}
		
		
		job.setStatus(context, ENU_JobStatus.Queued);
		
		try {
			job.commit(context);
		} catch (Exception e) {
			throw new CoreException("Could not commit job.", e);
		}
		
		IMendixObject jobObject = job.getMendixObject();
				
		ScheduledFuture<?> future =	executor.schedule(
					queueRepository.getQueueHandler(logger, this, scheduledJobRepository, queueRepository, jobRepository, jobObject.getId()), 
					job.getCurrentDelay(context), 
					timeUnitConverter.getTimeUnit(job.getDelayUnit(context).getCaption())
					);
		
		scheduledJobRepository.add(context, jobObject, future);
	}
	
	public void addRetry(IContext context, ILogNode logger, QueueRepository queueRepository, JobRepository jobRepository, ScheduledJobRepository scheduledJobRepository, Job job) throws CoreException {
		int retry = job.getRetry(context);
		int baseDelay = job.getBaseDelay(context);
		int newDelay= this.exponentialBackoffCalculator.calculate(baseDelay, retry);
		job.setCurrentDelay(context, newDelay);
		job.setRetry(context, retry + 1);
		add(context, logger, queueRepository, jobRepository, scheduledJobRepository, job);
	}
	
	public ExponentialBackoffCalculator getExponentialBackoffCalculator() {
		return this.exponentialBackoffCalculator;
	}
}