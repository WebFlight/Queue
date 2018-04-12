package queue.helpers;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IUser;

import queue.proxies.ENU_JobStatus;
import queue.proxies.Job;
import queue.repositories.ScheduledJobRepository;
import queue.repositories.JobRepository;
import queue.repositories.QueueRepository;

public class JobToQueueAdder {
	
	public void add(IContext context, ILogNode logger, QueueRepository queueRepository, JobRepository jobRepository, ScheduledJobRepository scheduledJobRepository, JobValidator jobValidator, Job job, boolean runFromUser, IUser user) throws CoreException {
		boolean valid = jobValidator.isValid(context, queueRepository, job);
		
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
			throw new CoreException("Could not commit job.");
		}
		
		if (user == null) {
			if(runFromUser) {
				if(context.getSession().getUser(context) != null) {
					logger.debug("Run from user enabled. User will be added to queue handler.");
					user = context.getSession().getUser(context);
					logger.debug("User " + user.getName() + " added to queue handler.");
				}
				if(context.getSession().getUser(context) == null) {
					logger.warn("Job is added by System user. RunFromUser will be disabled.");
				}
			}
		}
				
		ScheduledFuture<?> future =	executor.schedule(
					queueRepository.getQueueHandler(logger, user, jobValidator, this, scheduledJobRepository, queueRepository, jobRepository, job.getMendixObject().getId()), 
					job.getDelay(context), 
					TimeUnitConverter.getTimeUnit(job.getDelayUnit(context).getCaption())
					);
		
		scheduledJobRepository.add(context, job.getMendixObject(), future);
	}
	
	public void addRetry(IContext context, ILogNode logger, QueueRepository queueRepository, JobRepository jobRepository, ScheduledJobRepository scheduledJobRepository, JobValidator jobValidator, Job job, IUser user) throws CoreException {
		int newDelay= ExponentialBackoff.getExponentialBackOff(job.getDelay(context), job.getRetry(context));
		job.setDelay(context, newDelay);
		job.setRetry(context, job.getRetry(context) + 1);
		add(context, logger, queueRepository, jobRepository, scheduledJobRepository, jobValidator, job, true, user);
	}
}