package queue.helpers;

import java.util.concurrent.ScheduledFuture;

import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;

import queue.proxies.ENU_JobStatus;
import queue.proxies.Job;
import queue.repositories.ScheduledJobRepository;

public class JobCanceller {
	
	public boolean cancel(IContext context, ScheduledJobRepository scheduledJobRepository, Job job, boolean removeWhenRunning) throws CoreException {
		ScheduledFuture<?> future; 
		future = scheduledJobRepository.get(context, job.getMendixObject());
		
		if(future == null) {
			throw new CoreException("Job cannot be cancelled, because ScheduledFuture does not exist.");
		}
		
		boolean cancelled = future.cancel(removeWhenRunning);
		
		if(cancelled) {
			job.setStatus(ENU_JobStatus.Cancelled);
			job.commit();
		}
		
		scheduledJobRepository.remove(context, job.getMendixObject());
		
		return true;
	}

}
