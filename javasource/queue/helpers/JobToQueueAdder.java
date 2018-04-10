package queue.helpers;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IUser;

import queue.proxies.ENU_JobStatus;
import queue.proxies.Job;
import queue.repositories.ScheduledJobRepository;
import queue.repositories.JobRepository;
import queue.repositories.QueueRepository;

public class JobToQueueAdder {
	
	public boolean add(IContext context, ILogNode logger, QueueRepository queueRepository, JobRepository jobRepository, ScheduledJobRepository scheduledJobRepository, JobValidator jobValidator, Job job, boolean runFromUser) {
		boolean valid = jobValidator.isValid(queueRepository, job);
		
		if (valid == false) {
			return false;
		}
		
		ScheduledExecutorService executor = queueRepository.getQueue(job.getQueue());
		
		if(executor == null) {
			logger.error("Queue with name " + job.getQueue() + " could not be found. Job has not been added.");
			return false;
		}
		
		if(executor.isShutdown() || executor.isTerminated()) {
			logger.error("Queue with name " + job.getQueue() + " has already been shut down or terminated. Job has not been added.");
			return false;
		}
		
		
		job.setStatus(ENU_JobStatus.Queued);
		
		try {
			job.commit(context);
		} catch (Exception e) {
			logger.error("Could not commit job.");
			return false;
		}
		
		IUser user = null;
		
		if(runFromUser) {
			logger.debug("Run from user enabled. User will be added to queue handler.");
			user = context.getSession().getUser(context);
			logger.debug("User " + user.getName() + " added to queue handler.");
		}
		
		ScheduledFuture<?> future =	executor.schedule(
					queueRepository.getQueueHandler(logger, user, queueRepository, jobRepository, job.getMendixObject().getId()), 
					job.getDelay(), 
					TimeUnitConverter.getTimeUnit(job.getDelayUnit().getCaption())
					);
		
		scheduledJobRepository.add(job.getMendixObject(), future);
		
		return true;
	}
}
