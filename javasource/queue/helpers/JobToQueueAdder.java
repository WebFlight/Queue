package queue.helpers;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;

import queue.proxies.ENU_JobStatus;
import queue.proxies.Job;
import queue.repositories.JobRepository;
import queue.repositories.QueueRepository;
import queue.usecases.QueueHandler;

public class JobToQueueAdder {
	
	public boolean add(IContext context, ILogNode logger, JobRepository jobRepository, JobValidator jobValidator, Job job) {
		boolean valid = jobValidator.isValid(job);
		
		if (valid == false) {
			return false;
		}
		
		
		ScheduledExecutorService executor = QueueRepository.getQueue(job.getQueue());
		
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
		
		ScheduledFuture<?> future =	executor.schedule(
					new QueueHandler(job.getMendixObject().getId()), 
					job.getDelay(), 
					TimeUnitConverter.getTimeUnit(job.getDelayUnit().getCaption())
					);
		
		jobRepository.add(job.getMendixObject(), future);
		
		return true;
	}
}
